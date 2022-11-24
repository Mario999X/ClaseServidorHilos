package model

import kotlinx.serialization.Serializable

@Serializable
data class Alumno(
    val nombre: String,
    val nota: Int
){
    override fun toString(): String {
        return "Alumno('$nombre', nota=$nota)"
    }
}
