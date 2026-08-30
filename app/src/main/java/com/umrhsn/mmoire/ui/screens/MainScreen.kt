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
                        label = "gameTransition"
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
                Spacer(modifier = Modifier.height(100.dp))
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
                    title = "Create Custom Game",
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
            .padding(16.dp)
            .fillMaxWidth(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBadge(icon = Icons.Default.DirectionsRun, value = numMoves.toString())
                StatBadge(icon = Icons.Default.Extension, value = "$numPairs/$totalPairs")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                ControlIcon(icon = Icons.Default.Refresh, contentDescription = "Reset", onClick = onRefresh)
                ControlIcon(icon = Icons.Default.AspectRatio, contentDescription = "Size", onClick = onSize)
                ControlIcon(icon = Icons.Default.AddCircle, contentDescription = "Create", onClick = onCreate)
                ControlIcon(icon = Icons.Default.FolderOpen, contentDescription = "Load Local", onClick = onDownload)
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
        title = if (isSmoothWin) "Perfect Win! 🏆" else "Game Finished!",
        icon = if (isSmoothWin) Icons.Default.WorkspacePremium else Icons.Default.SentimentVerySatisfied
    ) {
        Text(
            text = "Amazing! You cleared the board in $numMoves moves.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Close")
            }
            Button(
                onClick = { onPlayAgain() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Play Again", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BoardSizeDialog(
    currentSize: BoardSize,
    title: String = "Choose Level",
    icon: ImageVector = Icons.Default.Layers,
    onSizeSelected: (BoardSize) -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = title,
        icon = icon
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 450.dp)
                .verticalScroll(scrollState)
        ) {
            BoardSize.values().forEach { size ->
                val isSelected = size == currentSize
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onSizeSelected(size) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(12.dp),
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
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = size.name.replace("_", " ").lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${size.numCards} cards • ${size.getNumPairs()} pairs",
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
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("MAYBE LATER", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
