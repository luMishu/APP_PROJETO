package com.example.meuprimeiroapp.data
import com.example.meuprimeiroapp.R  // Ajuste para seu pacote!

object QuestionDataSource {
    fun getNatureQuestions(): List<Question> {
        return listOf(
            Question(
                id = 1,
                questionText = "Qual é a maior floresta tropical do mundo?",
                imageResId = R.mipmap.amazonia,
                options = listOf("Floresta do Congo", "Floresta Amazônica", "Floresta de Bornéu"),
                correctAnswer = 1,
                category = "Florestas",
                difficulty = 1
            ),
            Question(
                id = 2,
                questionText = "Qual animal é conhecido como 'rei da selva'?",
                options = listOf("Tigre", "Leão", "Elefante"),
                correctAnswer = 1,
                category = "Animais",
                difficulty = 1
            )
            // Adicione mais perguntas aqui...
        )
    }
}