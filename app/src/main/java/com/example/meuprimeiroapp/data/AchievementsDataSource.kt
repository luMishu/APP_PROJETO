package com.example.meuprimeiroapp.data
import com.example.meuprimeiroapp.R  // Ajuste para seu pacote!
object AchievementsDataSource {
    fun getAchievements(): List<Achievement> {
        return listOf(
            Achievement(
                id = 1,
                name = "Novato",
                description = "Complete seu primeiro quiz",
                iconResId = R.drawable.ic_novato
            ),
            Achievement(
                id = 2,
                name = "Especialista",
                description = "Acertou 10 perguntas difíceis",
                iconResId = R.drawable.ic_especialista
            )
        )
    }
}