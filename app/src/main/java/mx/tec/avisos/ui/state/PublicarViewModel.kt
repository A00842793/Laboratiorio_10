package mx.tec.avisos.ui.state

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import mx.tec.avisos.data.AvisosRepository
import mx.tec.avisos.data.ImagenesRepository
import mx.tec.avisos.data.imagenes.FotosTemporales
import mx.tec.avisos.data.imagenes.ImagenIlegible
import mx.tec.avisos.domain.AvisoValidator
import retrofit2.HttpException
import java.io.IOException

data class PublicarUiState(
    val titulo: String = "",
    val cuerpo: String = "",
    val imagen: Uri? = null,
    val etapa: String? = null,
    val error: String? = null
) {
    val enviando: Boolean = etapa != null

    val puedePublicar: Boolean = AvisoValidator.esValido(titulo, cuerpo) && !enviando

    val caracteresRestantes: Int = AvisoValidator.CUERPO_MAX - cuerpo.trim().length
}

@HiltViewModel
class PublicarViewModel @Inject constructor(
    private val avisos: AvisosRepository,
    private val imagenes: ImagenesRepository,
    private val fotos: FotosTemporales
) : ViewModel() {

    var uiState by mutableStateOf(PublicarUiState())
        private set

    fun onTituloChange(texto: String) {
        if (texto.length <= AvisoValidator.TITULO_MAX) uiState = uiState.copy(titulo = texto, error = null)
    }

    fun onCuerpoChange(texto: String) {
        if (texto.length <= AvisoValidator.CUERPO_MAX) uiState = uiState.copy(cuerpo = texto, error = null)
    }

    fun onImagenElegida(uri: Uri) {
        uiState = uiState.copy(imagen = uri, error = null)
    }

    fun quitarImagen() {
        uiState = uiState.copy(imagen = null, error = null)
    }

    fun nuevaFoto(): Uri = fotos.nueva()

    /** `alTerminar` se llama solo si el servidor aceptó el aviso. Un 403 se queda a la vista. */
    fun publicar(alTerminar: () -> Unit) {
        if (!uiState.puedePublicar) return
        viewModelScope.launch {
            val imagen = uiState.imagen
            try {
                val clave = if (imagen != null) {
                    uiState = uiState.copy(etapa = "Subiendo la imagen…", error = null)
                    imagenes.subir(imagen)
                } else {
                    null
                }
                uiState = uiState.copy(etapa = "Publicando…", error = null)
                avisos.publicar(uiState.titulo, uiState.cuerpo, clave)
                fotos.limpiar()
                uiState = uiState.copy(etapa = null)
                alTerminar()
            } catch (e: ImagenIlegible) {
                uiState = uiState.copy(etapa = null, error = "No se pudo leer esa imagen. Elige otra.")
            } catch (e: IOException) {
                uiState = uiState.copy(etapa = null, error = "No hay conexión. El aviso no se publicó.")
            } catch (e: HttpException) {
                uiState = uiState.copy(etapa = null, error = mensajeDe(e))
            }
        }
    }
}
