package mx.tec.avisos.data.imagenes

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class FotosTemporales @Inject constructor(@ApplicationContext private val context: Context) {

    private val carpeta: File get() = File(context.filesDir, "fotos").apply { mkdirs() }

    fun nueva(): Uri {
        val archivo = File.createTempFile("foto-", ".jpg", carpeta)
        return FileProvider.getUriForFile(context, "${context.packageName}.fotos", archivo)
    }

    fun limpiar() {
        carpeta.listFiles()?.forEach { it.delete() }
    }
}
