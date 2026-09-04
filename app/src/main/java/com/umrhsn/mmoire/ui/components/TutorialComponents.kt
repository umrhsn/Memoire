package com.umrhsn.mmoire.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.umrhsn.mmoire.R

data class TutorialStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val anchorKey: String? = null
)

@Composable
fun TutorialOverlay(
    steps: List<TutorialStep>,
    anchors: Map<String, Rect>,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onStepChanged: (Int) -> Unit = {}
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentStepIndex) {
        onStepChanged(currentStepIndex)
    }

    val currentStep = steps[currentStepIndex]
    val rawTargetRect = currentStep.anchorKey?.let { anchors[it] }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    var popupScreenPos by remember { mutableStateOf(IntOffset.Zero) }

    Popup(
        offset = IntOffset.Zero,
        properties = PopupProperties(
            focusable = true,
            excludeFromSystemGesture = true,
            clippingEnabled = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    val pos = coordinates.positionOnScreen()
                    popupScreenPos = IntOffset(pos.x.toInt(), pos.y.toInt())
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Block touches */ }
        ) {
            val targetRect = rawTargetRect?.let {
                it.translate(-popupScreenPos.x.toFloat(), -popupScreenPos.y.toFloat())
            }

            // Spotlight Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val spotlightPath = Path().apply {
                    targetRect?.let { rect ->
                        val padding = 8.dp.toPx()
                        addRoundRect(
                            RoundRect(
                                rect = Rect(
                                    left = rect.left - padding,
                                    top = rect.top - padding,
                                    right = rect.right + padding,
                                    bottom = rect.bottom + padding
                                ),
                                cornerRadius = CornerRadius(16.dp.toPx())
                            )
                        )
                    }
                }

                clipPath(spotlightPath, clipOp = ClipOp.Difference) {
                    drawRect(Color.Black.copy(alpha = 0.85f))
                }
            }

            // Information Card
            val cardAlignment = when {
                targetRect == null -> Alignment.Center
                targetRect.top < screenHeightPx / 2 -> Alignment.BottomCenter
                else -> Alignment.TopCenter
            }

            Column(
                modifier = Modifier
                    .align(cardAlignment)
                    .padding(horizontal = 32.dp, vertical = 80.dp)
                    .widthIn(max = 400.dp)
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 24.dp
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Hero Icon
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(currentStep.icon, null, modifier = Modifier.size(32.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = currentStep.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currentStep.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onSkip) {
                                Text(
                                    stringResource(R.string.skip),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    if (currentStepIndex < steps.size - 1) {
                                        currentStepIndex++
                                    } else {
                                        onComplete()
                                    }
                                },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                val btnText = if (currentStepIndex < steps.size - 1)
                                    stringResource(R.string.next)
                                else stringResource(R.string.finish)
                                Text(btnText)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Indicators
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    steps.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentStepIndex) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentStepIndex) MaterialTheme.colorScheme.primary
                                    else Color.White.copy(alpha = 0.4f)
                                )
                        )
                    }
                }
            }
        }
    }
}
