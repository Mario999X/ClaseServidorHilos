package server

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.Alumno
import model.mensajes.Request
import model.mensajes.Request.Type.*
import model.mensajes.Response
import monitor.DB
import mu.KotlinLogging
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

private val log = KotlinLogging.logger { }
private val json = Json

private lateinit var response: Response<String>
private lateinit var request: Request<Alumno>
private lateinit var requestGenerica: Request<Int>

class GestionClientes(private val s: Socket, private val db: DB) : Runnable {

    override fun run() {
        // Recibo el aviso del cliente
        val readerRequest = DataInputStream(s.getInputStream())
        val signal = readerRequest.read()

        // Devuelvo la señal al cliente para indicar que ha llegado
        val sendResponse = DataOutputStream(s.getOutputStream())
        sendResponse.write(signal)

        // Recibo el Request mandado por el cliente y actua en consecuencia segun el signal recibido
        if (signal == 1) {
            request = json.decodeFromString<Request<Alumno>>(readerRequest.readUTF())
            log.debug { "Recibido: $request" }

            // Recogemos el contenido y lo casteamos a Alumno
            val alumno = request.content as Alumno

            when (request.type) {
                ADD -> {
                    log.debug { "Alumno: $alumno" }
                    db.put(alumno)
                    log.debug { "Alumno agregado" }
                    response = Response("Operacion realizada", Response.Type.OK)
                }

                UPDATE -> {
                    log.debug { "Alumno: $alumno" }
                    val existe = alumno.id?.let { db.update(alumno.id!!, alumno) }
                    response = if (!existe!!) {
                        Response("Alumno no existe", Response.Type.OK)
                    } else Response("Alumno actualizado", Response.Type.OK)
                }

                else -> {
                    response = Response("Error | signal recibido: $signal ", Response.Type.ERROR)
                }
            }

            // Request Generica
        } else {
            requestGenerica = json.decodeFromString<Request<Int>>(readerRequest.readUTF())
            log.debug { "Recibido: $request" }

            // Recogemos el contenido y lo casteamos a Alumno
            val numOpcion = requestGenerica.content as Int

            when (requestGenerica.type) {
                DELETE -> {

                    log.debug { "ID: $numOpcion" }
                    val existe = numOpcion.let { db.delete(it) }
                    response = if (!existe) {
                        Response("Alumno no existe", Response.Type.OK)
                    } else Response("Alumno eliminado", Response.Type.OK)

                }

                CONSULT -> {

                    val listaAlumnos = db.getAll().toSortedMap()
                    log.debug { "Obteniendo lista en orden pedido" }
                    var orden = listOf<Alumno>()

                    if (numOpcion == 1) {
                        orden = listaAlumnos.values.sortedBy { it.nombre }
                    }
                    if (numOpcion == 2) {
                        orden = listaAlumnos.values.sortedByDescending { it.nota }
                    }
                    if (numOpcion == 3) {
                        orden = listaAlumnos.values.filter { it.nota >= 5 }
                    }
                    if (numOpcion == 4) {
                        orden = listaAlumnos.values.filter { it.nota < 5 }
                    }
                    response = Response(orden.toString(), Response.Type.OK)

                    if (numOpcion == 5) {
                        val alumnos = db.getAll().values.toList()
                        val media = alumnos.stream().mapToInt { it.nota }.average()

                        response = Response(media.toString(), Response.Type.OK)
                    }
                }

                else -> {
                    response = Response("Error | signal recibido: $signal ", Response.Type.ERROR)
                }
            }
        }
        // Como los Response son mandados como un String, se puede reutilizar asi
        sendResponse.writeUTF(json.encodeToString(response) + "\n")
    }
}