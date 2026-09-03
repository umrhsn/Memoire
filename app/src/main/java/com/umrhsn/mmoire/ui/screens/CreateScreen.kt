package com.umrhsn.mmoire.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.ui.components.AppDialog
import com.umrhsn.mmoire.ui.components.AppHeader
import com.umrhsn.mmoire.ui.components.AppHeaderIcon
import com.umrhsn.mmoire.ui.components.getAppTextFieldColors
import com.umrhsn.mmoire.ui.theme.MemoireTheme
import com.umrhsn.mmoire.utils.EXTRA_GAME_NAME
import com.umrhsn.mmoire.viewmodels.CreateViewModel
import compose.icons.EvaIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.AlertTriangle
import compose.icons.evaicons.outline.ArrowBack
import compose.icons.evaicons.outline.Award
import compose.icons.evaicons.outline.Close
import compose.icons.evaicons.outline.CloudUpload
import compose.icons.evaicons.outline.Edit
import compose.icons.evaicons.outline.Flash
import compose.icons.evaicons.outline.Image
import compose.icons.evaicons.outline.Plus
import compose.icons.evaicons.outline.Save

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    viewModel: CreateViewModel,
    boardSize: BoardSize,
    chosenImageUris: List<Uri>,
    modifier: Modifier = Modifier,
    oldName: String? = null,
    onBackClicked: () -> Unit,
    onPlaceholderClicked: () -> Unit,
    onImageClicked: (Int) -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onSaveClicked: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var gameName by remember { mutableStateOf(oldName ?: "") }
    val numImagesRequired = boardSize.getNumPairs()
    val context = LocalContext.current

    MemoireTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                AppHeader(
                    title = if (oldName != null) stringResource(R.string.edit_board) else stringResource(
                        R.string.new_board
                    ),
                    navigationIcon = {
                        AppHeaderIcon(
                            icon = EvaIcons.Outline.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            onClick = onBackClicked
                        )
                    }
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = dimensionResource(R.dimen.spacing_medium))
                ) {
                    // Step 1
                    SectionHeader(
                        icon = EvaIcons.Outline.Image,
                        title = stringResource(R.string.step_1_title),
                        subtitle = pluralStringResource(
                            R.plurals.step_1_subtitle_plural,
                            numImagesRequired,
                            numImagesRequired
                        )
                    )

                    // Selection Progress Bar
                    val selectionProgress = chosenImageUris.size.toFloat() / numImagesRequired
                    LinearProgressIndicator(
                        progress = { selectionProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = dimensionResource(R.dimen.spacing_small))
                            .height(10.dp)
                            .clip(CircleShape),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        color = if (selectionProgress >= 1f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    // Grid Area - Fixed Grid for creation
                    Box(modifier = Modifier.weight(1f)) {
                        if (chosenImageUris.isEmpty() && !uiState.isLoading) {
                            EmptySelectionState(
                                onClick = onPlaceholderClicked,
                                onSoundClick = { viewModel.playButtonClick() }
                            )
                        } else if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(chosenImageUris) { index, uri ->
                                    ImageItem(
                                        uri = uri,
                                        onClick = { onImageClicked(index) },
                                        onRemove = { onRemoveImage(uri) }
                                    )
                                }
                                if (chosenImageUris.size < numImagesRequired) {
                                    item {
                                        PlaceholderItem(onClick = onPlaceholderClicked)
                                    }
                                }
                            }
                        }
                    }

                    // Step 2
                    SectionHeader(
                        icon = EvaIcons.Outline.Edit,
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
                    shadowElevation = 32.dp,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(dimensionResource(R.dimen.spacing_large))
                            .navigationBarsPadding()
                    ) {
                        OutlinedTextField(
                            value = gameName,
                            onValueChange = { input ->
                                if (input.length <= 24) { // Increased from 14
                                    gameName = input
                                }
                            },
                            label = { Text(stringResource(R.string.board_identity_label)) },
                            placeholder = { Text(stringResource(R.string.board_id_placeholder)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isUploading,
                            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large)),
                            colors = getAppTextFieldColors(),
                            leadingIcon = {
                                Icon(
                                    EvaIcons.Outline.Flash,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                autoCorrectEnabled = true,
                                keyboardType = KeyboardType.Text
                            )
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip {
                                    Text(
                                        if (chosenImageUris.size < numImagesRequired)
                                            stringResource(R.string.select_all_photos)
                                        else stringResource(R.string.finalize_board)
                                    )
                                }
                            },
                            state = rememberTooltipState()
                        ) {
                            Button(
                                onClick = { onSaveClicked(gameName.trim()) }, // Added trim()
                                modifier = Modifier
                                    .height(dimensionResource(R.dimen.button_height_large))
                                    .fillMaxWidth(),
                                enabled = chosenImageUris.size == numImagesRequired &&
                                        gameName.isNotBlank() &&
                                        gameName.length >= 3 &&
                                        !uiState.isUploading,
                                shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large)),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                            ) {
                                if (uiState.isUploading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(dimensionResource(R.dimen.spacing_large)),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.radius_medium)))
                                    Text(
                                        stringResource(R.string.saving_board),
                                        fontWeight = FontWeight.Black
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (oldName != null) EvaIcons.Outline.Save else EvaIcons.Outline.CloudUpload,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.radius_medium)))
                                    Text(
                                        text = if (oldName != null) stringResource(R.string.update_and_play) else stringResource(
                                            R.string.create_and_play
                                        ),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        if (uiState.isUploading) {
                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                            LinearProgressIndicator(
                                progress = { uiState.uploadProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CircleShape),
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
                    icon = EvaIcons.Outline.Award
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
                    icon = EvaIcons.Outline.AlertTriangle
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
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptySelectionState(onClick: () -> Unit, onSoundClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.spacing_medium)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = EvaIcons.Outline.Image,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

                Text(
                    text = stringResource(R.string.no_photos_yet),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(R.string.no_photos_yet_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        onSoundClick()
                        onClick()
                    },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(
                        horizontal = 32.dp,
                        vertical = 16.dp
                    )
                ) {
                    Icon(EvaIcons.Outline.Plus, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.open_gallery), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageItem(
    uri: Uri,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Delete Icon Badge
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text(stringResource(R.string.remove))
                    }
                },
                state = rememberTooltipState()
            ) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clickable { onRemove() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                    contentColor = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = EvaIcons.Outline.Close,
                            contentDescription = stringResource(R.string.remove_desc),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderItem(onClick: () -> Unit) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(stringResource(R.string.add_image_desc))
            }
        },
        state = rememberTooltipState()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable { onClick() },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = 0.2f
                )
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = EvaIcons.Outline.Plus,
                    contentDescription = stringResource(R.string.add_image_desc),
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
