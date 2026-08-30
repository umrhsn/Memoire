package com.umrhsn.mmoire.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.models.MemoryGame
import com.umrhsn.mmoire.ui.components.AppDialog
import com.umrhsn.mmoire.ui.components.AppHeader
import com.umrhsn.mmoire.ui.components.MemoryBoard
import com.umrhsn.mmoire.ui.components.StatBadge
import com.umrhsn.mmoire.ui.theme.MemoireTheme
import com.umrhsn.mmoire.viewmodels.MainViewModel
import java.util.Locale

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onCreateClicked: (BoardSize) -> Unit,
    onBrowseClicked: () -> Unit,
    onCardClicked: (Int) -> Unit,
    onWin: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val game = uiState.memoryGame

    var showWinDialog by remember { mutableStateOf(false) }
    var hasTriggeredWinEffects by remember { mutableStateOf(false) }
    
    var showSizeDialog by remember { mutableStateOf(false) }
    var showCreateSelectionDialog by remember { mutableStateOf(false) }

    // Detect win state
    LaunchedEffect(game?.haveWonGame()) {
        if (game?.haveWonGame() == true && !hasTriggeredWinEffects) {
            onWin(game.smoothWin())
            viewModel.playWinSound()
            showWinDialog = true
            hasTriggeredWinEffects = true
        } else if (game?.haveWonGame() == false) {
            hasTriggeredWinEffects = false
            showWinDialog = false
        }
    }

    MemoireTheme {
        Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header (Shared)
                AppHeader(title = uiState.gameName ?: stringResource(R.string.app_name))

                // Game Board Area with Transition
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = uiState.gameSessionId,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(500)) togetherWith
                            fadeOut(animationSpec = tween(500))
                        },
                        label = stringResource(R.string.gameTransition_label)
                    ) { targetSessionId: Long ->
                        var sessionGame by remember { mutableStateOf(uiState.memoryGame) }
                        var sessionSize by remember { mutableStateOf(uiState.boardSize) }

                        if (targetSessionId == uiState.gameSessionId) {
                            sessionGame = uiState.memoryGame
                            sessionSize = uiState.boardSize
                        }

                        if (sessionGame != null) {
                            MemoryBoard(
                                boardSize = sessionSize,
                                cards = sessionGame!!.cards,
                                onCardClicked = onCardClicked
                            )
                        }
                    }

                    // Loading Overlay
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                }
                
                // Spacer for Floating Bottom Bar
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.app_header_height)))
            }

            // Floating Bottom Bar
            if (game != null) {
                FloatingControlBar(
                    numMoves = game.getNumMoves(),
                    numPairs = game.numPairsFound,
                    totalPairs = uiState.boardSize.getNumPairs(),
                    onRefresh = { viewModel.refreshGame() },
                    onSize = { showSizeDialog = true },
                    onCreate = { showCreateSelectionDialog = true },
                    onDownload = { onBrowseClicked() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // Dialogs
            if (showWinDialog) {
                WinDialog(
                    numMoves = game?.getNumMoves() ?: 0,
                    isSmoothWin = game?.smoothWin() == true,
                    onPlayAgain = {
                        showWinDialog = false
                        viewModel.refreshGame()
                    },
                    onDismiss = { showWinDialog = false }
                )
            }

            if (showSizeDialog) {
                BoardSizeDialog(
                    currentSize = uiState.boardSize,
                    onSizeSelected = {
                        viewModel.changeSize(it)
                        showSizeDialog = false
                    },
                    onDismiss = { showSizeDialog = false }
                )
            }

            if (showCreateSelectionDialog) {
                BoardSizeDialog(
                    currentSize = uiState.boardSize,
                    title = stringResource(R.string.create_custom_game),
                    icon = Icons.Default.AddPhotoAlternate,
                    onSizeSelected = {
                        onCreateClicked(it)
                        showCreateSelectionDialog = false
                    },
                    onDismiss = { showCreateSelectionDialog = false }
                )
            }
        }
    }
}

@Composable
private fun FloatingControlBar(
    numMoves: Int,
    numPairs: Int,
    totalPairs: Int,
    onRefresh: () -> Unit,
    onSize: () -> Unit,
    onCreate: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(dimensionResource(R.dimen.spacing_medium))
            .fillMaxWidth(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = dimensionResource(R.dimen.spacing_small),
        shadowElevation = dimensionResource(R.dimen.radius_medium),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.spacing_small),
                vertical = dimensionResource(R.dimen.spacing_tiny)
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.padding(start = dimensionResource(R.dimen.radius_medium)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.radius_medium))
            ) {
                StatBadge(icon = Icons.Default.DirectionsRun, value = numMoves.toString())
                StatBadge(icon = Icons.Default.Extension, value = stringResource(R.string.pairs_progress, numPairs, totalPairs))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                ControlIcon(icon = Icons.Default.Refresh, contentDescription = stringResource(R.string.reset_game), onClick = onRefresh)
                ControlIcon(icon = Icons.Default.AspectRatio, contentDescription = stringResource(R.string.change_size), onClick = onSize)
                ControlIcon(icon = Icons.Default.AddCircle, contentDescription = stringResource(R.string.create_game), onClick = onCreate)
                ControlIcon(icon = Icons.Default.FolderOpen, contentDescription = stringResource(R.string.load_game), onClick = onDownload)
            }
        }
    }
}

@Composable
private fun ControlIcon(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun WinDialog(
    numMoves: Int,
    isSmoothWin: Boolean,
    onPlayAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = if (isSmoothWin) stringResource(R.string.perfect_win) else stringResource(R.string.game_finished),
        icon = if (isSmoothWin) Icons.Default.WorkspacePremium else Icons.Default.SentimentVerySatisfied
    ) {
        Text(
            text = stringResource(R.string.win_message, numMoves),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_extra_large)))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.radius_medium))
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
            ) {
                Text(stringResource(R.string.close))
            }
            Button(
                onClick = { onPlayAgain() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
            ) {
                Text(stringResource(R.string.play_again), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BoardSizeDialog(
    currentSize: BoardSize,
    title: String = stringResource(R.string.choose_level),
    icon: ImageVector = Icons.Default.Layers,
    onSizeSelected: (BoardSize) -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = title,
        icon = icon
    ) {
        val density = LocalDensity.current
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = dimensionResource(R.dimen.dialog_max_height))
                .verticalScroll(scrollState)
        ) {
            BoardSize.values().forEach { size ->
                val isSelected = size == currentSize
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.spacing_small))
                        .clickable { onSizeSelected(size) },
                    shape = RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large)),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(dimensionResource(R.dimen.spacing_huge)),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium)),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when(size) {
                                        BoardSize.SUPER_DUPER_EASY -> Icons.Default.SentimentVerySatisfied
                                        BoardSize.SUPER_EASY -> Icons.Default.Mood
                                        BoardSize.EASY -> Icons.Default.Bolt
                                        BoardSize.MEDIUM -> Icons.Default.FilterHdr
                                        BoardSize.HARD -> Icons.Default.Diamond
                                        BoardSize.SUPER_HARD -> Icons.Default.Whatshot
                                        BoardSize.SUPER_DUPER_HARD -> Icons.Default.LocalFireDepartment
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(with(density) { dimensionResource(R.dimen.text_large).toSp() }.value.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = size.name.replace("_", " ").lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.cards_pairs_info, size.numCards, size.getNumPairs()),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.maybe_later), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
