package com.example.psm_pia

import android.R

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val id: Int? = null,
    val gmail: String? = null,
    val nombre_usuario: String? = null,
    val telefono: String? = null,
    val imagen: String? = null
)