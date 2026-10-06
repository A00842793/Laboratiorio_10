package mx.tec.avisos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import mx.tec.avisos.domain.Aviso
import mx.tec.avisos.ui.theme.AvisosTema
import mx.tec.avisos.ui.theme.AvisosTheme

/**
 * Un aviso del tablón. Dos versiones del mismo componente:
 *
 *   - La de abajo recibe los datos YA listos para mostrar (textos y un
 *     booleano). No sabe qué es un `Aviso` ni qué hora es: por eso sirve igual
 *     para la lista que para la vista previa de un aviso que todavía no existe.
 *   - La de arriba recibe un `Aviso` y hace la traducción. Es la cómoda.
 *
 * En la tarjeta no hay un solo color, tamaño ni radio escrito a mano: todo
 * sale del tema. Por eso cambia sola con el modo oscuro.
 */
@Composable
fun TarjetaAviso(
    aviso: Aviso,
    modifier: Modifier = Modifier,
    ahora: Long = System.currentTimeMillis()
) {
    TarjetaAviso(
        titulo = aviso.titulo,
        cuerpo = aviso.cuerpo,
        autor = aviso.autor,
        cuando = tiempoRelativo(aviso.creadoEn, ahora),
        nuevo = esReciente(aviso.creadoEn, ahora),
        imagen = aviso.imagenUrl,
        modifier = modifier
    )
}

@Composable
fun TarjetaAviso(
    titulo: String,
    cuerpo: String,
    autor: String,
    cuando: String,
    nuevo: Boolean,
    modifier: Modifier = Modifier,
    imagen: Any? = null
) {
    val espaciado = AvisosTema.espaciado

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        if (imagen != null) {
            AsyncImage(
                model = imagen,
                contentDescription = "Imagen del aviso",
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceContainerHighest),
                error = ColorPainter(MaterialTheme.colorScheme.surfaceContainerHighest),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
        }
        Column(
            modifier = Modifier.padding(espaciado.xl),
            verticalArrangement = Arrangement.spacedBy(espaciado.sm)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(espaciado.sm)
            ) {
                Avatar(autor)
                // Autor y hora en su propia fila, que se queda con todo el espacio
                // que sobra. Si no cabe, cede el autor —con "…"—, nunca la etiqueta.
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(espaciado.xs)
                ) {
                    Text(
                        text = autor,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = "· $cuando",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                if (nuevo) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Nuevo",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = espaciado.sm, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = cuerpo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true, name = "Nuevo · claro")
@Composable
private fun TarjetaNuevaPreview() {
    AvisosTheme(oscuro = false) {
        TarjetaAviso(
            titulo = "Examen parcial",
            cuerpo = "El parcial es el jueves a las 10:00 en el salón de siempre. Traigan lápiz.",
            autor = "profe.prueba",
            cuando = "hace 12 min",
            nuevo = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1512, name = "Anterior · oscuro")
@Composable
private fun TarjetaAnteriorPreview() {
    AvisosTheme(oscuro = true) {
        TarjetaAviso(
            titulo = "Bienvenidos al tablón",
            cuerpo = "Este aviso lo publicó el servidor al crear la tabla. Los siguientes los publica un profesor desde la app.",
            autor = "profesor",
            cuando = "21 sep",
            nuevo = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}
