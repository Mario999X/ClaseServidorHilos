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
private lateinit var requestGenerica: Request<Int>

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

    var aviso = 0 // 0 -> Request Generica [Int], 1 -> Request de Alumno

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
                // Las notas van de 0 a 10, punto.
                if (nota < 0) nota = 0
                if (nota > 10) nota = 10

                alumno = Alumno(nombre, nota)
                request = Request(alumno, Request.Type.ADD)

                log.debug { "Alumno enviado, esperando respuesta..." }
                aviso = 1
            }

            2 -> {
                log.debug { "\tIntroduzca el ID del alumno: " }
                id = readln().toInt()
                requestGenerica = Request(id, Request.Type.DELETE)

                log.debug { "ID enviado, esperando respuesta..." }
                aviso = 0
            }

            3 -> {
                log.debug { "\tIntroduzca el nuevo NOMBRE del alumno: " }
                nombre = readln()

                log.debug { "\tIntroduzca la nueva NOTA SIN DECIMALES del alumno: " }
                nota = readln().toInt()
                // Las notas van de 0 a 10, punto.
                if (nota < 0) nota = 0
                if (nota > 10) nota = 10

                log.debug { "\tIntroduzca el ID del alumno existente: " }
                id = readln().toInt()

                request = Request(Alumno(nombre, nota, id), Request.Type.UPDATE)
                log.debug { "Alumno enviado para actualizar, esperando respuesta..." }
                aviso = 1
            }

            4 -> {
                log.debug {
                    """Elija el orden de los alumnos a mostrar:
                    |1. Alfabetico
                    |2. Nota
                    |3. Solo APROBADOS
                    |4. Solo SUSPENSOS
                    |5. Media de NOTAS
                """.trimMargin()
                }
                id = readln().toInt() // Reutilizando el codigo
                if (id == 1) {
                    requestGenerica = Request(id, Request.Type.CONSULT)
                }
                if (id == 2) {
                    requestGenerica = Request(id, Request.Type.CONSULT)
                }
                if (id == 3) {
                    requestGenerica = Request(id, Request.Type.CONSULT)
                }
                if (id == 4) {
                    requestGenerica = Request(id, Request.Type.CONSULT)
                }
                if (id == 5) {
                    requestGenerica = Request(id, Request.Type.CONSULT)
                }
                if (id < 1 || id > 5) {
                    requestGenerica = Request(id, Request.Type.ERROR)
                }
                log.debug { "Esperando listado..." }
                aviso = 0
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
                // Conectamos con el servidor, y segun la opcion seleccionada, le enviamos un aviso (un Int por ejemplo)
                direccion = InetAddress.getLocalHost()
                servidor = Socket(direccion, puerto)

                val sendRequest = DataOutputStream(servidor.getOutputStream())
                // Enviamos el aviso de Int segun la opcion escogida para que el servidor sepa que Request va a recibir
                sendRequest.write(aviso)
                // Esperamos respuesta
                val receiveResponse = DataInputStream(servidor.getInputStream())
                receiveResponse.read()

                // Segun la opcion escogida, enviamos un request o otro
                if (opcion == 1 || opcion == 3) {
                    // Envio la Request / Espero la respuesta
                    sendRequest.writeUTF(json.encodeToString(request) + "\n")
                    log.debug { "$request enviada con exito, esperando respuesta..." }
                }

                if (opcion == 2 || opcion == 4) {
                    // Envio la Request / Espero la respuesta
                    sendRequest.writeUTF(json.encodeToString(requestGenerica) + "\n")
                    log.debug { "$requestGenerica enviada con exito, esperando respuesta..." }
                }

                // Respuesta del servidor a la opcion escogida
                val response = json.decodeFromString<Response<String>>(receiveResponse.readUTF())
                log.debug { "Respuesta del servidor: ${response.content}" }

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}