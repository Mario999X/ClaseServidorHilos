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

            when (request.type) {
                ADD -> {
                    // Recogemos el contenido y lo casteamos a Alumno
                    val alumno = request.content as Alumno

                    log.debug { "Alumno: $alumno" }
                    db.put(alumno)
                    log.debug { "Alumno agregado" }
                    response = Response("Operacion realizada", Response.Type.OK)
                }

                UPDATE -> {
                    val alumno = request.content as Alumno

                    log.debug { "Alumno: $alumno" }
                    val existe = alumno.id?.let { db.update(alumno.id!!, alumno) }
                    response = if (!existe!!) {
                        Response("Alumno no existe", Response.Type.OK)
                    } else Response("Alumno actualizado", Response.Type.OK)
                }

                else -> {
                    response = Response("Tipo no reconocido", Response.Type.ERROR)
                }
            }

            // Request Generica
        } else {
            requestGenerica = json.decodeFromString<Request<Int>>(readerRequest.readUTF())
            log.debug { "Recibido: $request" }

            when (requestGenerica.type) {
                DELETE -> {
                    val id = requestGenerica.content

                    log.debug { "ID: $id" }
                    val existe = id?.let { db.delete(it) }
                    response = if (!existe!!) {
                        Response("Alumno no existe", Response.Type.OK)
                    } else Response("Alumno eliminado", Response.Type.OK)

                }

                CONSULT -> {
                    val opcion = requestGenerica.content

                    val listaAlumnos = db.getAll().toSortedMap()
                    log.debug { "Obteniendo lista en orden pedido" }
                    var orden: List<Alumno>

                    if (opcion == 1) {
                        orden = listaAlumnos.values.sortedBy { it.nombre }
                        response = Response(orden.toString(), Response.Type.OK)
                    }
                    if (opcion == 2) {
                        orden = listaAlumnos.values.sortedByDescending { it.nota }
                        response = Response(orden.toString(), Response.Type.OK)
                    }
                }

                else -> {
                    response = Response("Tipo no reconocido", Response.Type.ERROR)
                }
            }
        }
        // Como los Response son mandados como un String, se puede reutilizar asi
        sendResponse.writeUTF(json.encodeToString(response) + "\n")
    }
}