package mx.tec.avisos.ui.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.tec.avisos.domain.Sesion
import mx.tec.avisos.notificaciones.PedirPermisoDeNotificaciones
import mx.tec.avisos.ui.screens.AvisosScreen
import mx.tec.avisos.ui.screens.PublicarScreen
import mx.tec.avisos.ui.state.AvisosViewModel
import mx.tec.avisos.ui.state.PublicarViewModel

/** Las pantallas que existen SOLO con sesión. Recibe la sesión ya resuelta: aquí nunca es null. */
@Composable
fun AvisosNavHost(sesion: Sesion, onSalir: () -> Unit) {
    val nav = rememberNavController()

    // Ya entró y ya sabe qué es la app: ahora sí tiene sentido pedirle permiso para avisarle.
    PedirPermisoDeNotificaciones()

    NavHost(navController = nav, startDestination = Route.AVISOS) {

        composable(Route.AVISOS) {
            val viewModel: AvisosViewModel = hiltViewModel()

            // Al pasar a START (la pantalla se ve) carga y se queda escuchando;
            // al pasar a STOP (la app se fue al fondo) cierra la conexión.
            // Es el mismo "cuándo escuchar" del stateIn de la Práctica 5, hecho a mano.
            LifecycleStartEffect(Unit) {
                viewModel.cargar()
                onStopOrDispose { viewModel.dejarDeEscuchar() }
            }

            AvisosScreen(
                sesion = sesion,
                avisos = viewModel.avisos,
                onRecargar = { viewModel.cargar() },
                onPublicar = { nav.navigate(Route.PUBLICAR) },
                onSalir = onSalir
            )
        }

        composable(Route.PUBLICAR) {
            val viewModel: PublicarViewModel = hiltViewModel()

            val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) viewModel.onImagenElegida(uri)
            }

            var fotoPendiente by rememberSaveable { mutableStateOf<Uri?>(null) }
            val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
                val uri = fotoPendiente
                if (tomada && uri != null) viewModel.onImagenElegida(uri)
                fotoPendiente = null
            }

            PublicarScreen(
                uiState = viewModel.uiState,
                // Para la vista previa: el aviso sale firmado por quien tiene la sesión.
                autor = sesion.usuario,
                onTituloChange = viewModel::onTituloChange,
                onCuerpoChange = viewModel::onCuerpoChange,
                onGaleria = { galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onCamara = {
                    val uri = viewModel.nuevaFoto()
                    fotoPendiente = uri
                    camara.launch(uri)
                },
                onQuitarImagen = viewModel::quitarImagen,
                // El popBackStack ocurre cuando el servidor aceptó, no antes.
                onPublicar = { viewModel.publicar { nav.popBackStack() } },
                onCancelar = { nav.popBackStack() }
            )
        }
    }
}
