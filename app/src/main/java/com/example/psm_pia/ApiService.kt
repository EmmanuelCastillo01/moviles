package com.example.psm_pia

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.Response
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("alta_usuario.php")
    suspend fun registerUser(
        @Field("gmail") gmail: String,
        @Field("nombre_usuario") nombreUsuario: String,
        @Field("contraseña") contraseña: String,
        @Field("telefono") telefono: String,
        @Field("imagen") imagen: String
    ): Response<ApiResponse>
}