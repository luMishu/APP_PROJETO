package com.example.meuprimeiroapp.data

data class Achievement(
    val id: Int,
    val name: String,              // Ex: "Mestre da Natureza"
    val description: String,       // Ex: "Acertou 10 perguntas seguidas"
    val iconResId: Int,           // Ícone (R.drawable.ic_achievement)
    val isUnlocked: Boolean = false
)