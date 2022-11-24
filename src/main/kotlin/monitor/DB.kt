package monitor

import model.Alumno
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private val log = KotlinLogging.logger {}

class DB {

    private val listaAlumnos = mutableMapOf<Int, Alumno>()

    private var numAlumno = AtomicInteger(1)

    // Lock
    private val lock = ReentrantLock()

    // Obtenemos el listado de alumnos
    fun getAll(): Map<Int, Alumno> {
        lock.withLock {
            val mapa = listaAlumnos

            log.debug { "\tSe envia el listado de alumnos..." }

            return mapa
        }
    }

    // Introducimos un alumno con ID fijo y en aumento
    fun put(item: Alumno) {
        lock.withLock {
            log.debug { "\tAlumno -> $numAlumno / $item agregado" }
            item.id = numAlumno.toInt()
            listaAlumnos[numAlumno.toInt()] = item
            numAlumno.incrementAndGet()
        }
    }

    // Actualizamos a un alumno (Nombre y Nota) segun su ID
    fun update(item: Int, alumno: Alumno): Boolean {
        lock.withLock {
            var existe = false
            if (listaAlumnos.containsKey(item)) {
                log.debug { "\tAlumno -> ${listaAlumnos[item]} antiguo" }
                listaAlumnos[item] = alumno
                log.debug { "\tAlumno -> ${listaAlumnos[item]} actualizado" }
                existe = true
            } else {
                log.debug { "\tID NO EXISTE -> $item" }
            }
            return existe
        }
    }

    // Borramos a un alumno segun su ID
    fun delete(item: Int): Boolean {
        lock.withLock {
            var existe = false
            if (listaAlumnos.containsKey(item)) {
                log.debug { "\tAlumno -> ${listaAlumnos[item]} eliminado" }
                listaAlumnos.remove(item)
                existe = true
            } else {
                log.debug { "\tID NO EXISTE -> $item" }
            }
            return existe
        }
    }

}