package com.umrhsn.mmoire.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.models.MemoryCard
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MemoryBoard(
    boardSize: BoardSize,
    cards: List<MemoryCard>,
    onCardClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val scope = this
        val maxWidth = scope.maxWidth
        val maxHeight = scope.maxHeight
        val numCards = cards.size

        // Dynamic Grid Sizing:
        // We want to fit all cards in the available space without scrolling.
        var bestCols = 1
        var bestCardSize = 0.dp

        for (cols in 1..numCards) {
            val rows = ceil(numCards.toFloat() / cols).toInt()
            val cardWidth = maxWidth / cols
            val cardHeight = maxHeight / rows
            val currentSize = if (cardWidth < cardHeight) cardWidth else cardHeight
            
            if (currentSize > bestCardSize) {
                bestCardSize = currentSize
                bestCols = cols
            }
        }

        val rows = ceil(numCards.toFloat() / bestCols).toInt()
        
        // One-time entry animation flag
        var isVisible by remember { mutableStateOf(false) }
        LaunchedEffect(boardSize, cards.size) {
            isVisible = false
            delay(50.milliseconds)
            isVisible = true
        }

        Column(
            modifier = Modifier.wrapContentSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (r in 0 until rows) {
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (c in 0 until bestCols) {
                        val index = r * bestCols + c
                        if (index < numCards) {
                            val card = cards[index]
                            
                            val scale by animateFloatAsState(
                                targetValue = if (isVisible) 1f else 0f,
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = index * 20
                                ),
                                label = "cardEntryScale"
                            )
                            
                            val alpha by animateFloatAsState(
                                targetValue = if (isVisible) 1f else 0f,
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = index * 20
                                ),
                                label = "cardEntryAlpha"
                            )

                            Box(
                                modifier = Modifier
                                    .size(bestCardSize)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        this.alpha = alpha
                                    }
                            ) {
                                MemoryCardItem(
                                    memoryCard = card,
                                    onClick = { onCardClicked(index) }
                                )
                            }
                        } else {
                            Box(modifier = Modifier.size(bestCardSize))
                        }
                    }
                }
            }
        }
    }
}
