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
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.AppColorTheme
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.models.MemoryGame
import com.umrhsn.mmoire.models.TwoPlayerLayout
import com.umrhsn.mmoire.ui.components.AppDialog
import com.umrhsn.mmoire.ui.components.AppDropdownItem
import com.umrhsn.mmoire.ui.components.AppHeader
import com.umrhsn.mmoire.ui.components.AppHeaderIcon
import com.umrhsn.mmoire.ui.components.FloatingPill
import com.umrhsn.mmoire.ui.components.FloatingVerticalPill
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
import compose.icons.evaicons.outline.Flip
import compose.icons.evaicons.outline.Flip2
import compose.icons.evaicons.outline.Folder
import compose.icons.evaicons.outline.Grid
import compose.icons.evaicons.outline.Image
import compose.icons.evaicons.outline.Layers
import compose.icons.evaicons.outline.MoreVertical
import compose.icons.evaicons.outline.Navigation2
import compose.icons.evaicons.outline.People
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

    val isTablet = config.smallestScreenWidthDp >= 500
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Foldables are large displays (isTablet) but they aren't "tall" (Expanded height) when opened.
    // Real tablets in portrait are "tall" (Expanded height).
    val isTallScreen = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Expanded
    val isFoldableOpened = isTablet && !isTallScreen

    val treatAsLandscape = isLandscape || isFoldableOpened

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
        if (treatAsLandscape && isTablet && !uiState.isTwoPlayerMode) {
            // Large Display Flanking Panels Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                LeftIconActionPanel(
                    onRefreshClicked = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.refreshGame()
                    },
                    onSizeClicked = { showSizeDialog = true },
                    onCreateClicked = { showCreateSelectionDialog = true },
                    onBrowseClicked = onBrowseClicked,
                    onSettingsClicked = onSettingsClicked,
                    onHelpClicked = { viewModel.startTutorial() }
                )

                Box(modifier = Modifier.weight(1f)) {
                    GameBoardArea(
                        uiState = uiState,
                        onCardClicked = onCardClicked,
                        haptic = haptic
                    )
                }

                RightStatsIconPanel(uiState = uiState)
            }
        } else {
            // Phone or Tablet Portrait Layout
            Column(modifier = Modifier.fillMaxSize()) {
                MainHeader(
                    uiState = uiState,
                    haptic = haptic,
                    viewModel = viewModel,
                    isTablet = isTablet,
                    treatAsLandscape = treatAsLandscape,
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
                            treatAsLandscape = treatAsLandscape
                        )
                    } else {
                        GameBoardArea(
                            uiState = uiState,
                            onCardClicked = onCardClicked,
                            haptic = haptic
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
                    StatsSection(uiState = uiState)
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
    treatAsLandscape: Boolean
) {
    val layout = uiState.twoPlayerLayout

    val isHorizontalSplit = !treatAsLandscape
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    if (isHorizontalSplit) {
        // Mode: Portrait split (Always stacked Top/Bottom)
        Column(modifier = Modifier.fillMaxSize()) {
            // Player 1 Area (Top Slot)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        // For tablets in portrait, force 180 (Opposite)
                        // For phones, they are locked to portrait, so they also always get 180.
                        rotationZ = 180f
                    }
            ) {
                PlayerRaceHalf(
                    playerNumber = 1,
                    game = uiState.memoryGameP1,
                    boardSize = uiState.boardSize,
                    timeSeconds = uiState.timerSeconds,
                    onCardClicked = { onCardClicked(it, 1) },
                    isTablet = isTablet,
                    statsAtBottom = false,
                    useCompactHeader = !isTablet,
                    appColorTheme = uiState.appColorTheme,
                    isCardTintEnabled = uiState.isCardTintEnabled
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 48.dp)
            )
            // Player 2 Area (Bottom Slot)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                PlayerRaceHalf(
                    playerNumber = 2,
                    game = uiState.memoryGameP2,
                    boardSize = uiState.boardSize,
                    timeSeconds = uiState.timerSecondsP2,
                    onCardClicked = { onCardClicked(it, 2) },
                    isTablet = isTablet,
                    statsAtBottom = false,
                    useCompactHeader = !isTablet,
                    appColorTheme = uiState.appColorTheme,
                    isCardTintEnabled = uiState.isCardTintEnabled
                )
            }
        }
    } else {
        // Landscape vertical split
        Row(modifier = Modifier.fillMaxSize()) {
            val p1Slot = @Composable {
                // Player 1 Area (Left Slot)
                Box(modifier = Modifier.weight(1f)) {
                    when (layout) {
                        TwoPlayerLayout.FACE_TO_FACE -> {
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val w = this.maxWidth
                                val h = this.maxHeight
                                Box(
                                    modifier = Modifier
                                        .size(h, w)
                                        .align(Alignment.Center)
                                        .graphicsLayer { rotationZ = 90f }
                                ) {
                                    PlayerRaceHalf(
                                        playerNumber = 1,
                                        game = uiState.memoryGameP1,
                                        boardSize = uiState.boardSize,
                                        timeSeconds = uiState.timerSeconds,
                                        onCardClicked = { onCardClicked(it, 1) },
                                        isTablet = isTablet,
                                        statsAtBottom = false, // Top rotates to divider
                                        useCompactHeader = true,
                                        appColorTheme = uiState.appColorTheme,
                                        isCardTintEnabled = uiState.isCardTintEnabled
                                    )
                                }
                            }
                        }

                        else -> {
                            // SIDE_BY_SIDE or OPPOSITE
                            PlayerRaceHalf(
                                playerNumber = 1,
                                game = uiState.memoryGameP1,
                                boardSize = uiState.boardSize,
                                timeSeconds = uiState.timerSeconds,
                                onCardClicked = { onCardClicked(it, 1) },
                                isTablet = isTablet,
                                statsAtBottom = false,
                                appColorTheme = uiState.appColorTheme,
                                isCardTintEnabled = uiState.isCardTintEnabled
                            )
                        }
                    }
                }
            }

            val p2Slot = @Composable {
                // Player 2 Area (Right Slot)
                Box(modifier = Modifier.weight(1f)) {
                    when (layout) {
                        TwoPlayerLayout.FACE_TO_FACE -> {
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val w = this.maxWidth
                                val h = this.maxHeight
                                Box(
                                    modifier = Modifier
                                        .size(h, w)
                                        .align(Alignment.Center)
                                        .graphicsLayer { rotationZ = -90f }
                                ) {
                                    PlayerRaceHalf(
                                        playerNumber = 2,
                                        game = uiState.memoryGameP2,
                                        boardSize = uiState.boardSize,
                                        timeSeconds = uiState.timerSecondsP2,
                                        onCardClicked = { onCardClicked(it, 2) },
                                        isTablet = isTablet,
                                        statsAtBottom = false, // Top rotates to divider
                                        useCompactHeader = true,
                                        appColorTheme = uiState.appColorTheme,
                                        isCardTintEnabled = uiState.isCardTintEnabled
                                    )
                                }
                            }
                        }

                        TwoPlayerLayout.OPPOSITE -> {
                            // Opposite mode rotates the entire right slot by 180 deg
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationZ = 180f }) {
                                PlayerRaceHalf(
                                    playerNumber = 2,
                                    game = uiState.memoryGameP2,
                                    boardSize = uiState.boardSize,
                                    timeSeconds = uiState.timerSecondsP2,
                                    onCardClicked = { onCardClicked(it, 2) },
                                    isTablet = isTablet,
                                    statsAtBottom = false,
                                    appColorTheme = uiState.appColorTheme,
                                    isCardTintEnabled = uiState.isCardTintEnabled
                                )
                            }
                        }

                        else -> {
                            // SIDE_BY_SIDE
                            PlayerRaceHalf(
                                playerNumber = 2,
                                game = uiState.memoryGameP2,
                                boardSize = uiState.boardSize,
                                timeSeconds = uiState.timerSecondsP2,
                                onCardClicked = { onCardClicked(it, 2) },
                                isTablet = isTablet,
                                statsAtBottom = false,
                                appColorTheme = uiState.appColorTheme,
                                isCardTintEnabled = uiState.isCardTintEnabled
                            )
                        }
                    }
                }
            }

            if (isRtl) {
                p2Slot()
                VerticalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 48.dp)
                )
                p1Slot()
            } else {
                p1Slot()
                VerticalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 48.dp)
                )
                p2Slot()
            }
        }
    }
}

@Composable
private fun MainHeader(
    uiState: MainUiState,
    haptic: HapticFeedback,
    viewModel: MainViewModel,
    isTablet: Boolean,
    treatAsLandscape: Boolean,
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
            if (uiState.isTwoPlayerMode && isTablet && treatAsLandscape) {
                // Layout Toggle for 2-player mode (Only on tablets in landscape/foldables)
                AppHeaderIcon(
                    icon = when (uiState.twoPlayerLayout) {
                        TwoPlayerLayout.FACE_TO_FACE -> EvaIcons.Outline.Flip2
                        TwoPlayerLayout.SIDE_BY_SIDE -> EvaIcons.Outline.People
                        TwoPlayerLayout.OPPOSITE -> EvaIcons.Outline.Flip
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

            if (isTablet) {
                AppHeaderIcon(
                    icon = EvaIcons.Outline.PlusCircle,
                    contentDescription = stringResource(R.string.create_game),
                    onClick = showCreateSelectionDialog
                )
                AppHeaderIcon(
                    icon = EvaIcons.Outline.Folder,
                    contentDescription = stringResource(R.string.load_game),
                    onClick = onBrowseClicked
                )
                AppHeaderIcon(
                    icon = EvaIcons.Outline.Settings,
                    contentDescription = stringResource(R.string.settings),
                    onClick = onSettingsClicked
                )
                AppHeaderIcon(
                    icon = EvaIcons.Outline.QuestionMarkCircle,
                    contentDescription = stringResource(R.string.help),
                    onClick = { viewModel.startTutorial() }
                )
            } else {
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
    haptic: HapticFeedback
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
                appColorTheme = uiState.appColorTheme,
                isCardTintEnabled = uiState.isCardTintEnabled
            )
        }
    }
}

@Composable
private fun StatsSection(uiState: MainUiState) {
    val gameP1 = uiState.memoryGameP1 ?: return

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

@Composable
private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return if (mins == 0L) {
        stringResource(R.string.duration_seconds, secs)
    } else {
        stringResource(R.string.duration_minutes_seconds, mins, secs)
    }
}

@Composable
private fun PlayerStatsRow(
    playerNumber: Int,
    game: MemoryGame?,
    boardSize: BoardSize,
    timeSeconds: Long,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Player Info Row (Aligned to Start)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCompact) 28.dp else 32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    EvaIcons.Outline.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(if (isCompact) 16.dp else 18.dp)
                )
            }
            Spacer(modifier = Modifier.width(if (isCompact) 6.dp else 8.dp))
            Text(
                text = stringResource(R.string.player_n, playerNumber),
                style = if (isCompact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black
            )
        }

        // Stats Badges Row (Aligned to End)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                if (isCompact) 4.dp else 8.dp,
                Alignment.End
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
}

@Composable
private fun PlayerRaceHalf(
    playerNumber: Int,
    game: MemoryGame?,
    boardSize: BoardSize,
    timeSeconds: Long,
    onCardClicked: (Int) -> Unit,
    isTablet: Boolean = false,
    statsAtBottom: Boolean = false,
    useCompactHeader: Boolean = false,
    appColorTheme: AppColorTheme = AppColorTheme.DEFAULT,
    isCardTintEnabled: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = if (useCompactHeader) Alignment.CenterHorizontally else Alignment.Start
    ) {
        if (!statsAtBottom) {
            PlayerStatsRow(
                playerNumber = playerNumber,
                game = game,
                boardSize = boardSize,
                timeSeconds = timeSeconds,
                isCompact = useCompactHeader
            )
        }

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
                    appColorTheme = appColorTheme,
                    isCardTintEnabled = isCardTintEnabled
                )
            }
        }

        if (statsAtBottom) {
            PlayerStatsRow(
                playerNumber = playerNumber,
                game = game,
                boardSize = boardSize,
                timeSeconds = timeSeconds,
                isCompact = useCompactHeader
            )
        }
    }
}

@Composable
private fun LeftIconActionPanel(
    onRefreshClicked: () -> Unit,
    onSizeClicked: () -> Unit,
    onCreateClicked: () -> Unit,
    onBrowseClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onHelpClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingVerticalPill(modifier = modifier) {
        AppHeaderIcon(
            icon = EvaIcons.Outline.Refresh,
            contentDescription = stringResource(R.string.reset_game),
            onClick = onRefreshClicked
        )
        AppHeaderIcon(
            icon = EvaIcons.Outline.Grid,
            contentDescription = stringResource(R.string.change_size),
            onClick = onSizeClicked
        )
        AppHeaderIcon(
            icon = EvaIcons.Outline.PlusCircle,
            contentDescription = stringResource(R.string.create_game),
            onClick = onCreateClicked
        )
        AppHeaderIcon(
            icon = EvaIcons.Outline.Folder,
            contentDescription = stringResource(R.string.load_game),
            onClick = onBrowseClicked
        )
        AppHeaderIcon(
            icon = EvaIcons.Outline.Settings,
            contentDescription = stringResource(R.string.settings),
            onClick = onSettingsClicked
        )
        AppHeaderIcon(
            icon = EvaIcons.Outline.QuestionMarkCircle,
            contentDescription = stringResource(R.string.help),
            onClick = onHelpClicked
        )
    }
}

@Composable
private fun RightStatsIconPanel(
    uiState: MainUiState,
    modifier: Modifier = Modifier
) {
    val game = uiState.memoryGameP1 ?: return
    FloatingVerticalPill(modifier = modifier) {
        StatIconValue(
            icon = EvaIcons.Outline.Flash,
            value = game.getNumMoves().toString(),
            tooltipText = stringResource(R.string.moves_tracking_label)
        )
        StatIconValue(
            icon = EvaIcons.Outline.Layers,
            value = "${game.numPairsFound}/${uiState.boardSize.getNumPairs()}",
            tooltipText = stringResource(R.string.pairs_tracking_label)
        )
        StatIconValue(
            icon = EvaIcons.Outline.Clock,
            value = formatDuration(uiState.timerSeconds),
            tooltipText = stringResource(R.string.timer_tracking_label)
        )
        uiState.bestTime?.let {
            StatIconValue(
                icon = EvaIcons.Outline.Award,
                value = formatDuration(it),
                tooltipText = stringResource(R.string.best_time_tracking_label),
                contentColor = MaterialTheme.colorScheme.primary,
                useHighlightBackground = true
            )
        }
    }
}

@Composable
private fun StatIconValue(
    icon: ImageVector,
    value: String,
    tooltipText: String,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    useHighlightBackground: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (useHighlightBackground) contentColor.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tooltipText,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = if (useHighlightBackground) contentColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
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
