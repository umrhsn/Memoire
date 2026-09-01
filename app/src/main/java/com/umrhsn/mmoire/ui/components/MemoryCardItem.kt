package com.umrhsn.mmoire.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth scale effect on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardPressScale"
    )

    // Tactile spring rotation
    val rotation by animateFloatAsState(
        targetValue = if (memoryCard.isFaceUp) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = stringResource(R.string.cardFlip_label)
    )

    Card(
        modifier = modifier
            .padding(dimensionResource(R.dimen.spacing_tiny))
            .aspectRatio(1f)
            .scale(scale)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 15f * density
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom visual feedback via scale
                onClick = onClick
            ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(R.dimen.spacing_tiny),
            pressedElevation = 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (rotation > 90f) rotationY = 180f
                }
        ) {
            val contentDescription = if (memoryCard.isFaceUp) {
                stringResource(R.string.face_up)
            } else {
                stringResource(R.string.face_down)
            }

            // Subtle matched alpha transition
            val matchedAlpha by animateFloatAsState(
                targetValue = if (memoryCard.isMatched) 0.5f else 1.0f,
                animationSpec = tween(500),
                label = "matchedAlpha"
            )

            if (rotation > 90f) {
                if (memoryCard.imageUrl != null) {
                    AsyncImage(
                        model = memoryCard.imageUrl,
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = matchedAlpha,
                        placeholder = painterResource(R.drawable.image_loading)
                    )
                } else {
                    Image(
                        painter = painterResource(memoryCard.identifier),
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = matchedAlpha
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
