package com.umrhsn.mmoire.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.dimensionResource
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.models.MemoryCard
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MemoryBoard(
    boardSize: BoardSize,
    cards: List<MemoryCard>,
    onCardClicked: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isTwoPlayerMode: Boolean = false
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.spacing_medium)),
        contentAlignment = Alignment.Center
    ) {
        val maxWidth = this.maxWidth
        val maxHeight = this.maxHeight

        // Use pre-defined dimensions for consistency
        val columns = remember(boardSize, isTwoPlayerMode) { boardSize.getWidth(isTwoPlayerMode) }
        val rows = remember(boardSize, isTwoPlayerMode) { boardSize.getHeight(isTwoPlayerMode) }

        // Calculate card size once per size change
        val bestCardSize = remember(maxWidth, maxHeight, columns, rows) {
            val cardWidth = maxWidth / columns
            val cardHeight = maxHeight / rows
            if (cardWidth < cardHeight) cardWidth else cardHeight
        }

        // Entry animation control
        var isVisible by remember { mutableStateOf(false) }
        LaunchedEffect(boardSize) {
            isVisible = false
            delay(100.milliseconds)
            isVisible = true
        }

        // 1. Performance Fix: Using LazyVerticalGrid for efficient layout management
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .width(bestCardSize * columns)
                .height(bestCardSize * rows),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Center,
            userScrollEnabled = false // Keep it static as a memory board
        ) {
            itemsIndexed(
                items = cards,
                // 2. Performance Fix: Providing stable keys prevents unnecessary item replacement
                key = { index, _ -> "${boardSize.name}_$index" }
            ) { index, card ->

                // Entry animation state
                val entryScale by animateFloatAsState(
                    targetValue = if (isVisible) 1f else 0f,
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = index * 15
                    ),
                    label = "cardEntryScale"
                )

                val entryAlpha by animateFloatAsState(
                    targetValue = if (isVisible) 1f else 0f,
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = index * 15
                    ),
                    label = "cardEntryAlpha"
                )

                Box(
                    modifier = Modifier
                        .size(bestCardSize)
                        .graphicsLayer {
                            scaleX = entryScale
                            scaleY = entryScale
                            alpha = entryAlpha
                        }
                ) {
                    MemoryCardItem(
                        memoryCard = card,
                        onClick = { onCardClicked(index) }
                    )
                }
            }
        }
    }
}
