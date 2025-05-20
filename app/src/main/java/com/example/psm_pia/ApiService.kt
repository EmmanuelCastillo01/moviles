package com.example.psm_pia

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @Multipart
    @POST("alta_usuario.php")
    suspend fun registerUser(
        @Part("gmail") gmail: String,
        @Part("nombre_usuario") nombreUsuario: String,
        @Part("contrasena") contrasena: String,
        @Part("telefono") telefono: String,
        @Part("imagen") imagen: String?
    ): Response<ApiResponse>

    @FormUrlEncoded
    @POST("login_usuario.php")
    suspend fun loginUser(
        @Field("gmail") gmail: String,
        @Field("contraseña") contraseña: String
    ): Response<ApiResponse>
}