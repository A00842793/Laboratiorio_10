package mx.tec.avisos.data.remote

import kotlinx.serialization.Serializable

/** Lo que se manda al entrar o al registrarse. `codigoProfesor` solo al registrarse, y es opcional. */
@Serializable
data class Credenciales(
    val usuario: String,
    val password: String,
    val codigoProfesor: String? = null
)

/** Lo que el servidor devuelve a cambio: el par de tokens y quién eres. */
@Serializable
data class TokensDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val usuario: String,
    val rol: String
)

@Serializable
data class RefreshBody(val refreshToken: String)

@Serializable
data class MeDto(
    val usuario: String,
    val rol: String,
    val exp: Long,
    val sesionesActivas: Int
)

@Serializable
data class AvisoDto(
    val id: Int,
    val titulo: String,
    val cuerpo: String,
    val autor: String,
    val createdAt: String,
    val imagen: String? = null
)

@Serializable
data class NuevoAvisoBody(val titulo: String, val cuerpo: String)
