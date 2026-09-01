package com.umrhsn.mmoire.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.ui.components.AppDialog
import com.umrhsn.mmoire.ui.components.AppDropdownItem
import com.umrhsn.mmoire.ui.components.AppHeader
import com.umrhsn.mmoire.ui.components.AppHeaderIcon
import com.umrhsn.mmoire.ui.components.FloatingPill
import com.umrhsn.mmoire.ui.components.MemoryBoard
import com.umrhsn.mmoire.ui.components.StatBadge
import com.umrhsn.mmoire.ui.components.TutorialOverlay
import com.umrhsn.mmoire.ui.components.getMainTutorialSteps
import com.umrhsn.mmoire.ui.components.tutorialAnchor
import com.umrhsn.mmoire.ui.theme.MemoireTheme
import com.umrhsn.mmoire.viewmodels.MainViewModel
import compose.icons.EvaIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.Activity
import compose.icons.evaicons.outline.AlertTriangle
import compose.icons.evaicons.outline.Award
import compose.icons.evaicons.outline.CheckmarkCircle2
import compose.icons.evaicons.outline.Clock
import compose.icons.evaicons.outline.Flash
import compose.icons.evaicons.outline.Folder
import compose.icons.evaicons.outline.Grid
import compose.icons.evaicons.outline.Image
import compose.icons.evaicons.outline.Layers
import compose.icons.evaicons.outline.MoreVertical
import compose.icons.evaicons.outline.Navigation2
import compose.icons.evaicons.outline.PlusCircle
import compose.icons.evaicons.outline.QuestionMarkCircle
import compose.icons.evaicons.outline.Refresh
import compose.icons.evaicons.outline.Settings
import compose.icons.evaicons.outline.SmilingFace
import compose.icons.evaicons.outline.Star
import compose.icons.evaicons.outline.Sun

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
    val haptic = LocalHapticFeedback.current

    var showWinDialog by remember { mutableStateOf(false) }
    var hasTriggeredWinEffects by remember { mutableStateOf(false) }

    var showSizeDialog by remember { mutableStateOf(false) }
    var showCreateSelectionDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    var showMoreMenu by remember { mutableStateOf(false) }

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
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AppHeader(
                    title = uiState.gameName ?: stringResource(R.string.app_name),
                    actions = {
                        // Keep primary actions visible
                        AppHeaderIcon(
                            icon = EvaIcons.Outline.Refresh,
                            contentDescription = stringResource(R.string.reset_game),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.refreshGame()
                            },
                            autoMirror = true
                        )
                        AppHeaderIcon(
                            icon = EvaIcons.Outline.Grid,
                            contentDescription = stringResource(R.string.change_size),
                            onClick = {
                                viewModel.playClickSound()
                                showSizeDialog = true
                            }
                        )

                        // Overflow for less frequent actions
                        Box {
                            AppHeaderIcon(
                                icon = EvaIcons.Outline.MoreVertical,
                                contentDescription = stringResource(R.string.more_options),
                                onClick = { showMoreMenu = true }
                            )

                            MaterialTheme(
                                shapes = MaterialTheme.shapes.copy(
                                    extraSmall = RoundedCornerShape(
                                        24.dp
                                    )
                                )
                            ) {
                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surface)
                                        .width(220.dp)
                                ) {
                                    AppDropdownItem(
                                        text = stringResource(R.string.create_game),
                                        icon = EvaIcons.Outline.PlusCircle,
                                        onClick = {
                                            showMoreMenu = false
                                            showCreateSelectionDialog = true
                                        }
                                    )
                                    AppDropdownItem(
                                        text = stringResource(R.string.load_game),
                                        icon = EvaIcons.Outline.Folder,
                                        onClick = {
                                            showMoreMenu = false
                                            onBrowseClicked()
                                        }
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(
                                            vertical = 4.dp,
                                            horizontal = 12.dp
                                        ),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                    AppDropdownItem(
                                        text = stringResource(R.string.settings),
                                        icon = EvaIcons.Outline.Settings,
                                        onClick = {
                                            showMoreMenu = false
                                            showSettingsDialog = true
                                        },
                                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    AppDropdownItem(
                                        text = stringResource(R.string.help),
                                        icon = EvaIcons.Outline.QuestionMarkCircle,
                                        onClick = {
                                            showMoreMenu = false
                                            viewModel.startTutorial()
                                        },
                                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.tutorialAnchor(
                                            "help_action",
                                            viewModel::onAnchorPositioned
                                        )
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.tutorialAnchor(
                        "header_actions",
                        viewModel::onAnchorPositioned
                    )
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .tutorialAnchor("game_board", viewModel::onAnchorPositioned)
                ) {
                    AnimatedContent(
                        targetState = uiState.gameSessionId,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(600, easing = EaseInOutQuart)) togetherWith
                                    fadeOut(animationSpec = tween(400))
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
                                onCardClicked = { pos ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onCardClicked(pos)
                                }
                            )
                        }
                    }

                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 6.dp
                            )
                        }
                    }
                }

                if (game != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingPill(
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val movesColor =
                                    if (game.getNumMoves() <= uiState.boardSize.getNumPairs()) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                                    } else {
                                        null
                                    }

                                Row(
                                    modifier = Modifier.tutorialAnchor(
                                        "stats_tracking",
                                        viewModel::onAnchorPositioned
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    StatBadge(
                                        icon = EvaIcons.Outline.Flash,
                                        value = stringResource(
                                            R.string.moves_count,
                                            game.getNumMoves()
                                        ),
                                        tooltipText = stringResource(R.string.moves_tracking_label),
                                        containerColor = movesColor,
                                        contentColor = if (movesColor != null) MaterialTheme.colorScheme.onPrimaryContainer else null
                                    )

                                    val pairsFinished =
                                        game.numPairsFound == uiState.boardSize.getNumPairs()
                                    StatBadge(
                                        icon = EvaIcons.Outline.Layers,
                                        value = stringResource(
                                            R.string.pairs_progress,
                                            game.numPairsFound,
                                            uiState.boardSize.getNumPairs()
                                        ),
                                        tooltipText = stringResource(R.string.pairs_tracking_label),
                                        containerColor = if (pairsFinished) MaterialTheme.colorScheme.secondaryContainer.copy(
                                            alpha = 0.8f
                                        ) else null,
                                        contentColor = if (pairsFinished) MaterialTheme.colorScheme.onSecondaryContainer else null,
                                        autoMirror = true
                                    )
                                }

                                Row(
                                    modifier = Modifier.tutorialAnchor(
                                        "timer_tracking",
                                        viewModel::onAnchorPositioned
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    val isBreakingRecord =
                                        uiState.bestTime == null || uiState.timerSeconds < uiState.bestTime!!
                                    StatBadge(
                                        icon = EvaIcons.Outline.Clock,
                                        value = formatDuration(uiState.timerSeconds),
                                        tooltipText = stringResource(R.string.timer_tracking_label),
                                        containerColor = if (isBreakingRecord && game.numCardFlips > 0) MaterialTheme.colorScheme.tertiaryContainer.copy(
                                            alpha = 0.6f
                                        ) else null,
                                        contentColor = if (isBreakingRecord && game.numCardFlips > 0) MaterialTheme.colorScheme.onTertiaryContainer else null
                                    )

                                    uiState.bestTime?.let {
                                        StatBadge(
                                            icon = EvaIcons.Outline.Award,
                                            value = formatDuration(it),
                                            tooltipText = stringResource(R.string.best_time_tracking_label),
                                            containerColor = MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.15f
                                            ),
                                            contentColor = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showWinDialog) {
                WinDialog(
                    numMoves = game?.getNumMoves() ?: 0,
                    timeSeconds = uiState.timerSeconds,
                    isSmoothWin = game?.smoothWin() == true,
                    bestTime = uiState.bestTime,
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
                    icon = EvaIcons.Outline.Image,
                    onSizeSelected = {
                        onCreateClicked(it)
                        showCreateSelectionDialog = false
                    },
                    onDismiss = { showCreateSelectionDialog = false }
                )
            }

            if (showSettingsDialog) {
                SettingsDialog(
                    currentLanguage = uiState.appLanguage,
                    onLanguageSelected = {
                        viewModel.changeLanguage(it)
                        showSettingsDialog = false
                    },
                    onDismiss = { showSettingsDialog = false }
                )
            }

            if (uiState.showTutorial) {
                TutorialOverlay(
                    steps = getMainTutorialSteps(),
                    anchors = uiState.tutorialAnchors,
                    onComplete = { viewModel.dismissTutorial() },
                    onSkip = { viewModel.dismissTutorial() }
                )
            }
        }
    }
}

@Composable
fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return if (mins == 0L) {
        stringResource(R.string.duration_seconds, secs)
    } else {
        stringResource(R.string.duration_minutes_seconds, mins, secs)
    }
}

@Composable
private fun SettingsDialog(
    currentLanguage: String?,
    onLanguageSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.app_settings),
        icon = EvaIcons.Outline.Settings
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            Text(
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val languages = listOf(
                null to stringResource(R.string.lang_system),
                "ar-EG" to stringResource(R.string.lang_ar_eg),
                "ar" to stringResource(R.string.lang_ar),
                "en" to stringResource(R.string.lang_en),
                "fr" to stringResource(R.string.lang_fr),
                "de" to stringResource(R.string.lang_de),
                "es" to stringResource(R.string.lang_es)
            )

            languages.forEach { (tag, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onLanguageSelected(tag) }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentLanguage == tag,
                        onClick = { onLanguageSelected(tag) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun WinDialog(
    numMoves: Int,
    timeSeconds: Long,
    isSmoothWin: Boolean,
    bestTime: Long?,
    onPlayAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = if (isSmoothWin) stringResource(R.string.perfect_win) else stringResource(R.string.game_finished),
        icon = if (isSmoothWin) EvaIcons.Outline.Award else EvaIcons.Outline.SmilingFace
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.win_message, numMoves),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.play) + ": ${formatDuration(timeSeconds)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (bestTime != null && timeSeconds <= bestTime) {
                Text(
                    text = stringResource(R.string.current_record_info),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Black
                )
            } else if (bestTime != null) {
                Text(
                    text = stringResource(R.string.best_time_label, formatDuration(bestTime)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

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
                Text(stringResource(R.string.close))
            }
            Button(
                onClick = { onPlayAgain() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
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
    icon: ImageVector = EvaIcons.Outline.Layers,
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
                        .padding(vertical = 4.dp)
                        .clickable { onSizeSelected(size) },
                    shape = RoundedCornerShape(24.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.2f
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.primary
                    ) else null
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (size) {
                                        BoardSize.SUPER_DUPER_EASY -> EvaIcons.Outline.SmilingFace
                                        BoardSize.SUPER_EASY -> EvaIcons.Outline.Sun
                                        BoardSize.EASY -> EvaIcons.Outline.Flash
                                        BoardSize.MEDIUM -> EvaIcons.Outline.Navigation2
                                        BoardSize.HARD -> EvaIcons.Outline.Star
                                        BoardSize.SUPER_HARD -> EvaIcons.Outline.Activity
                                        BoardSize.SUPER_DUPER_HARD -> EvaIcons.Outline.AlertTriangle
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(size.getNameResId()),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(
                                    R.string.cards_pairs_info,
                                    size.numCards,
                                    size.getNumPairs()
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                    alpha = 0.7f
                                ) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = EvaIcons.Outline.CheckmarkCircle2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                stringResource(R.string.maybe_later),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
