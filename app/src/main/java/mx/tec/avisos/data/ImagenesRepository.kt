package mx.tec.avisos.data

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton
import mx.tec.avisos.data.imagenes.CompresorDeImagen
import mx.tec.avisos.data.remote.AvisosApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Subir una imagen son DOS peticiones, no una: primero la imagen (devuelve su clave),
 * luego el aviso (que lleva la clave). Así el aviso sigue siendo un JSON pequeño.
 */
@Singleton
class ImagenesRepository @Inject constructor(
    private val api: AvisosApi,
    private val compresor: CompresorDeImagen
) {
    /** Reduce la imagen y la sube. Devuelve la clave que guardó el servidor. */
    suspend fun subir(uri: Uri): String {
        val bytes = compresor.comprimir(uri)
        val parte = MultipartBody.Part.createFormData(
            name = "archivo",
            filename = "foto.jpg",
            body = bytes.toRequestBody("image/jpeg".toMediaType())
        )
        return api.subirImagen(parte).id
    }
}
