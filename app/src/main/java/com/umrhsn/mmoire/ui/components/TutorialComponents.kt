package com.umrhsn.mmoire.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

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
    onSkip: () -> Unit
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]
    val targetRect = currentStep.anchorKey?.let { anchors[it] }

    Dialog(
        onDismissRequest = onSkip,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Background with Spotlight
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = false) { }
            ) {
                val spotlightPath = Path().apply {
                    if (targetRect != null) {
                        // Create a rounded spotlight around the target
                        val padding = 8.dp.toPx()
                        addRoundRect(
                            androidx.compose.ui.geometry.RoundRect(
                                rect = Rect(
                                    left = targetRect.left - padding,
                                    top = targetRect.top - padding,
                                    right = targetRect.right + padding,
                                    bottom = targetRect.bottom + padding
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                            )
                        )
                    }
                }

                clipPath(spotlightPath, clipOp = androidx.compose.ui.graphics.ClipOp.Difference) {
                    drawRect(Color.Black.copy(alpha = 0.8f))
                }
            }

            // Information Card
            val cardAlignment =
                if (targetRect != null && targetRect.top < 400f) Alignment.BottomCenter else Alignment.Center

            Column(
                modifier = Modifier
                    .align(cardAlignment)
                    .padding(32.dp)
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 24.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Icon Circle
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(currentStep.icon, null, modifier = Modifier.size(32.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = currentStep.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currentStep.description,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onSkip) {
                                Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                Text(if (currentStepIndex < steps.size - 1) "Next" else "Let's Go!")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    steps.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentStepIndex) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentStepIndex) MaterialTheme.colorScheme.primary
                                    else Color.White.copy(alpha = 0.5f)
                                )
                        )
                    }
                }
            }
        }
    }
}
