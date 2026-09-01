package com.umrhsn.mmoire.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.umrhsn.mmoire.R
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
        title = stringResource(R.string.tut_welcome_title),
        description = stringResource(R.string.tut_welcome_desc),
        icon = EvaIcons.Outline.SmilingFace
    ),
    TutorialStep(
        title = stringResource(R.string.tut_hub_title),
        description = stringResource(R.string.tut_hub_desc),
        icon = EvaIcons.Outline.Layout,
        anchorKey = "header_actions"
    ),
    TutorialStep(
        title = stringResource(R.string.tut_board_title),
        description = stringResource(R.string.tut_board_desc),
        icon = EvaIcons.Outline.Grid,
        anchorKey = "game_board"
    ),
    TutorialStep(
        title = stringResource(R.string.tut_stats_title),
        description = stringResource(R.string.tut_stats_desc),
        icon = EvaIcons.Outline.Flash,
        anchorKey = "stats_tracking"
    ),
    TutorialStep(
        title = stringResource(R.string.tut_timer_title),
        description = stringResource(R.string.tut_timer_desc),
        icon = EvaIcons.Outline.Clock,
        anchorKey = "timer_tracking"
    ),
    TutorialStep(
        title = stringResource(R.string.tut_help_title),
        description = stringResource(R.string.tut_help_desc),
        icon = EvaIcons.Outline.QuestionMarkCircle,
        anchorKey = "help_action"
    )
)
