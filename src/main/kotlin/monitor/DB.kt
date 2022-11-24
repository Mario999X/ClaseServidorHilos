package monitor

import model.Alumno
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

class DB {

    private val listaAlumnos = mutableListOf(
        Alumno("Mario", 10),
        Alumno("Federico", 7),
        Alumno("Jose", 1),
        Alumno("Javier", 10),
        Alumno("Loli", 10),
    )
}