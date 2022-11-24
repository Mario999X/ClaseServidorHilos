package server

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.Alumno
import model.mensajes.Request
import model.mensajes.Response
import monitor.DB
import mu.KotlinLogging
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

private val log = KotlinLogging.logger { }
private val json = Json

private lateinit var response: Response<String>

class GestionClientes(private val s: Socket, private val db: DB) : Runnable {

    override fun run() {
        // Recibo el Request mandado por el cliente y actua en consecuencia
        val readerRequest = DataInputStream(s.getInputStream())
        val request = json.decodeFromString<Request<Alumno>>(readerRequest.readUTF())
        log.debug { "Recibido: $request" }

        // Recogemos el contenido y lo casteamos a Alumno, en algunos casos no nos importara ni el nombre ni la nota...
        val alumno = request.content as Alumno

        when(request.type){
            Request.Type.ADD -> {
                log.debug { "Alumno: $alumno" }
                db.put(alumno)
                log.debug { "Alumno agregado" }
                response = Response("Operacion realizada", Response.Type.OK)
            }
            Request.Type.CONSULT -> {
                val listaAlumnos = db.getAll().toSortedMap()
                if (alumno.nota == 1){
                    log.debug { "Obteniendo lista en orden pedido"}
                    val orden = listaAlumnos.values.sortedBy { it.nombre }
                    response = Response(orden.toString(), Response.Type.OK)
                }
            }

            else -> {}
        }

        val sendResponse = DataOutputStream(s.getOutputStream())
        sendResponse.writeUTF(json.encodeToString(response) + "\n")
    }
}