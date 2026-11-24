package com.luiyigames.dogescape

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFF8EB)
                ) {
                    DogEscapeEasyApp()
                }
            }
        }
    }
}

data class GridPos(val row: Int, val col: Int)

@Composable
fun DogEscapeEasyApp() {
    var selectedLevelIndex by remember { mutableStateOf<Int?>(null) }

    val currentIndex = selectedLevelIndex
    if (currentIndex == null) {
        LevelSelectScreen(
            onSelectLevel = { idx -> selectedLevelIndex = idx }
        )
    } else {
        val level = EasyLevels.levels[currentIndex]
        GameBoardScreen(
            level = level,
            hasNextLevel = currentIndex + 1 < EasyLevels.levels.size,
            onBackToMenu = { selectedLevelIndex = null },
            onNextLevel = {
                if (currentIndex + 1 < EasyLevels.levels.size) {
                    selectedLevelIndex = currentIndex + 1
                } else {
                    selectedLevelIndex = null
                }
            }
        )
    }
}

@Composable
fun LevelSelectScreen(onSelectLevel: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🐶 Perrito Escape Fácil",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5D3A1A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Elige un nivel para empezar",
            fontSize = 18.sp,
            color = Color(0xFF7A5533)
        )
        Spacer(modifier = Modifier.height(28.dp))

        EasyLevels.levels.forEachIndexed { index, level ->
            Button(
                onClick = { onSelectLevel(index) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA726),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Nivel ${level.number}: ${level.title}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun GameBoardScreen(
    level: EasyLevel,
    hasNextLevel: Boolean,
    onBackToMenu: () -> Unit,
    onNextLevel: () -> Unit
) {
    val startPos = remember(level) {
        var found = GridPos(1, 1)
        level.grid.forEachIndexed { r, row ->
            row.forEachIndexed { c, ch ->
                if (ch == 'P') found = GridPos(r, c)
            }
        }
        found
    }

    var dogPos by remember(level) { mutableStateOf(startPos) }
    var wonLevel by remember(level) { mutableStateOf(false) }
    var dragAccumX by remember { mutableFloatStateOf(0f) }
    var dragAccumY by remember { mutableFloatStateOf(0f) }

    fun moveDog(dRow: Int, dCol: Int) {
        if (wonLevel) return
        val nextRow = dogPos.row + dRow
        val nextCol = dogPos.col + dCol
        if (nextRow !in level.grid.indices) return
        val rowStr = level.grid[nextRow]
        if (nextCol !in rowStr.indices) return

        val targetCell = rowStr[nextCol]
        if (targetCell != '#') {
            val newPos = GridPos(nextRow, nextCol)
            dogPos = newPos
            if (targetCell == 'G') {
                wonLevel = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBackToMenu) {
                Text("Niveles")
            }
            Text(
                text = "Nivel ${level.number}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D3A1A)
            )
            OutlinedButton(
                onClick = {
                    dogPos = startPos
                    wonLevel = false
                }
            ) {
                Text("Reiniciar")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val rows = level.grid.size
        val cols = level.grid.first().length

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(level, wonLevel) {
                    detectDragGestures(
                        onDragStart = {
                            dragAccumX = 0f
                            dragAccumY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumX += dragAmount.x
                            dragAccumY += dragAmount.y
                            val threshold = 48f
                            if (abs(dragAccumX) > abs(dragAccumY)) {
                                if (dragAccumX > threshold) {
                                    moveDog(0, 1)
                                    dragAccumX = 0f
                                    dragAccumY = 0f
                                } else if (dragAccumX < -threshold) {
                                    moveDog(0, -1)
                                    dragAccumX = 0f
                                    dragAccumY = 0f
                                }
                            } else {
                                if (dragAccumY > threshold) {
                                    moveDog(1, 0)
                                    dragAccumX = 0f
                                    dragAccumY = 0f
                                } else if (dragAccumY < -threshold) {
                                    moveDog(-1, 0)
                                    dragAccumX = 0f
                                    dragAccumY = 0f
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(cols.toFloat() / rows.toFloat())
            ) {
                val cellW = size.width / cols
                val cellH = size.height / rows
                val pad = minOf(cellW, cellH) * 0.08f

                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val x = c * cellW
                        val y = r * cellH
                        val ch = level.grid[r][c]

                        val tileColor = if (ch == '#') {
                            Color(0xFF8D6E63)
                        } else {
                            Color(0xFFFFF3D6)
                        }

                        drawRoundRect(
                            color = tileColor,
                            topLeft = Offset(x + pad / 2f, y + pad / 2f),
                            size = Size(cellW - pad, cellH - pad),
                            cornerRadius = CornerRadius(12f, 12f)
                        )

                        if (ch == 'G') {
                            drawRoundRect(
                                color = Color(0xFF66BB6A),
                                topLeft = Offset(x + pad * 1.5f, y + pad * 1.5f),
                                size = Size(cellW - pad * 3f, cellH - pad * 3f),
                                cornerRadius = CornerRadius(16f, 16f)
                            )
                        }
                    }
                }

                val dogX = dogPos.col * cellW + cellW / 2f
                val dogY = dogPos.row * cellH + cellH / 2f
                val radius = minOf(cellW, cellH) * 0.34f

                drawCircle(
                    color = Color(0xFFFF7043),
                    radius = radius,
                    center = Offset(dogX, dogY)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = { moveDog(-1, 0) },
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("▲", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { moveDog(0, -1) },
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("◀", fontSize = 22.sp)
                }
                Button(
                    onClick = { moveDog(1, 0) },
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("▼", fontSize = 22.sp)
                }
                Button(
                    onClick = { moveDog(0, 1) },
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("▶", fontSize = 22.sp)
                }
            }
        }

        if (wonLevel) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("¡Lo lograste!") },
                text = { Text("El perrito llegó a su meta en el Nivel ${level.number}.") },
                confirmButton = {
                    Button(onClick = onNextLevel) {
                        Text(if (hasNextLevel) "Siguiente nivel" else "Volver al menú")
                    }
                }
            )
        }
    }
}
