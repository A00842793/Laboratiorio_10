package mx.tec.avisos.data.remote

import mx.tec.avisos.domain.Aviso
import mx.tec.avisos.domain.Rol
import mx.tec.avisos.domain.Sesion

/**
 * El servidor dice "expira en 300 segundos"; la app necesita saber CUÁNDO.
 * Se traduce aquí, en el momento en que llega la respuesta.
 */
fun TokensDto.toSesion(ahora: Long = System.currentTimeMillis() / 1000) = Sesion(
    usuario = usuario,
    rol = Rol.de(rol),
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiraEn = ahora + expiresIn
)

fun AvisoDto.toDomain() = Aviso(
    id = id,
    titulo = titulo,
    cuerpo = cuerpo,
    autor = autor,
    creadoEn = createdAt,
    imagenUrl = imagen?.let { "${Network.BASE_URL}imagenes/$it" }
)
