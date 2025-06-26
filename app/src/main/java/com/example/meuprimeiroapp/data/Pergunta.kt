package com.example.meuprimeiroapp.data
data class Question(
    val id: Int,
    val questionText: String,
    val imageResId: Int? = null,
    val options: List<String>,
    val correctAnswer: Int,
    val category: String,
    val difficulty: Int // 1 (Fácil) - 3 (Difícil)
)