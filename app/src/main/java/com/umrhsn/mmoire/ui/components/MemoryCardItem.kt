package com.umrhsn.mmoire.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.MemoryCard

@Composable
fun MemoryCardItem(
    memoryCard: MemoryCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (memoryCard.isFaceUp) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = stringResource(R.string.cardFlip_label)
    )

    Card(
        modifier = modifier
            .padding(dimensionResource(R.dimen.spacing_tiny))
            .aspectRatio(1f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.spacing_tiny)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Reverse the rotation for the content so it stays upright
                    if (rotation > 90f) rotationY = 180f
                }
        ) {
            val contentDescription = if (memoryCard.isFaceUp) {
                stringResource(R.string.face_up)
            } else {
                stringResource(R.string.face_down)
            }
            val alpha = if (memoryCard.isMatched) 0.4f else 1.0f

            // Show content only when appropriate in the rotation
            if (rotation > 90f) {
                if (memoryCard.imageUrl != null) {
                    AsyncImage(
                        model = memoryCard.imageUrl,
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = alpha,
                        placeholder = painterResource(R.drawable.image_loading)
                    )
                } else {
                    Image(
                        painter = painterResource(memoryCard.identifier),
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = alpha
                    )
                }
            } else {
                Image(
                    painter = painterResource(R.drawable.memory_card_facedown),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
