package server

import model.Alumno
import monitor.DB
import mu.KotlinLogging
import java.net.ServerSocket
import java.net.Socket

private val log = KotlinLogging.logger {}

private const val PUERTO = 6969

// No me hago responsable por el codigo que voy a tener que hacer ni de lo que eso conlleve
fun main() {

    // Datos del servidor y variables
    val servidor: ServerSocket
    var cliente: Socket

    // Preparamos la SC con monitor
    val db = DB()

    // Alumnos base para la lista
    val alumnosBase = listOf(
        Alumno("Mario", 10),
        Alumno("Kratos", 5),
    )
    // Introducimos los alumnos
    repeat(alumnosBase.size) {
        db.put(alumnosBase[it])
    }

    log.debug { "Arrancando servidor..." }
    try {
        servidor = ServerSocket(PUERTO)
        while (true) {
            log.debug { "\t--Servidor esperando..." }

            cliente = servidor.accept()
            log.debug { "Peticion de cliente -> " + cliente.inetAddress + " --- " + cliente.port }

            val gc = GestionClientes(cliente, db)
            gc.run()
            log.debug { "Cliente -> " + cliente.inetAddress + " --- " + cliente.port + " desconectado." }
        }
        //servidor.close()
    } catch (e: IllegalStateException) {
        e.printStackTrace()
    }
}