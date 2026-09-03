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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.MemoryCard

private const val OPTIMIZED_IMAGE_SIZE = 250

@Composable
fun MemoryCardItem(
    memoryCard: MemoryCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 1. Static scale for the card itself - simple and efficient
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cardPressScale"
    )

    // 2. Rotation state - determines if we show front or back
    val rotation by animateFloatAsState(
        targetValue = if (memoryCard.isFaceUp) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = stringResource(R.string.cardFlip_label)
    )

    // 3. Matched alpha - moved to graphicsLayer to prevent image recomposition
    val matchedAlpha by animateFloatAsState(
        targetValue = if (memoryCard.isMatched) 0.4f else 1.0f,
        animationSpec = tween(400),
        label = "matchedAlpha"
    )

    // Performance Fix: Remember the ImageRequest to avoid rebuilding it on every recomposition
    val imageRequest = remember(memoryCard.imageUrl) {
        if (memoryCard.imageUrl != null) {
            ImageRequest.Builder(context)
                .data(memoryCard.imageUrl)
                .crossfade(true)
                .size(OPTIMIZED_IMAGE_SIZE, OPTIMIZED_IMAGE_SIZE) // Consistent size
                .diskCacheKey(memoryCard.imageUrl) // Efficient caching
                .memoryCacheKey(memoryCard.imageUrl)
                .build()
        } else null
    }

    Card(
        modifier = modifier
            .padding(dimensionResource(R.dimen.spacing_tiny))
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
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
                    // Only flip content if we've passed the midpoint
                    if (rotation > 90f) rotationY = 180f
                    alpha = matchedAlpha
                }
        ) {
            val contentDescription = if (memoryCard.isFaceUp) {
                stringResource(R.string.face_up)
            } else {
                stringResource(R.string.face_down)
            }

            if (rotation > 90f) {
                if (imageRequest != null) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.image_loading)
                    )
                } else {
                    Image(
                        painter = painterResource(memoryCard.identifier),
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
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
