package com.umrhsn.mmoire.ui.screens

import android.text.format.DateUtils
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.repository.UserImageListWithId
import com.umrhsn.mmoire.ui.components.AppDialog
import com.umrhsn.mmoire.ui.components.AppHeader
import com.umrhsn.mmoire.ui.components.AppHeaderIcon
import com.umrhsn.mmoire.ui.components.AppTooltipIconButton
import com.umrhsn.mmoire.ui.components.getAppTextFieldColors
import com.umrhsn.mmoire.ui.theme.MemoireTheme
import com.umrhsn.mmoire.viewmodels.BrowseViewModel
import com.umrhsn.mmoire.viewmodels.SortOrder
import compose.icons.EvaIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.ArrowBack
import compose.icons.evaicons.outline.Calendar
import compose.icons.evaicons.outline.Checkmark
import compose.icons.evaicons.outline.Clock
import compose.icons.evaicons.outline.Edit
import compose.icons.evaicons.outline.Eye
import compose.icons.evaicons.outline.Layers
import compose.icons.evaicons.outline.Options2
import compose.icons.evaicons.outline.PlayCircle
import compose.icons.evaicons.outline.Search
import compose.icons.evaicons.outline.Text
import compose.icons.evaicons.outline.Trash2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel,
    onBackClicked: () -> Unit,
    onGameSelected: (String) -> Unit,
    onEditSelected: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var previewGame by remember { mutableStateOf<UserImageListWithId?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    MemoireTheme(appTheme = uiState.appTheme) {
        Scaffold(
            topBar = {
                Column {
                    AppHeader(
                        title = stringResource(R.string.saved_boards),
                        navigationIcon = {
                            AppHeaderIcon(
                                icon = EvaIcons.Outline.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                onClick = onBackClicked
                            )
                        }
                    )

                    // Search & Sort Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.spacing_medium)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text(stringResource(R.string.search_boards)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large)),
                            leadingIcon = {
                                Icon(
                                    EvaIcons.Outline.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            colors = getAppTextFieldColors()
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            AppTooltipIconButton(
                                icon = EvaIcons.Outline.Options2,
                                contentDescription = stringResource(R.string.sort),
                                tooltipText = stringResource(R.string.sort),
                                onClick = { showSortMenu = true },
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                    alpha = 0.4f
                                ),
                                modifier = Modifier.size(48.dp)
                            )

                            MaterialTheme(
                                shapes = MaterialTheme.shapes.copy(
                                    extraSmall = RoundedCornerShape(
                                        20.dp
                                    )
                                ),
                                colorScheme = MaterialTheme.colorScheme.copy(surface = MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surface)
                                        .width(200.dp)
                                ) {
                                    SortOrder.entries.forEach { order ->
                                        val isSelected = uiState.sortOrder == order
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = when (order) {
                                                        SortOrder.LATEST -> stringResource(R.string.sort_latest)
                                                        SortOrder.OLDEST -> stringResource(R.string.sort_oldest)
                                                        SortOrder.NAME_ASC -> stringResource(R.string.sort_name_asc)
                                                        SortOrder.NAME_DESC -> stringResource(R.string.sort_name_desc)
                                                        SortOrder.PAIRS_COUNT -> stringResource(R.string.sort_pairs)
                                                    },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                viewModel.onSortOrderChanged(order)
                                                showSortMenu = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = when (order) {
                                                        SortOrder.LATEST -> EvaIcons.Outline.Clock
                                                        SortOrder.OLDEST -> EvaIcons.Outline.Calendar
                                                        SortOrder.NAME_ASC -> EvaIcons.Outline.Text
                                                        SortOrder.NAME_DESC -> EvaIcons.Outline.Text
                                                        SortOrder.PAIRS_COUNT -> EvaIcons.Outline.Layers
                                                    },
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            },
                                            trailingIcon = {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = EvaIcons.Outline.Checkmark,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            },
                                            colors = MenuDefaults.itemColors(
                                                textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.filteredGames.isEmpty()) {
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.filteredGames) { game ->
                            GameItem(
                                game = game,
                                onClick = { previewGame = game },
                                onEdit = { onEditSelected(game.name) },
                                onDelete = { viewModel.deleteGame(game.name) }
                            )
                        }
                    }
                }

                // Preview Dialog
                previewGame?.let { game ->
                    BoardPreviewDialog(
                        game = game,
                        onDismiss = { previewGame = null },
                        onPlay = {
                            onGameSelected(game.name)
                            previewGame = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameItem(
    game: UserImageListWithId,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val relativeDate = DateUtils.getRelativeTimeSpanString(game.createdAt).toString()

    Surface(
        modifier = Modifier
            .padding(horizontal = dimensionResource(R.dimen.spacing_medium))
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = dimensionResource(R.dimen.spacing_tiny),
        shadowElevation = dimensionResource(R.dimen.spacing_tiny)
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.created_at, relativeDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    AppTooltipIconButton(
                        icon = EvaIcons.Outline.Trash2,
                        contentDescription = stringResource(R.string.delete),
                        tooltipText = stringResource(R.string.delete),
                        onClick = onDelete,
                        containerColor = Color.Transparent,
                        iconTint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(40.dp)
                    )

                    AppTooltipIconButton(
                        icon = EvaIcons.Outline.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tooltipText = stringResource(R.string.edit),
                        onClick = onEdit,
                        containerColor = Color.Transparent,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    AppTooltipIconButton(
                        icon = EvaIcons.Outline.Eye,
                        contentDescription = stringResource(R.string.preview_desc),
                        tooltipText = stringResource(R.string.preview_desc),
                        onClick = onClick,
                        containerColor = MaterialTheme.colorScheme.primary,
                        iconTint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            // Image Preview Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                modifier = Modifier.fillMaxWidth()
            ) {
                game.images.take(4).forEach { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.card_size_preview))
                            .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_medium)))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                }

                if (game.images.size > 4) {
                    Box(
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.card_size_preview))
                            .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_medium)))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.more_images_count, game.images.size - 4),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardPreviewDialog(
    game: UserImageListWithId,
    onDismiss: () -> Unit,
    onPlay: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.board_gallery),
        icon = EvaIcons.Outline.Eye
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = game.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = pluralStringResource(
                    R.plurals.contains_pairs_plural,
                    game.images.size,
                    game.images.size
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_large))
            )

            // Grid showing ALL images
            Box(modifier = Modifier.heightIn(max = dimensionResource(R.dimen.dialog_max_height))) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(80.dp),
                    contentPadding = PaddingValues(dimensionResource(R.dimen.spacing_tiny)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(game.images) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_medium)))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_extra_large)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.radius_medium))
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = onPlay,
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(EvaIcons.Outline.PlayCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.start_game), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(dimensionResource(R.dimen.spacing_extra_large)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.no_saved_games),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.create_first_board),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing_small))
        )
    }
}
