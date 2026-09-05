package com.umrhsn.mmoire.viewmodels

import app.cash.turbine.test
import com.umrhsn.mmoire.utils.LocaleManager
import com.umrhsn.mmoire.utils.PrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        whenever(prefs.getTheme()).thenReturn(com.umrhsn.mmoire.models.AppTheme.SYSTEM)
        whenever(localeManager.getSelectedLanguageTag()).thenReturn("en")
        viewModel = SettingsViewModel(prefs, localeManager)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state reflects current preferences`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("en", state.currentLanguage)
            assertTrue(state.currentSoundEnabled)
            assertEquals(com.umrhsn.mmoire.models.AppTheme.SYSTEM, state.currentTheme)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateLanguage changes state and prefs immediately`() = runTest {
        viewModel.updateLanguage("fr")
        verify(localeManager).applyLanguageTag("fr")
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("fr", state.currentLanguage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateTheme changes state and prefs immediately`() = runTest {
        viewModel.updateTheme(com.umrhsn.mmoire.models.AppTheme.DARK)
        verify(prefs).setTheme(com.umrhsn.mmoire.models.AppTheme.DARK)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(com.umrhsn.mmoire.models.AppTheme.DARK, state.currentTheme)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleSound changes state and prefs immediately`() = runTest {
        viewModel.toggleSound(false)
        verify(prefs).setSoundEnabled(false)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.currentSoundEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
