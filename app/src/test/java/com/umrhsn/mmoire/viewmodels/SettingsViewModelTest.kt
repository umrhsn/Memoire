package com.umrhsn.mmoire.viewmodels

import app.cash.turbine.test
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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val prefs: PrefsManager = mock()
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(prefs.getLanguage()).thenReturn("en")
        whenever(prefs.isSoundEnabled()).thenReturn(true)
        viewModel = SettingsViewModel(prefs)
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
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateLanguage changes temporary state but not prefs`() = runTest {
        viewModel.updateLanguage("fr")
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("fr", state.currentLanguage)
            verify(prefs, never()).setLanguage("fr")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `applyChanges saves settings to prefs`() = runTest {
        viewModel.updateLanguage("ar")
        viewModel.toggleSound(false)

        viewModel.applyChanges()

        verify(prefs).setLanguage("ar")
        verify(prefs).setSoundEnabled(false)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isSuccess)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
