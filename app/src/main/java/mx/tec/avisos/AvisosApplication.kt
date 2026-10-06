package mx.tec.avisos

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import dagger.hilt.android.HiltAndroidApp
import mx.tec.avisos.notificaciones.Notificador
import mx.tec.avisos.trabajo.RevisarAvisosWorker
import javax.inject.Inject

/**
 * Vive tanto como el proceso. Declarada en el manifiesto con `android:name`.
 *
 * Ya no hay `AppContainer`: quién construye a quién está en `di/`, y Hilt
 * escribe el contenedor al compilar (Práctica 9).
 *
 * `Configuration.Provider`: WorkManager construye los workers, y por omisión
 * no sabe inyectarles nada. Con la fábrica de Hilt, sí. Por eso el manifiesto
 * apaga el arranque automático de WorkManager.
 */
@HiltAndroidApp
class AvisosApplication : Application(), Configuration.Provider, SingletonImageLoader.Factory {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var notificador: Notificador

    @Inject lateinit var imageLoader: dagger.Lazy<ImageLoader>

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader.get()

    override fun onCreate() {
        super.onCreate()
        notificador.crearCanal()
        // El worker se programa una vez y queda programado aunque la app muera.
        RevisarAvisosWorker.programar(this)
    }
}
