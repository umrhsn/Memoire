package com.umrhsn.mmoire.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.umrhsn.mmoire.R

@Composable
fun AppHeader(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Surface(
        modifier = modifier.fillMaxWidth(),
        // Pitch black header in dark mode for immersive feel
        color = if (isDark) Color.Black else MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        shape = RoundedCornerShape(
            bottomStart = dimensionResource(R.dimen.radius_extra_large),
            bottomEnd = dimensionResource(R.dimen.radius_extra_large)
        ),
        shadowElevation = dimensionResource(R.dimen.spacing_small)
    ) {
        Row(
            modifier = Modifier.padding(
                start = dimensionResource(R.dimen.spacing_medium),
                top = dimensionResource(R.dimen.spacing_huge),
                end = dimensionResource(R.dimen.spacing_large),
                bottom = dimensionResource(R.dimen.spacing_large)
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigationIcon != null) {
                navigationIcon()
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

@Composable
fun StatBadge(
    icon: ImageVector,
    value: String,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    // Improved contrast using primary container colors
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.spacing_medium),
                vertical = dimensionResource(R.dimen.spacing_small)
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.stat_icon_size))
            )
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = with(density) { dimensionResource(R.dimen.text_small).toSp() }
            )
        }
    }
}

@Composable
fun NumericBadge(
    number: Int,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    Surface(
        modifier = modifier.size(dimensionResource(R.dimen.badge_size)),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = dimensionResource(R.dimen.spacing_tiny)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = number.toString(),
                fontSize = with(density) { dimensionResource(R.dimen.text_tiny).toSp() },
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun AppDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    properties: DialogProperties = DialogProperties(),
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_huge)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = dimensionResource(R.dimen.spacing_small),
            shadowElevation = dimensionResource(R.dimen.spacing_large)
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.spacing_extra_large)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (icon != null) {
                    Surface(
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_giant)),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_large))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
                }
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_medium))
                )
                
                content()
            }
        }
    }
}
