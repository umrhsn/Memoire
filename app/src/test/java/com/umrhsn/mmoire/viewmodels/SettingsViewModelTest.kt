package com.umrhsn.mmoire.viewmodels

import app.cash.turbine.test
import com.umrhsn.mmoire.models.AppColorTheme
import com.umrhsn.mmoire.models.AppTheme
import com.umrhsn.mmoire.utils.LocaleManager
import com.umrhsn.mmoire.utils.PrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val prefs: PrefsManager = mock()
    private val localeManager: LocaleManager = mock()
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(prefs.isSoundEnabled()).thenReturn(true)
        whenever(prefs.getTheme()).thenReturn(AppTheme.SYSTEM)
        whenever(prefs.getColorTheme()).thenReturn(AppColorTheme.DEFAULT)
        whenever(prefs.isBackgroundTintEnabled()).thenReturn(true)
        whenever(prefs.isCardTintEnabled()).thenReturn(true)
        whenever(prefs.getLanguage()).thenReturn("en")
        whenever(prefs.themeFlow).thenReturn(MutableStateFlow(AppTheme.SYSTEM))
        whenever(prefs.colorThemeFlow).thenReturn(MutableStateFlow(AppColorTheme.DEFAULT))
        whenever(prefs.tintFlow).thenReturn(MutableStateFlow(true))
        whenever(prefs.cardTintFlow).thenReturn(MutableStateFlow(true))
        whenever(prefs.localeFlow).thenReturn(MutableStateFlow("en"))

        whenever(localeManager.getSelectedLanguageTag()).thenReturn("en")
        viewModel = SettingsViewModel(prefs, localeManager)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state reflects current preferences`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("en", state.currentLanguage)
            assertTrue(state.currentSoundEnabled)
            assertEquals(AppTheme.SYSTEM, state.currentTheme)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateLanguage shows restart dialog and confirmLanguageChange applies it`() =
        runTest(testDispatcher) {
            viewModel.updateLanguage("fr")
            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals("fr", state.pendingLanguage)
                assertTrue(state.showRestartDialog)

                viewModel.confirmLanguageChange()
                val nextState = awaitItem()
                assertEquals("fr", nextState.currentLanguage)
                assertEquals(false, nextState.showRestartDialog)
                verify(localeManager).applyLanguageTag("fr")
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `updateTheme changes state and prefs immediately`() = runTest(testDispatcher) {
        viewModel.updateTheme(AppTheme.DARK)
        verify(prefs).setTheme(AppTheme.DARK)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(AppTheme.DARK, state.currentTheme)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleSound changes state and prefs immediately`() = runTest(testDispatcher) {
        viewModel.toggleSound(false)
        verify(prefs).setSoundEnabled(false)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.currentSoundEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
