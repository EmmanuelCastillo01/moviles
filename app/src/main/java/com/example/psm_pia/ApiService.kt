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

    @FormUrlEncoded
    @POST("editar_usuario.php")
    suspend fun updateUser(
        @Field("original_gmail") originalGmail: String,
        @Field("new_gmail") newGmail: String,
        @Field("nombre_usuario") nombreUsuario: String,
        @Field("telefono") telefono: String
    ): Response<ApiResponse>

    @FormUrlEncoded
    @POST("eliminar_usuario.php")
    suspend fun deleteUser(
        @Field("gmail") gmail: String
    ): Response<ApiResponse>

    @FormUrlEncoded
    @POST("alta_receta.php")
    suspend fun addRecipe(
        @Field("id_usuario") idUsuario: Int,
        @Field("nombre_receta") nombreReceta: String,
        @Field("ingredientes") ingredientes: String,
        @Field("instrucciones") instrucciones: String,
        @Field("pais_origen") paisOrigen: String,
        @Field("dificultad") dificultad: String,
        @Field("tipo_platillo") tipoPlatillo: String,
        @Field("cantidad_personas") cantidadPersonas: String
    ): Response<ApiResponse>

    @GET("obtener_recetas.php")
    suspend fun getRecipes(
        @Query("id_usuario") idUsuario: Int
    ): Response<RecetasResponse>
}