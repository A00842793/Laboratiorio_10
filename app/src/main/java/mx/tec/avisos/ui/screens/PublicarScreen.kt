package mx.tec.avisos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import mx.tec.avisos.R
import mx.tec.avisos.domain.AvisoValidator
import mx.tec.avisos.ui.components.BotonPrincipal
import mx.tec.avisos.ui.components.CampoTexto
import mx.tec.avisos.ui.components.TarjetaAviso
import mx.tec.avisos.ui.state.PublicarUiState
import mx.tec.avisos.ui.theme.AvisosTema
import mx.tec.avisos.ui.theme.AvisosTheme

/**
 * Escribir un aviso. La pieza nueva es la vista previa: es la MISMA
 * `TarjetaAviso` del tablón, alimentada con lo que el profesor lleva tecleado.
 * Si mañana cambia la tarjeta, la vista previa cambia con ella — no hay una
 * copia que mantener al día.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicarScreen(
    uiState: PublicarUiState,
    autor: String,
    onTituloChange: (String) -> Unit,
    onCuerpoChange: (String) -> Unit,
    onGaleria: () -> Unit,
    onCamara: () -> Unit,
    onQuitarImagen: () -> Unit,
    onPublicar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val espaciado = AvisosTema.espaciado

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Nuevo aviso", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onCancelar) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
                    }
                }
            )
        },
        // La acción principal abajo, y sube con el teclado: nunca queda tapada.
        bottomBar = {
            BotonPrincipal(
                texto = uiState.etapa ?: "Publicar",
                onClick = onPublicar,
                habilitado = uiState.puedePublicar,
                cargando = uiState.enviando,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(espaciado.lg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = espaciado.lg, vertical = espaciado.sm),
            verticalArrangement = Arrangement.spacedBy(espaciado.lg)
        ) {
            CampoTexto(
                valor = uiState.titulo,
                onValorChange = onTituloChange,
                etiqueta = "Título",
                ayuda = "De ${AvisoValidator.TITULO_MIN} a ${AvisoValidator.TITULO_MAX} caracteres",
                contador = "${uiState.titulo.length}/${AvisoValidator.TITULO_MAX}"
            )
            CampoTexto(
                valor = uiState.cuerpo,
                onValorChange = onCuerpoChange,
                etiqueta = "Aviso",
                contador = "${uiState.cuerpo.length}/${AvisoValidator.CUERPO_MAX}",
                lineasMinimas = 4
            )

            Row(horizontalArrangement = Arrangement.spacedBy(espaciado.sm)) {
                OutlinedButton(onClick = onGaleria, enabled = !uiState.enviando) {
                    Icon(painterResource(R.drawable.ic_galeria), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(espaciado.sm))
                    Text("Galería")
                }
                OutlinedButton(onClick = onCamara, enabled = !uiState.enviando) {
                    Icon(painterResource(R.drawable.ic_camara), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(espaciado.sm))
                    Text("Cámara")
                }
                if (uiState.imagen != null) {
                    TextButton(onClick = onQuitarImagen, enabled = !uiState.enviando) { Text("Quitar") }
                }
            }

            // El error del servidor: un 403, un 422 que la validación no atrapó,
            // o una caída de red. La pantalla NO se cierra.
            val error = uiState.error
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Text(
                text = "Vista previa",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TarjetaAviso(
                titulo = uiState.titulo.ifBlank { "Título del aviso" },
                cuerpo = uiState.cuerpo.ifBlank { "Así se va a ver tu aviso en el tablón." },
                autor = autor,
                cuando = "ahora",
                nuevo = true,
                imagen = uiState.imagen
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 844)
@Composable
private fun PublicarPreview() {
    AvisosTheme {
        PublicarScreen(
            uiState = PublicarUiState(
                titulo = "Examen parcial",
                cuerpo = "El parcial es el jueves a las 10:00 en el salón de siempre. Traigan lápiz."
            ),
            autor = "profe.prueba",
            onTituloChange = {}, onCuerpoChange = {}, onGaleria = {}, onCamara = {},
            onQuitarImagen = {}, onPublicar = {}, onCancelar = {}
        )
    }
}
