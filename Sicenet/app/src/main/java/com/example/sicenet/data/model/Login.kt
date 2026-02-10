package com.example.sicenet.data.model

data class Login(
    val acceso: Boolean,
    val mensaje: String,
    val cookie: String? = null
)