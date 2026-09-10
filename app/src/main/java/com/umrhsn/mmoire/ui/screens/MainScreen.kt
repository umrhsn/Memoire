package com.umrhsn.mmoire.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.models.MemoryGame
import com.umrhsn.mmoire.models.TwoPlayerLayout
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
import com.umrhsn.mmoire.viewmodels.MainUiState
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
import compose.icons.evaicons.outline.Person
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
    windowSizeClass: WindowSizeClass,
    onCreateClicked: (BoardSize) -> Unit,
    onBrowseClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onCardClicked: (Int, Int) -> Unit,
    onWin: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val config = LocalConfiguration.current

    val isTablet = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE

    var showWinDialog by remember { mutableStateOf(false) }

    var showSizeDialog by remember { mutableStateOf(false) }
    var showCreateSelectionDialog by remember { mutableStateOf(false) }

    var showMoreMenu by remember { mutableStateOf(false) }

    LaunchedEffect(
        uiState.winner,
        uiState.memoryGameP1?.haveWonGame(),
        uiState.hasTriggeredWinEffects
    ) {
        val isWon = if (uiState.isTwoPlayerMode) {
            uiState.winner != null
        } else {
            uiState.memoryGameP1?.haveWonGame() == true
        }

        if (isWon && !uiState.hasTriggeredWinEffects) {
            val game = if (uiState.isTwoPlayerMode) {
                if (uiState.winner == 1) uiState.memoryGameP1 else uiState.memoryGameP2
            } else {
                uiState.memoryGameP1
            }
            onWin(game?.smoothWin() == true)
            viewModel.playWinSound()
            showWinDialog = true
            viewModel.setWinEffectsTriggered()
        } else if (!isWon) {
            showWinDialog = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isTablet && isLandscape && !uiState.isTwoPlayerMode) {
            // Tablet Landscape Layout: Sidebar for stats, main area for board
            Row(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .width(280.dp)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(32.dp),
                    tonalElevation = 2.dp,
                    shadowElevation = 8.dp,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.gameName ?: stringResource(R.string.app_name),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Start,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Box {
                                AppHeaderIcon(
                                    icon = EvaIcons.Outline.MoreVertical,
                                    contentDescription = stringResource(R.string.more_options),
                                    onClick = { showMoreMenu = true }
                                )

                                OverflowDropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false },
                                    onSettingsClicked = onSettingsClicked,
                                    onBrowseClicked = onBrowseClicked,
                                    showCreateSelectionDialog = {
                                        showCreateSelectionDialog = true
                                    },
                                    startTutorial = { viewModel.startTutorial() }
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.4f
                            )
                        )

                        StatsSection(uiState = uiState, isVertical = true)

                        Spacer(modifier = Modifier.weight(1f))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.refreshGame()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(EvaIcons.Outline.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.reset_game))
                            }

                            Button(
                                onClick = { showSizeDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(EvaIcons.Outline.Grid, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.change_size))
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    GameBoardArea(
                        uiState = uiState,
                        onCardClicked = onCardClicked,
                        haptic = haptic,
                        isTablet = isTablet,
                        isLandscape = isLandscape
                    )
                }
            }
        } else {
            // Phone or Tablet Portrait Layout
            Column(modifier = Modifier.fillMaxSize()) {
                MainHeader(
                    uiState = uiState,
                    haptic = haptic,
                    viewModel = viewModel,
                    onBrowseClicked = onBrowseClicked,
                    onSettingsClicked = onSettingsClicked,
                    showSizeDialog = { showSizeDialog = true },
                    showCreateSelectionDialog = { showCreateSelectionDialog = true },
                    showMoreMenu = showMoreMenu,
                    onMoreMenuChange = { showMoreMenu = it }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .tutorialAnchor(
                            "game_board",
                            viewModel::onAnchorPositioned,
                            viewModel::onAnchorRemoved
                        )
                ) {
                    if (uiState.isTwoPlayerMode) {
                        TwoPlayerLayoutSwitcher(
                            uiState = uiState,
                            onCardClicked = onCardClicked,
                            isTablet = isTablet,
                            isLandscape = isLandscape
                        )
                    } else {
                        GameBoardArea(
                            uiState = uiState,
                            onCardClicked = onCardClicked,
                            haptic = haptic,
                            isTablet = isTablet,
                            isLandscape = isLandscape
                        )
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

                if (uiState.memoryGameP1 != null && !uiState.isTwoPlayerMode) {
                    StatsSection(uiState = uiState, isVertical = false)
                }
            }
        }

        // Dialogs...
        if (showWinDialog) {
            WinDialog(
                numMoves = (if (uiState.winner == 2) uiState.memoryGameP2 else uiState.memoryGameP1)?.getNumMoves()
                    ?: 0,
                timeSeconds = uiState.timerSeconds,
                isSmoothWin = (if (uiState.winner == 2) uiState.memoryGameP2 else uiState.memoryGameP1)?.smoothWin() == true,
                bestTime = uiState.bestTime,
                winner = uiState.winner,
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
                isTwoPlayerMode = uiState.isTwoPlayerMode,
                onToggleTwoPlayerMode = { viewModel.toggleTwoPlayerMode(it) },
                onAnchorPositioned = viewModel::onAnchorPositioned,
                onAnchorRemoved = viewModel::onAnchorRemoved,
                onSizeSelected = {
                    viewModel.changeSize(it)
                    showSizeDialog = false
                },
                onDismiss = { showSizeDialog = false },
                isTablet = isTablet
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
                onDismiss = { showCreateSelectionDialog = false },
                isTablet = isTablet
            )
        }

        if (uiState.showTutorial) {
            TutorialOverlay(
                steps = getMainTutorialSteps(),
                anchors = uiState.tutorialAnchors,
                onComplete = { viewModel.dismissTutorial() },
                onSkip = { viewModel.dismissTutorial() },
                onStepChanged = { index ->
                    showSizeDialog = index == 5
                }
            )
        }
    }
}

@Composable
private fun TwoPlayerLayoutSwitcher(
    uiState: MainUiState,
    onCardClicked: (Int, Int) -> Unit,
    isTablet: Boolean,
    isLandscape: Boolean
) {
    val layout = uiState.twoPlayerLayout

    // Split direction based on mode and orientation
    val isHorizontalSplit = if (isLandscape) {
        layout == TwoPlayerLayout.SIDE_BY_SIDE
    } else {
        true // Always top-bottom in portrait
    }

    if (isHorizontalSplit) {
        // Mode: SIDE_BY_SIDE or Portrait split
        Column(modifier = Modifier.fillMaxSize()) {
            // Player 2 Area (Top half)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationZ =
                            if (layout == TwoPlayerLayout.OPPOSITE || layout == TwoPlayerLayout.FACE_TO_FACE) 180f else 0f
                    }
            ) {
                PlayerRaceHalf(
                    playerNumber = 2,
                    game = uiState.memoryGameP2,
                    boardSize = uiState.boardSize,
                    timeSeconds = uiState.timerSecondsP2,
                    onCardClicked = { onCardClicked(it, 2) },
                    isTablet = isTablet,
                    isLandscape = isLandscape,
                    statsInCenter = (layout != TwoPlayerLayout.SIDE_BY_SIDE)
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 48.dp)
            )
            // Player 1 Area (Bottom half)
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()) {
                PlayerRaceHalf(
                    playerNumber = 1,
                    game = uiState.memoryGameP1,
                    boardSize = uiState.boardSize,
                    timeSeconds = uiState.timerSeconds,
                    onCardClicked = { onCardClicked(it, 1) },
                    isTablet = isTablet,
                    isLandscape = isLandscape,
                    statsInCenter = (layout != TwoPlayerLayout.SIDE_BY_SIDE)
                )
            }
        }
    } else {
        // Mode: FACE_TO_FACE or OPPOSITE (Landscape vertical split)
        Row(modifier = Modifier.fillMaxSize()) {
            // Player 2 Area (Left) - Using rotation 180 or -90 based on playerEnd sitting end
            Box(modifier = Modifier.weight(1f)) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val w = this.maxWidth
                    val h = this.maxHeight
                    Box(
                        modifier = Modifier
                            .size(h, w)
                            .align(Alignment.Center)
                            .graphicsLayer {
                                rotationZ =
                                    if (layout == TwoPlayerLayout.FACE_TO_FACE) -90f else -180f
                            }
                    ) {
                        PlayerRaceHalf(
                            playerNumber = 2,
                            game = uiState.memoryGameP2,
                            boardSize = uiState.boardSize,
                            timeSeconds = uiState.timerSecondsP2,
                            onCardClicked = { onCardClicked(it, 2) },
                            isTablet = isTablet,
                            isLandscape = true,
                            statsInCenter = true
                        )
                    }
                }
            }

            VerticalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 48.dp)
            )

            // Player 1 Area (Right)
            Box(modifier = Modifier.weight(1f)) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val w = this.maxWidth
                    val h = this.maxHeight
                    Box(
                        modifier = Modifier
                            .size(h, w)
                            .align(Alignment.Center)
                            .graphicsLayer {
                                rotationZ = if (layout == TwoPlayerLayout.FACE_TO_FACE) 90f else 0f
                            }
                    ) {
                        PlayerRaceHalf(
                            playerNumber = 1,
                            game = uiState.memoryGameP1,
                            boardSize = uiState.boardSize,
                            timeSeconds = uiState.timerSeconds,
                            onCardClicked = { onCardClicked(it, 1) },
                            isTablet = isTablet,
                            isLandscape = true,
                            statsInCenter = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainHeader(
    uiState: MainUiState,
    haptic: HapticFeedback,
    viewModel: MainViewModel,
    onBrowseClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    showSizeDialog: () -> Unit,
    showCreateSelectionDialog: () -> Unit,
    showMoreMenu: Boolean,
    onMoreMenuChange: (Boolean) -> Unit
) {
    AppHeader(
        title = uiState.gameName ?: stringResource(R.string.app_name),
        actions = {
            if (uiState.isTwoPlayerMode) {
                // Layout Toggle for 2-player mode
                AppHeaderIcon(
                    icon = when (uiState.twoPlayerLayout) {
                        TwoPlayerLayout.FACE_TO_FACE -> EvaIcons.Outline.Navigation2
                        TwoPlayerLayout.SIDE_BY_SIDE -> EvaIcons.Outline.Person
                        TwoPlayerLayout.OPPOSITE -> EvaIcons.Outline.Flash
                    },
                    contentDescription = "Change Layout",
                    onClick = { viewModel.toggleTwoPlayerLayout() }
                )
            }

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
                    showSizeDialog()
                }
            )

            Box {
                AppHeaderIcon(
                    icon = EvaIcons.Outline.MoreVertical,
                    contentDescription = stringResource(R.string.more_options),
                    onClick = { onMoreMenuChange(true) },
                    modifier = Modifier.tutorialAnchor(
                        "more_options",
                        viewModel::onAnchorPositioned,
                        viewModel::onAnchorRemoved
                    )
                )

                OverflowDropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { onMoreMenuChange(false) },
                    onBrowseClicked = onBrowseClicked,
                    onSettingsClicked = onSettingsClicked,
                    showCreateSelectionDialog = showCreateSelectionDialog,
                    startTutorial = { viewModel.startTutorial() }
                )
            }
        },
        modifier = Modifier.tutorialAnchor(
            "header_actions",
            viewModel::onAnchorPositioned,
            viewModel::onAnchorRemoved
        )
    )
}

@Composable
private fun OverflowDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onBrowseClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    showCreateSelectionDialog: () -> Unit,
    startTutorial: () -> Unit
) {
    MaterialTheme(
        shapes = MaterialTheme.shapes.copy(
            extraSmall = RoundedCornerShape(24.dp)
        )
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .width(220.dp)
        ) {
            AppDropdownItem(
                text = stringResource(R.string.create_game),
                icon = EvaIcons.Outline.PlusCircle,
                onClick = {
                    onDismissRequest()
                    showCreateSelectionDialog()
                }
            )
            AppDropdownItem(
                text = stringResource(R.string.load_game),
                icon = EvaIcons.Outline.Folder,
                onClick = {
                    onDismissRequest()
                    onBrowseClicked()
                }
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            AppDropdownItem(
                text = stringResource(R.string.settings),
                icon = EvaIcons.Outline.Settings,
                onClick = {
                    onDismissRequest()
                    onSettingsClicked()
                }
            )
            AppDropdownItem(
                text = stringResource(R.string.help),
                icon = EvaIcons.Outline.QuestionMarkCircle,
                onClick = {
                    onDismissRequest()
                    startTutorial()
                }
            )
        }
    }
}

@Composable
private fun GameBoardArea(
    uiState: MainUiState,
    onCardClicked: (Int, Int) -> Unit,
    haptic: HapticFeedback,
    isTablet: Boolean,
    isLandscape: Boolean
) {
    AnimatedContent(
        targetState = uiState.gameSessionId,
        transitionSpec = {
            fadeIn(animationSpec = tween(600, easing = EaseInOutQuart)) togetherWith
                    fadeOut(animationSpec = tween(400))
        },
        label = stringResource(R.string.gameTransition_label)
    ) { targetSessionId: Long ->
        var sessionGame by remember { mutableStateOf(uiState.memoryGameP1) }
        var sessionSize by remember { mutableStateOf(uiState.boardSize) }

        if (targetSessionId == uiState.gameSessionId) {
            sessionGame = uiState.memoryGameP1
            sessionSize = uiState.boardSize
        }

        if (sessionGame != null) {
            MemoryBoard(
                boardSize = sessionSize,
                cards = sessionGame!!.cards,
                isTwoPlayerMode = uiState.isTwoPlayerMode,
                onCardClicked = { pos ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCardClicked(pos, 1)
                },
                isTablet = isTablet,
                isLandscape = isLandscape
            )
        }
    }
}

@Composable
private fun StatsSection(uiState: MainUiState, isVertical: Boolean) {
    val gameP1 = uiState.memoryGameP1 ?: return

    if (isVertical) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatItem(
                icon = EvaIcons.Outline.Flash,
                label = stringResource(R.string.moves_tracking_label),
                value = gameP1.getNumMoves().toString()
            )
            StatItem(
                icon = EvaIcons.Outline.Layers,
                label = stringResource(R.string.pairs_tracking_label),
                value = "${gameP1.numPairsFound}/${uiState.boardSize.getNumPairs()}"
            )
            StatItem(
                icon = EvaIcons.Outline.Clock,
                label = stringResource(R.string.timer_tracking_label),
                value = formatDuration(uiState.timerSeconds)
            )
            uiState.bestTime?.let {
                StatItem(
                    icon = EvaIcons.Outline.Award,
                    label = "Best Time",
                    value = formatDuration(it),
                    isHighlight = true
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            FloatingPill(modifier = Modifier.wrapContentWidth()) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBadge(
                        icon = EvaIcons.Outline.Flash,
                        value = stringResource(R.string.moves_count, gameP1.getNumMoves()),
                        tooltipText = stringResource(R.string.moves_tracking_label)
                    )
                    StatBadge(
                        icon = EvaIcons.Outline.Layers,
                        value = stringResource(
                            R.string.pairs_progress,
                            gameP1.numPairsFound,
                            uiState.boardSize.getNumPairs()
                        ),
                        tooltipText = stringResource(R.string.pairs_tracking_label)
                    )
                    StatBadge(
                        icon = EvaIcons.Outline.Clock,
                        value = formatDuration(uiState.timerSeconds),
                        tooltipText = stringResource(R.string.timer_tracking_label)
                    )
                    uiState.bestTime?.let {
                        StatBadge(
                            icon = EvaIcons.Outline.Award,
                            value = formatDuration(it),
                            tooltipText = stringResource(R.string.best_time_tracking_label),
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isHighlight) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
        border = if (isHighlight) BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
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
private fun PlayerStatsContent(
    playerNumber: Int,
    game: MemoryGame?,
    boardSize: BoardSize,
    timeSeconds: Long
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                EvaIcons.Outline.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.player_n, playerNumber),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatBadge(
            EvaIcons.Outline.Clock,
            formatDuration(timeSeconds),
            tooltipText = stringResource(R.string.timer_tracking_label)
        )
        StatBadge(
            EvaIcons.Outline.Flash,
            (game?.getNumMoves() ?: 0).toString(),
            tooltipText = stringResource(R.string.moves_tracking_label)
        )
        StatBadge(
            EvaIcons.Outline.Layers,
            "${game?.numPairsFound ?: 0}/${boardSize.getNumPairs()}",
            tooltipText = stringResource(R.string.pairs_tracking_label)
        )
    }
}

@Composable
private fun PlayerStatsHeader(
    playerNumber: Int,
    game: MemoryGame?,
    boardSize: BoardSize,
    timeSeconds: Long
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerStatsContent(playerNumber, game, boardSize, timeSeconds)
    }
}

@Composable
private fun PlayerRaceHalf(
    playerNumber: Int,
    game: MemoryGame?,
    boardSize: BoardSize,
    timeSeconds: Long,
    onCardClicked: (Int) -> Unit,
    isTablet: Boolean = false,
    isLandscape: Boolean = false,
    statsInCenter: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    Column(modifier = Modifier.fillMaxSize()) {
        if (statsInCenter) {
            Box(modifier = Modifier.weight(1f)) {
                if (game != null) {
                    MemoryBoard(
                        boardSize = boardSize,
                        cards = game.cards,
                        isTwoPlayerMode = true,
                        onCardClicked = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCardClicked(it)
                        },
                        isTablet = isTablet,
                        isLandscape = isLandscape
                    )
                }
            }
            PlayerStatsHeader(playerNumber, game, boardSize, timeSeconds)
        } else {
            PlayerStatsHeader(playerNumber, game, boardSize, timeSeconds)
            Box(modifier = Modifier.weight(1f)) {
                if (game != null) {
                    MemoryBoard(
                        boardSize = boardSize,
                        cards = game.cards,
                        isTwoPlayerMode = true,
                        onCardClicked = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCardClicked(it)
                        },
                        isTablet = isTablet,
                        isLandscape = isLandscape
                    )
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
    winner: Int? = null,
    onPlayAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = if (winner != null) {
            if (winner == 0) stringResource(R.string.draw_game)
            else stringResource(R.string.winner_player_n, winner)
        } else if (isSmoothWin) stringResource(R.string.perfect_win)
        else stringResource(R.string.game_finished),
        icon = if (winner != null || isSmoothWin) EvaIcons.Outline.Award else EvaIcons.Outline.SmilingFace
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (winner != null) {
                Text(
                    text = stringResource(R.string.congratulations),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = pluralStringResource(R.plurals.win_message_plural, numMoves, numMoves),
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
            ) { Text(stringResource(R.string.close)) }
            Button(
                onClick = { onPlayAgain() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) { Text(stringResource(R.string.play_again), fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun BoardSizeDialog(
    currentSize: BoardSize,
    title: String = stringResource(R.string.choose_level),
    icon: ImageVector = EvaIcons.Outline.Layers,
    isTwoPlayerMode: Boolean = false,
    onToggleTwoPlayerMode: ((Boolean) -> Unit)? = null,
    onAnchorPositioned: ((String, Rect) -> Unit)? = null,
    onAnchorRemoved: ((String) -> Unit)? = null,
    onSizeSelected: (BoardSize) -> Unit,
    onDismiss: () -> Unit,
    isTablet: Boolean = false
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = title,
        icon = icon,
        modifier = if (isTablet) Modifier.fillMaxWidth(0.6f) else Modifier.fillMaxWidth(0.9f)
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = dimensionResource(R.dimen.dialog_max_height))
                .verticalScroll(scrollState)
        ) {
            if (onToggleTwoPlayerMode != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            EvaIcons.Outline.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.two_player_mode),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        Switch(
                            checked = isTwoPlayerMode,
                            onCheckedChange = onToggleTwoPlayerMode,
                            modifier = Modifier.then(
                                if (onAnchorPositioned != null) Modifier.tutorialAnchor(
                                    "race_toggle",
                                    onAnchorPositioned,
                                    onAnchorRemoved
                                ) else Modifier
                            ),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            BoardSize.entries.forEach { size ->
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
                    border = if (isSelected) BorderStroke(
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
                                EvaIcons.Outline.CheckmarkCircle2,
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
        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(
                stringResource(R.string.maybe_later),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
