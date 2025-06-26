package com.example.meuprimeiroapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Modelos de dados
data class Pergunta(
    val id: Int,
    val pergunta: String,
    val respostas: List<String>,
    val correta: Int,
    val categoria: String = "Geral",
    val dificuldade: String = "Fácil"
)

data class Medalha(
    val nome: String,
    val icone: String,
    val descricao: String,
    val condicao: (Int, Int, List<Pergunta>) -> Boolean
)

data class Itens(
    val tempoExtra: Int,
    val segundaChance: Int,
    val pularPergunta: Int
)

// Activity principal
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NaturezaQuizTheme {
                QuizApp()
            }
        }
    }
}

// Tema
@Composable
fun NaturezaQuizTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2E7D32),
            secondary = Color(0xFF81C784),
            surface = Color(0xFFE8F5E9)
        ),
        content = content
    )
}

// Componentes reutilizáveis
@Composable
fun InfoChip(icon: Any, text: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon as? ImageVector ?: Icons.Default.Info,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, color = color, fontSize = 14.sp)
        }
    }
}

@Composable
fun InfoBox(icon: Any, text: String, value: String, color: Color) {
    Card(
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon as? ImageVector ?: Icons.Default.Info,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(text, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AnswerButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(
            text,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ItemButton(icon: Any, text: String, count: Int, onClick: () -> Unit) {
    val enabled = count > 0

    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon as? ImageVector ?: Icons.Default.QuestionMark,
                contentDescription = text,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Text(text)
            Text("$count", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MedalhaItem(medalha: String) {
    Card(
        modifier = Modifier.padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Text(
            medalha,
            modifier = Modifier.padding(8.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

// Telas
@Composable
fun TelaInicial(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Bem-vindo ao Quiz da Natureza!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Teste seus conhecimentos sobre fauna, flora e ecologia!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStart,
            modifier = Modifier.width(200.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Iniciar Jogo")
        }
    }
}

@Composable
fun TelaQuiz(
    pergunta: Pergunta,
    nivel: Int,
    pontuacao: Int,
    erros: Int,
    medalhas: List<String>,
    itens: Itens,
    tempoRestante: Int,
    onAnswer: (Int) -> Unit,
    onItemUse: (String) -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoChip(icon = Icons.Default.Star, text = "Nível $nivel")
            InfoChip(icon = Icons.Default.Timer, text = "$tempoRestante s")
            InfoChip(
                icon = Icons.Default.AttachMoney,
                text = "$pontuacao pts",
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card da Pergunta
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Categoria: ${pergunta.categoria}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Dificuldade: ${pergunta.dificuldade}",
                        style = MaterialTheme.typography.labelLarge,
                        color = when(pergunta.dificuldade) {
                            "Fácil" -> Color(0xFF4CAF50)
                            "Médio" -> Color(0xFFFFC107)
                            else -> Color(0xFFF44336)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    pergunta.pergunta,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pergunta.respostas.size) { index ->
                        AnswerButton(
                            text = pergunta.respostas[index],
                            onClick = { onAnswer(index) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progresso
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoBox(
                icon = Icons.Default.Warning,
                text = "Erros",
                value = "$erros/3",
                color = if (erros >= 2) Color.Red else MaterialTheme.colorScheme.onSurface
            )

            InfoBox(
                icon = Icons.Default.MilitaryTech,
                text = "Medalhas",
                value = medalhas.size.toString(),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Itens
        Text(
            "Itens Especiais:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ItemButton(
                icon = Icons.Default.SkipNext,
                text = "Pular",
                count = itens.pularPergunta,
                onClick = { onItemUse("pular") }
            )

            ItemButton(
                icon = Icons.Default.Timer,
                text = "+10s",
                count = itens.tempoExtra,
                onClick = { onItemUse("tempo") }
            )

            ItemButton(
                icon = Icons.Default.Autorenew,
                text = "Chance",
                count = itens.segundaChance,
                onClick = { onItemUse("chance") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Medalhas
        if (medalhas.isNotEmpty()) {
            Text(
                "Suas Medalhas:",
                style = MaterialTheme.typography.titleMedium
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(medalhas) { medalha ->
                    MedalhaItem(medalha)
                }
            }
        }
    }
}

@Composable
fun TelaVitoria(
    pontuacao: Int,
    medalhas: List<String>,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Parabéns!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            "Você completou o quiz!",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Pontuação final: $pontuacao",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (medalhas.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Medalhas conquistadas:", style = MaterialTheme.typography.titleMedium)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                items(medalhas) { medalha ->
                    MedalhaItem(medalha)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onRestart,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Jogar Novamente")
            }

            OutlinedButton(
                onClick = onExit,
                modifier = Modifier.weight(1f)
            ) {
                Text("Sair")
            }
        }
    }
}

@Composable
fun TelaDerrota(
    pontuacao: Int,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color.Red,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Fim de Jogo",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )

        Text(
            "Você cometeu muitos erros",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Pontuação final: $pontuacao",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onRestart,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Tentar Novamente")
            }

            OutlinedButton(
                onClick = onExit,
                modifier = Modifier.weight(1f)
            ) {
                Text("Sair")
            }
        }
    }
}

// Componente principal
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizApp() {
    // 1. Estados e dados
    val perguntas = remember {
        listOf(
            Pergunta(
                id = 1,
                pergunta = "Qual é a maior floresta tropical do mundo?",
                respostas = listOf("Amazônica", "Congo", "Bornéu", "Daintree"),
                correta = 0,
                categoria = "Florestas",
                dificuldade = "Fácil"
            ),
            Pergunta(
                id = 2,
                pergunta = "Qual animal é conhecido como 'rei da selva'?",
                respostas = listOf("Tigre", "Leão", "Elefante", "Rinoceronte"),
                correta = 1,
                categoria = "Animais",
                dificuldade = "Fácil"
            ),
            Pergunta(
                id = 3,
                pergunta = "Qual destes não é um tipo de recife de coral?",
                respostas = listOf("Barreira", "Atol", "Franja", "Savana"),
                correta = 3,
                categoria = "Oceanos",
                dificuldade = "Médio"
            ),
            Pergunta(
                id = 4,
                pergunta = "Qual é o maior mamífero do mundo?",
                respostas = listOf("Elefante-africano", "Baleia-azul", "Girafa", "Hipopótamo"),
                correta = 1,
                categoria = "Animais",
                dificuldade = "Fácil"
            ),
            Pergunta(
                id = 5,
                pergunta = "Qual destas árvores é conhecida por viver milhares de anos?",
                respostas = listOf("Sequoia", "Eucalipto", "Ipê", "Jacarandá"),
                correta = 0,
                categoria = "Plantas",
                dificuldade = "Médio"
            ),
            Pergunta(
                id = 6,
                pergunta = "O que é fotossíntese?",
                respostas = listOf(
                    "Processo de reprodução das plantas",
                    "Processo de conversão de luz em energia",
                    "Forma de comunicação entre plantas",
                    "Método de absorção de água"
                ),
                correta = 1,
                categoria = "Biologia",
                dificuldade = "Médio"
            ),
            Pergunta(
                id = 7,
                pergunta = "Qual destes países tem a maior biodiversidade?",
                respostas = listOf("Brasil", "Estados Unidos", "Rússia", "China"),
                correta = 0,
                categoria = "Biodiversidade",
                dificuldade = "Difícil"
            ),
            Pergunta(
                id = 8,
                pergunta = "Qual é o principal gás responsável pelo efeito estufa?",
                respostas = listOf("Oxigênio", "Nitrogênio", "Dióxido de Carbono", "Hélio"),
                correta = 2,
                categoria = "Clima",
                dificuldade = "Médio"
            ),
            Pergunta(
                id = 9,
                pergunta = "Qual destes é um exemplo de energia renovável?",
                respostas = listOf("Petróleo", "Carvão", "Energia eólica", "Gás natural"),
                correta = 2,
                categoria = "Energia",
                dificuldade = "Fácil"
            ),
            Pergunta(
                id = 10,
                pergunta = "O que significa 'sustentabilidade'?",
                respostas = listOf(
                    "Extrair o máximo de recursos naturais",
                    "Atender necessidades atuais sem comprometer futuras gerações",
                    "Usar apenas produtos naturais",
                    "Viver sem tecnologia"
                ),
                correta = 1,
                categoria = "Ecologia",
                dificuldade = "Difícil"
            )
        )
    }

    val medalhasDisponiveis = remember {
        listOf(
            Medalha(
                nome = "🌱 Iniciante",
                icone = "🌱",
                descricao = "Acertou 5 perguntas",
                condicao = { pontuacao, _, perguntasRespondidas -> pontuacao >= 50 }
            ),
            Medalha(
                nome = "🥉 Bronze",
                icone = "🥉",
                descricao = "Pontuação de 100 pontos",
                condicao = { pontuacao, _, _ -> pontuacao >= 100 }
            ),
            Medalha(
                nome = "🥈 Prata",
                icone = "🥈",
                descricao = "Pontuação de 200 pontos",
                condicao = { pontuacao, _, _ -> pontuacao >= 200 }
            ),
            Medalha(
                nome = "🥇 Ouro",
                icone = "🥇",
                descricao = "Pontuação de 500 pontos",
                condicao = { pontuacao, _, _ -> pontuacao >= 500 }
            ),
            Medalha(
                nome = "💎 Diamante",
                icone = "💎",
                descricao = "Pontuação de 1000 pontos",
                condicao = { pontuacao, _, _ -> pontuacao >= 1000 }
            ),
            Medalha(
                nome = "🧠 Sábio",
                icone = "🧠",
                descricao = "Acertou 3 perguntas difíceis seguidas",
                condicao = { _, _, perguntasRespondidas ->
                    perguntasRespondidas.takeLast(3).all { it.dificuldade == "Difícil" } &&
                            perguntasRespondidas.size >= 3
                }
            ),
            Medalha(
                nome = "⏱️ Veloz",
                icone = "⏱️",
                descricao = "Responder 5 perguntas em menos de 5 segundos cada",
                condicao = { _, _, _ -> false }
            ),
            Medalha(
                nome = "🍃 Naturalista",
                icone = "🍃",
                descricao = "Acertou perguntas de todas as categorias",
                condicao = { _, _, perguntasRespondidas ->
                    val categorias = perguntasRespondidas.map { it.categoria }.distinct()
                    categorias.size >= 5
                }
            )
        )
    }

    var iniciou by remember { mutableStateOf(false) }
    var nivel by remember { mutableStateOf(1) }
    var perguntaAtual by remember { mutableStateOf(0) }
    var pontuacao by remember { mutableStateOf(0) }
    var medalhas by remember { mutableStateOf(emptyList<String>()) }
    var itens by remember { mutableStateOf(Itens(1, 1, 1)) }
    var erros by remember { mutableStateOf(0) }
    var tempoRestante by remember { mutableStateOf(15) }
    var perguntasRespondidas by remember { mutableStateOf(emptyList<Pergunta>()) }
    var jogoFinalizado by remember { mutableStateOf(false) }
    var vitoria by remember { mutableStateOf(false) }

    // 2. Funções auxiliares
    fun checkMedalhas() {
        val novasMedalhas = medalhasDisponiveis.filter { medalha ->
            medalha.condicao(pontuacao, erros, perguntasRespondidas) &&
                    !medalhas.contains(medalha.nome)
        }.map { it.nome }

        if (novasMedalhas.isNotEmpty()) {
            medalhas += novasMedalhas
        }
    }

    fun nextQuestion() {
        perguntaAtual = (perguntaAtual + 1) % perguntas.size
        if (perguntaAtual == 0) nivel++
        tempoRestante = 15
    }

    fun resetGame() {
        iniciou = false
        erros = 0
        pontuacao = 0
        perguntaAtual = 0
        nivel = 1
        perguntasRespondidas = emptyList()
        jogoFinalizado = false
        vitoria = false
    }

    fun handleResposta(index: Int) {
        val currentQuestion = perguntas[perguntaAtual]
        perguntasRespondidas += currentQuestion

        if (index == currentQuestion.correta) {
            val multiplicador = when(currentQuestion.dificuldade) {
                "Fácil" -> 1
                "Médio" -> 2
                else -> 3
            }
            pontuacao += 10 * nivel * multiplicador
            checkMedalhas()

            // Verifica se respondeu todas as perguntas
            if (perguntasRespondidas.size == perguntas.size) {
                jogoFinalizado = true
                vitoria = true
            }
        } else {
            erros++
            if (erros >= 3) {
                jogoFinalizado = true
                vitoria = false
            }
        }

        if (!jogoFinalizado) {
            nextQuestion()
        }
    }

    fun usarItem(tipo: String) {
        itens = when (tipo) {
            "pular" -> itens.copy(pularPergunta = itens.pularPergunta - 1).also { nextQuestion() }
            "tempo" -> itens.copy(tempoExtra = itens.tempoExtra - 1).also { tempoRestante += 10 }
            "chance" -> itens.copy(segundaChance = itens.segundaChance - 1)
            else -> itens
        }
    }

    // 3. Efeitos
    LaunchedEffect(key1 = perguntaAtual, key2 = iniciou) {
        if (iniciou && !jogoFinalizado) {
            tempoRestante = 15
            while (tempoRestante > 0) {
                delay(1000L)
                if (iniciou && !jogoFinalizado) tempoRestante--
            }
            if (iniciou && !jogoFinalizado) {
                erros++
                if (erros >= 3) {
                    jogoFinalizado = true
                    vitoria = false
                } else {
                    nextQuestion()
                }
            }
        }
    }

    // 4. UI Principal
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quiz da Natureza 🌿") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            AnimatedVisibility(
                visible = !iniciou && !jogoFinalizado,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TelaInicial(onStart = { iniciou = true })
            }

            AnimatedVisibility(
                visible = iniciou && !jogoFinalizado,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TelaQuiz(
                    perguntas[perguntaAtual],
                    nivel,
                    pontuacao,
                    erros,
                    medalhas,
                    itens,
                    tempoRestante,
                    onAnswer = ::handleResposta,
                    onItemUse = ::usarItem,
                    onReset = ::resetGame
                )
            }

            AnimatedVisibility(
                visible = jogoFinalizado && vitoria,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TelaVitoria(
                    pontuacao = pontuacao,
                    medalhas = medalhas,
                    onRestart = {
                        resetGame()
                        iniciou = true
                    },
                    onExit = { resetGame() }
                )
            }

            AnimatedVisibility(
                visible = jogoFinalizado && !vitoria,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TelaDerrota(
                    pontuacao = pontuacao,
                    onRestart = {
                        resetGame()
                        iniciou = true
                    },
                    onExit = { resetGame() }
                )
            }
        }
    }
}