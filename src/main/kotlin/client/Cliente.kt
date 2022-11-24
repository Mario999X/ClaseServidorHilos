package client

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.Alumno
import model.mensajes.Request
import model.mensajes.Response
import mu.KotlinLogging
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.Socket

private val log = KotlinLogging.logger { }
private val json = Json

// Varios tipos de request segun la operacion a realizar.
// Una vez se mande, y segun el tipo de Request, el gestor hara lo que deba con el contenido. (menos la opcion 5)
private lateinit var request: Request<Alumno>

fun main() {
    // Informacion del cliente y la conexion a realizar
    var direccion: InetAddress
    var servidor: Socket
    val puerto = 6969

    var salida = false

    // Datos para los Request
    lateinit var alumno: Alumno
    var nombre: String
    var nota: Int
    var id: Int

    while (!salida) {
        log.debug {
            """Por favor, seleccione una de las siguientes opciones:
            |1. AGREGAR ALUMNO
            |2. BORRAR ALUMNO
            |3. ACTUALIZAR ALUMNO
            |4. CONSULTAR ALUMNOS
            |5. SALIR
        """.trimMargin()
        }
        val opcion = readln().toIntOrNull()

        when (opcion) {
            1 -> {
                log.debug { "\tIntroduzca el NOMBRE del alumno: " }
                nombre = readln()
                log.debug { "\tIntroduzca la NOTA SIN DECIMALES del alumno: " }
                nota = readln().toInt()

                alumno = Alumno(nombre, nota)
                request = Request(alumno, Request.Type.ADD)

                log.debug { "Alumno enviado, esperando respuesta..." }
            }

            2 -> {
                log.debug { "\tIntroduzca el ID del alumno: " }
                id = readln().toInt()
                request = Request(Alumno("", null, id), Request.Type.DELETE)

                log.debug { "ID enviado, esperando respuesta..." }
            }

            3 -> {
                log.debug { "\tIntroduzca el NOMBRE del alumno: " }
                nombre = readln()
                log.debug { "\tIntroduzca la NOTA SIN DECIMALES del alumno: " }
                nota = readln().toInt()
                log.debug { "\tIntroduzca el ID del alumno: " }
                id = readln().toInt()

                request = Request(Alumno(nombre, nota, id), Request.Type.UPDATE)
                log.debug { "Alumno enviado para actualizar, esperando respuesta..." }
            }

            4 -> {
                log.debug {
                    """Elija el orden de los alumnos a mostrar:
                    |1. Alfabetico
                    |2. Nota
                """.trimMargin()
                }
                nota = readln().toInt() // Reutilizando el codigo
                if (nota == 1) {
                    request = Request(Alumno("", nota), Request.Type.CONSULT)
                }
                if (nota == 2) {
                    request = Request(Alumno("", nota), Request.Type.CONSULT)
                }
                log.debug { "Esperando listado..." }
            }

            5 -> {
                log.debug { " Saliendo del programa..." }
                salida = true
            }

            null -> {
                log.debug { "OPCION DESCONOCIDA..." }
            }
        }

        // CONEXION CON EL SERVIDOR / VUELTA AL WHEN SI FUE NULL | SALIDA DEL PROGRAMA SIN ENTRAR AL SERVER
        // Conexion cortante tipo HTTP, es decir, solo se abre momentaneamente.
        try {
            if (opcion == null || opcion <= 0 || opcion >= 5) {
                println("------------")
            } else {
                direccion = InetAddress.getLocalHost()
                servidor = Socket(direccion, puerto)

                // Envio la Request / Espero la respuesta
                val sendRequest = DataOutputStream(servidor.getOutputStream())
                sendRequest.writeUTF(json.encodeToString(request) + "\n")
                log.debug { "$request enviada con exito, esperando respuesta..." }

                val receiveResponse = DataInputStream(servidor.getInputStream())
                val response = json.decodeFromString<Response<String>>(receiveResponse.readUTF())
                log.debug { "Respuesta del servidor: ${response.content}" }

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}