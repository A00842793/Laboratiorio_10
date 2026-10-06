package mx.tec.avisos.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AvisosApi {

    // ---- sin token: son las llamadas que te dan uno
    @POST("auth/register")
    suspend fun register(@Body body: Credenciales): TokensDto

    @POST("auth/login")
    suspend fun login(@Body body: Credenciales): TokensDto

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshBody): TokensDto

    @POST("auth/logout")
    suspend fun logout(@Body body: RefreshBody)

    // ---- con token: el `Authorization: Bearer …` lo pone el interceptor
    @GET("auth/me")
    suspend fun me(): MeDto

    /** `desde`: solo los avisos con id mayor. Con 0, todos. Lo usa el worker para no bajar el tablón entero. */
    @GET("avisos")
    suspend fun getAvisos(@Query("desde") desde: Int = 0): List<AvisoDto>

    @POST("avisos")
    suspend fun crearAviso(@Body body: NuevoAvisoBody): AvisoDto

    @Multipart
    @POST("imagenes")
    suspend fun subirImagen(@Part archivo: MultipartBody.Part): ImagenDto

    @DELETE("avisos/{id}")
    suspend fun borrarAviso(@Path("id") id: Int): Response<Unit>
}
