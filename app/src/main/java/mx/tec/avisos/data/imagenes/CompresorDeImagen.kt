package mx.tec.avisos.data.imagenes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.math.max

/** No se pudo abrir o decodificar la imagen elegida. */
class ImagenIlegible : Exception("No se pudo leer la imagen")

/**
 * Reduce una foto ANTES de subirla. Una foto de 4032×3024 pesa unos 2 MB en JPEG,
 * pero en memoria ocupa 4032 × 3024 × 4 bytes ≈ 48 MB: por eso no se decodifica entera.
 *
 *  1. Se leen solo las medidas (`inJustDecodeBounds`): no se reserva ni un píxel.
 *  2. Se decodifica con `inSampleSize` (potencia de 2): el decodificador salta píxeles.
 *  3. Se ajusta al lado máximo exacto y se aplica la orientación EXIF.
 *  4. Se escribe un JPEG NUEVO: sin metadatos, o sea sin GPS, sin fecha, sin modelo de cámara.
 *
 * Todo corre en `Dispatchers.IO`: leer y comprimir bloquea.
 */
class CompresorDeImagen @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun comprimir(uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        try {
            val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            abrir(uri).use { BitmapFactory.decodeStream(it, null, medidas) }
            val ancho = medidas.outWidth
            val alto = medidas.outHeight
            if (ancho <= 0 || alto <= 0) throw ImagenIlegible()

            // La mayor potencia de 2 que todavía deja el lado largo >= LADO_MAX.
            var muestra = 1
            while (max(ancho, alto) / (muestra * 2) >= LADO_MAX) muestra *= 2

            val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
            val leido = abrir(uri).use { BitmapFactory.decodeStream(it, null, opciones) } ?: throw ImagenIlegible()

            val orientacion = abrir(uri).use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }

            // Un solo paso: escala al lado máximo y gira lo que diga la orientación.
            val lado = max(leido.width, leido.height)
            val matriz = Matrix().apply {
                if (lado > LADO_MAX) postScale(LADO_MAX.toFloat() / lado, LADO_MAX.toFloat() / lado)
                aplicarOrientacion(orientacion)
            }
            val final = Bitmap.createBitmap(leido, 0, 0, leido.width, leido.height, matriz, true)

            val salida = ByteArrayOutputStream()
            final.compress(Bitmap.CompressFormat.JPEG, CALIDAD, salida)
            if (final !== leido) leido.recycle()
            final.recycle()
            salida.toByteArray()
        } catch (e: IOException) {
            throw ImagenIlegible()
        } catch (e: SecurityException) {
            throw ImagenIlegible()
        }
    }

    private fun abrir(uri: Uri) = context.contentResolver.openInputStream(uri) ?: throw ImagenIlegible()

    private fun Matrix.aplicarOrientacion(orientacion: Int) {
        when (orientacion) {
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { postRotate(90f); postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE -> { postRotate(270f); postScale(-1f, 1f) }
        }
    }

    companion object {
        /** Lado largo máximo, en px. Cambiarlo cambia el peso (y la nitidez) de todo lo que se sube. */
        const val LADO_MAX = 1280
        const val CALIDAD = 80
    }
}
