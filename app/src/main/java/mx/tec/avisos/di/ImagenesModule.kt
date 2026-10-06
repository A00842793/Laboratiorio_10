package mx.tec.avisos.di

import android.content.Context
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Coil descarga con el MISMO `OkHttpClient` de la app: el que firma cada petición
 * con `Authorization: Bearer …` y renueva el token cuando el servidor dice 401.
 * Con un cliente propio, GET /imagenes/:clave llegaría sin token y el servidor
 * respondería 401 (es el experimento A3).
 */
@Module
@InstallIn(SingletonComponent::class)
object ImagenesModule {

    @Provides
    @Singleton
    fun imageLoader(@ApplicationContext context: Context, cliente: OkHttpClient): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { cliente })) }
            .crossfade(true)
            .build()
}
