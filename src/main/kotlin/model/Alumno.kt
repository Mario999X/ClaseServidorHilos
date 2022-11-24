package model

import kotlinx.serialization.Serializable

@Serializable
data class Alumno(
    private val nombre: String,
    private val nota: Int
){
    override fun toString(): String {
        return "Alumno('$nombre', nota=$nota)"
    }
}
