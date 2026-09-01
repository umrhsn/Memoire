package com.umrhsn.mmoire.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.res.stringResource
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
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.spacing_medium)),
        contentAlignment = Alignment.Center
    ) {
        val maxWidth = this.maxWidth
        val maxHeight = this.maxHeight
        val numCards = cards.size

        // We use the dimensions defined in the BoardSize model for a guaranteed clean grid
        val columns = boardSize.getWidth()
        val rows = boardSize.getHeight()

        // Calculate card size based on these fixed dimensions to fill the space
        val cardWidth = maxWidth / columns
        val cardHeight = maxHeight / rows
        val bestCardSize = if (cardWidth < cardHeight) cardWidth else cardHeight

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
                    for (c in 0 until columns) {
                        val index = r * columns + c
                        if (index < numCards) {
                            val card = cards[index]

                            val scale by animateFloatAsState(
                                targetValue = if (isVisible) 1f else 0f,
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = index * 20
                                ),
                                label = stringResource(R.string.cardEntryScale_label)
                            )

                            val alpha by animateFloatAsState(
                                targetValue = if (isVisible) 1f else 0f,
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = index * 20
                                ),
                                label = stringResource(R.string.cardEntryAlpha_label)
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
                        }
                    }
                }
            }
        }
    }
}
