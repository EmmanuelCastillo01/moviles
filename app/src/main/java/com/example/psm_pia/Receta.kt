package com.example.psm_pia

import com.google.gson.annotations.SerializedName

data class Receta(
    val id: Int,
    @SerializedName("nombre_receta") val nombreReceta: String?,
    @SerializedName("ingredientes") val ingredientes: String?,
    @SerializedName("instrucciones") val instrucciones: String?,
    @SerializedName("pais_origen") val paisOrigen: String?,
    @SerializedName("dificultad") val dificultad: String?,
    @SerializedName("tipo_platillo") val tipoPlatillo: String?,
    @SerializedName("cantidad_personas") val cantidadPersonas: String?
)

data class RecetasResponse(
    val success: Boolean,
    val message: String,
    val recetas: List<Receta>
)