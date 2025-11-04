package com.example.razer.model

import com.google.firebase.firestore.Exclude

data class Lista(
    @get:Exclude var id: String = "",
    val title: String = "",
    val status: String = "PENDIENTE",
    val userId: String = "",
    val items: List<ListaItem> = emptyList()
)