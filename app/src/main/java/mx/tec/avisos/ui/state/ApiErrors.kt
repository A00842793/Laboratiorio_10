package mx.tec.avisos.ui.state

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

/**
 * Traduce un código HTTP a algo que el usuario pueda leer. Cuando el servidor
 * manda un `error` en el cuerpo, se prefiere ese: él sabe mejor qué pasó.
 */
fun mensajeDe(e: HttpException): String {
    val cuerpo = e.response()?.errorBody()?.string()
    val mensaje = cuerpo
        ?.let { runCatching { Json.parseToJsonElement(it) }.getOrNull() }
        ?.jsonObject?.get("error")?.jsonPrimitive?.contentOrNull

    return when (e.code()) {
        401 -> mensaje ?: "Tu sesión no es válida. Vuelve a entrar."
        403 -> mensaje ?: "No tienes permiso para hacer eso."
        409 -> mensaje ?: "Ese usuario ya existe."
        422 -> mensaje ?: "Los datos no son válidos."
        else -> mensaje ?: "El servidor respondió ${e.code()}."
    }
}
