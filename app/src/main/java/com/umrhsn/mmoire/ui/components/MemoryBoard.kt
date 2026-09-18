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
import androidx.compose.ui.unit.dp
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.AppColorTheme
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
    isTwoPlayerMode: Boolean = false,
    appColorTheme: AppColorTheme = AppColorTheme.DEFAULT,
    isCardTintEnabled: Boolean = false,
    isHighVisibilityModeEnabled: Boolean = false
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.spacing_medium)),
        contentAlignment = Alignment.Center
    ) {
        val maxWidth = this.maxWidth
        val maxHeight = this.maxHeight

        // Dynamic Grid Engine: Find the (cols, rows) that results in the largest card size
        val gridDimens = remember(boardSize, maxWidth, maxHeight, isTwoPlayerMode) {
            val possibleGrids = boardSize.getPossibleGrids()
            var bestCols = 1
            var bestRows = boardSize.numCards
            var maxCardSize = 0.dp

            for (grid in possibleGrids) {
                val cols = grid.first
                val rows = grid.second

                // For 2-player mode split, we generally want more rows than columns
                // if we are splitting horizontally, or vice-versa. 
                // But maximizing card size is usually a good proxy for "fitting well".

                val cardWidth = maxWidth / cols
                val cardHeight = maxHeight / rows
                val size = if (cardWidth < cardHeight) cardWidth else cardHeight

                if (size > maxCardSize) {
                    maxCardSize = size
                    bestCols = cols
                    bestRows = rows
                } else if (size == maxCardSize) {
                    // Tie-breaker: Prefer layouts closer to the screen aspect ratio
                    val screenRatio = maxWidth / maxHeight
                    val currentGridRatio = bestCols.toFloat() / bestRows
                    val newGridRatio = cols.toFloat() / rows

                    if (Math.abs(newGridRatio - screenRatio) < Math.abs(currentGridRatio - screenRatio)) {
                        bestCols = cols
                        bestRows = rows
                    }
                }
            }
            bestCols to bestRows
        }

        val columns = gridDimens.first
        val rows = gridDimens.second

        val bestCardSize = remember(maxWidth, maxHeight, columns, rows) {
            val cardWidth = maxWidth / columns
            val cardHeight = maxHeight / rows
            val size = if (cardWidth < cardHeight) cardWidth else cardHeight
            // Cap card size to prevent oversized grids on large tablets
            if (size > 180.dp) 180.dp else size
        }

        // Entry animation control
        var isVisible by remember { mutableStateOf(false) }
        LaunchedEffect(boardSize) {
            isVisible = false
            delay(100.milliseconds)
            isVisible = true
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .width(bestCardSize * columns)
                .height(bestCardSize * rows),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Center,
            userScrollEnabled = false
        ) {
            itemsIndexed(
                items = cards,
                key = { index, _ -> "${boardSize.name}_$index" }
            ) { index, card ->

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
                        onClick = { onCardClicked(index) },
                        appColorTheme = appColorTheme,
                        isCardTintEnabled = isCardTintEnabled,
                        isHighVisibilityModeEnabled = isHighVisibilityModeEnabled
                    )
                }
            }
        }
    }
}
