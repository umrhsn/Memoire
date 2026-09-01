package com.umrhsn.mmoire.ui.components

import androidx.compose.runtime.Composable
import compose.icons.EvaIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.Clock
import compose.icons.evaicons.outline.Flash
import compose.icons.evaicons.outline.Grid
import compose.icons.evaicons.outline.Layout
import compose.icons.evaicons.outline.QuestionMarkCircle
import compose.icons.evaicons.outline.SmilingFace

@Composable
fun getMainTutorialSteps(): List<TutorialStep> = listOf(
    TutorialStep(
        title = "Welcome to Mémoire! 🏺",
        description = "A premium memory challenge designed for focus and speed. Let's show you around!",
        icon = EvaIcons.Outline.SmilingFace
    ),
    TutorialStep(
        title = "The Control Hub",
        description = "Use these icons to reset the game, change board levels, create your own boards, or browse your saved library.",
        icon = EvaIcons.Outline.Layout,
        anchorKey = "header_actions"
    ),
    TutorialStep(
        title = "Memory Board",
        description = "This is where the magic happens. Tap cards to flip them and find matching pairs.",
        icon = EvaIcons.Outline.Grid,
        anchorKey = "game_board"
    ),
    TutorialStep(
        title = "Live Performance",
        description = "Track your moves and pairs here. Try to find the most efficient path to victory!",
        icon = EvaIcons.Outline.Flash,
        anchorKey = "stats_tracking"
    ),
    TutorialStep(
        title = "Time & Records",
        description = "Keep an eye on the timer! Your best times are saved automatically for each board size.",
        icon = EvaIcons.Outline.Clock,
        anchorKey = "timer_tracking"
    ),
    TutorialStep(
        title = "Help Anytime",
        description = "Need a refresher? Tap the '?' icon anytime to see this guide again.",
        icon = EvaIcons.Outline.QuestionMarkCircle,
        anchorKey = "help_action"
    )
)
