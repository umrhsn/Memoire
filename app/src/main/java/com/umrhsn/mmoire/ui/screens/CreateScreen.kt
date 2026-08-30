package com.umrhsn.mmoire.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.ui.components.AppDialog
import com.umrhsn.mmoire.ui.components.AppHeader
import com.umrhsn.mmoire.ui.theme.MemoireTheme
import com.umrhsn.mmoire.utils.EXTRA_GAME_NAME
import com.umrhsn.mmoire.viewmodels.CreateViewModel
import kotlin.math.ceil

@Composable
fun CreateScreen(
    viewModel: CreateViewModel,
    boardSize: BoardSize,
    chosenImageUris: List<Uri>,
    onBackClicked: () -> Unit,
    onPlaceholderClicked: () -> Unit,
    onImageClicked: (Int) -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onSaveClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var gameName by remember { mutableStateOf("") }
    val numImagesRequired = boardSize.getNumPairs()
    val context = LocalContext.current

    MemoireTheme {
        Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header (Shared)
                AppHeader(
                    title = stringResource(R.string.new_board),
                    navigationIcon = {
                        IconButton(onClick = onBackClicked) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = dimensionResource(R.dimen.spacing_medium))
                ) {
                    // Step 1: Info with Icon
                    SectionHeader(
                        icon = Icons.Default.Collections,
                        title = stringResource(R.string.step_1_title),
                        subtitle = stringResource(R.string.step_1_subtitle, numImagesRequired)
                    )

                    // Selection Progress Bar
                    val selectionProgress = chosenImageUris.size.toFloat() / numImagesRequired
                    LinearProgressIndicator(
                        progress = { selectionProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = dimensionResource(R.dimen.spacing_small))
                            .height(dimensionResource(R.dimen.spacing_small)),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        color = if (selectionProgress >= 1f) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    // Grid Area
                    Box(modifier = Modifier.weight(1f)) {
                        if (chosenImageUris.isEmpty()) {
                            EmptySelectionState(onPlaceholderClicked)
                        } else {
                            DynamicImageGrid(
                                chosenImageUris = chosenImageUris,
                                numImagesRequired = numImagesRequired,
                                onPlaceholderClicked = onPlaceholderClicked,
                                onImageClicked = onImageClicked,
                                onRemoveImage = onRemoveImage
                            )
                        }
                    }

                    // Step 2: Naming
                    SectionHeader(
                        icon = Icons.Default.DriveFileRenameOutline,
                        title = stringResource(R.string.step_2_title),
                        subtitle = stringResource(R.string.step_2_subtitle)
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                }
                
                // Bottom Control Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(
                        topStart = dimensionResource(R.dimen.spacing_extra_large),
                        topEnd = dimensionResource(R.dimen.spacing_extra_large)
                    ),
                    shadowElevation = dimensionResource(R.dimen.spacing_large)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(dimensionResource(R.dimen.spacing_large))
                            .navigationBarsPadding()
                    ) {
                        OutlinedTextField(
                            value = gameName,
                            onValueChange = { input ->
                                if (input.length <= 14 && input.all { it.isLetterOrDigit() || it == '_' }) {
                                    gameName = input
                                }
                            },
                            label = { Text(stringResource(R.string.board_identity_label)) },
                            placeholder = { Text(stringResource(R.string.board_id_placeholder)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isUploading,
                            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large)),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) }
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                        Button(
                            onClick = { onSaveClicked(gameName) },
                            modifier = Modifier
                                .height(dimensionResource(R.dimen.button_height_large))
                                .fillMaxWidth(),
                            enabled = chosenImageUris.size == numImagesRequired && 
                                      gameName.isNotBlank() && 
                                      gameName.length >= 3 && 
                                      !uiState.isUploading,
                            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large)),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = dimensionResource(R.dimen.spacing_small))
                        ) {
                            if (uiState.isUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(dimensionResource(R.dimen.spacing_large)),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.radius_medium)))
                                Text(stringResource(R.string.saving_board), fontWeight = FontWeight.Black)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.radius_medium)))
                                Text(
                                    text = stringResource(R.string.create_and_play),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        if (uiState.isUploading) {
                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                            LinearProgressIndicator(
                                progress = { uiState.uploadProgress / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }

            // Success Dialog
            if (uiState.isSuccess) {
                AppDialog(
                    onDismissRequest = {}, 
                    title = stringResource(R.string.board_ready_title),
                    icon = Icons.Default.CheckCircle
                ) {
                    Text(
                        text = stringResource(R.string.board_ready_message, uiState.gameName ?: ""),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
                    Button(
                        onClick = {
                            val activity = context as? Activity
                            val resultData = Intent()
                            resultData.putExtra(EXTRA_GAME_NAME, uiState.gameName)
                            activity?.setResult(Activity.RESULT_OK, resultData)
                            activity?.finish()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium))
                    ) {
                        Text(stringResource(R.string.start_game), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Name Taken Dialog
            if (uiState.nameTaken) {
                AppDialog(
                    onDismissRequest = { viewModel.resetState() },
                    title = stringResource(R.string.name_exists_title),
                    icon = Icons.Default.Warning
                ) {
                    Text(
                        text = stringResource(R.string.name_exists_message, uiState.gameName ?: ""),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
                    Button(
                        onClick = { viewModel.resetState() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium))
                    ) {
                        Text(stringResource(R.string.change_name_action))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.padding(
            top = dimensionResource(R.dimen.spacing_large),
            bottom = dimensionResource(R.dimen.radius_medium)
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(dimensionResource(R.dimen.spacing_huge).value.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(dimensionResource(R.dimen.spacing_large).value.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptySelectionState(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.spacing_medium)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.spacing_extra_large)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size_huge)),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                
                Text(
                    text = stringResource(R.string.no_photos_yet),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = stringResource(R.string.no_photos_yet_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing_small))
                )
                
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_extra_large)))
                
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large)),
                    contentPadding = PaddingValues(
                        horizontal = dimensionResource(R.dimen.spacing_large),
                        vertical = dimensionResource(R.dimen.spacing_medium)
                    )
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.radius_medium)))
                    Text(stringResource(R.string.open_gallery), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun DynamicImageGrid(
    chosenImageUris: List<Uri>,
    numImagesRequired: Int,
    onPlaceholderClicked: () -> Unit,
    onImageClicked: (Int) -> Unit,
    onRemoveImage: (Uri) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val totalSlots = chosenImageUris.size + (if (chosenImageUris.size < numImagesRequired) 1 else 0)
        val spacing = dimensionResource(R.dimen.spacing_small)
        
        var bestCols = 1
        var bestCardSizeValue = 0f

        for (cols in 1..totalSlots) {
            val rows = ceil(totalSlots.toFloat() / cols).toInt()
            
            val availableWidth = maxWidth - (spacing * (cols - 1))
            val availableHeight = maxHeight - (spacing * (rows - 1))
            
            val cw = availableWidth.value / cols
            val ch = availableHeight.value / rows
            val s = if (cw < ch) cw else ch
            
            if (s > bestCardSizeValue) {
                bestCardSizeValue = s
                bestCols = cols
            }
        }

        val rows = ceil(totalSlots.toFloat() / bestCols).toInt()
        val bestCardSize = bestCardSizeValue.dp

        Column(
            modifier = Modifier.wrapContentSize(),
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (r in 0 until rows) {
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (c in 0 until bestCols) {
                        val index = r * bestCols + c
                        if (index < totalSlots) {
                            Box(modifier = Modifier.size(bestCardSize)) {
                                if (index < chosenImageUris.size) {
                                    ImageItem(
                                        uri = chosenImageUris[index],
                                        onClick = { onImageClicked(index) },
                                        onRemove = { onRemoveImage(chosenImageUris[index]) }
                                    )
                                } else {
                                    PlaceholderItem(onClick = onPlaceholderClicked)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.width(bestCardSize))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageItem(
    uri: Uri,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.spacing_tiny))
    ) {
        Box {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Delete Icon Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(dimensionResource(R.dimen.spacing_tiny))
                    .size(dimensionResource(R.dimen.badge_size))
                    .clickable { onRemove() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                contentColor = Color.White,
                shadowElevation = dimensionResource(R.dimen.spacing_tiny)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.remove_desc),
                        modifier = Modifier.size(dimensionResource(R.dimen.spacing_medium))
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderItem(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = stringResource(R.string.add_image_desc),
                modifier = Modifier.size(dimensionResource(R.dimen.spacing_extra_large)),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
