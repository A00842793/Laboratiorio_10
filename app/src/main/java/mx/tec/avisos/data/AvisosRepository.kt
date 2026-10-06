package mx.tec.avisos.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.tec.avisos.data.remote.AvisosApi
import mx.tec.avisos.data.remote.AvisosStream
import mx.tec.avisos.data.remote.NuevoAvisoBody
import mx.tec.avisos.data.remote.toDomain
import mx.tec.avisos.domain.Aviso

/**
 * El tablón. No sabe nada de tokens: el interceptor firma las peticiones por
 * debajo, y si el servidor dice 401 o 403, la excepción sube tal cual.
 *
 * Dos formas de leer, y no compiten: `obtener()` es una foto; `observar()`
 * es quedarse mirando.
 */
@Singleton
class AvisosRepository @Inject constructor(private val api: AvisosApi, private val stream: AvisosStream) {

    suspend fun obtener(desde: Int = 0): List<Aviso> = api.getAvisos(desde).map { it.toDomain() }

    /** Cada aviso nuevo, conforme llega. Se cierra cuando quien lo recolecta se va. */
    fun observar(desde: Int): Flow<Aviso> = stream.observar(desde).map { it.toDomain() }

    /** `imagen`: la clave que devolvió `ImagenesRepository.subir`, o null si el aviso no lleva. */
    suspend fun publicar(titulo: String, cuerpo: String, imagen: String? = null): Aviso =
        api.crearAviso(NuevoAvisoBody(titulo.trim(), cuerpo.trim(), imagen)).toDomain()
}
