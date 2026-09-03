package com.umrhsn.mmoire.viewmodels

import app.cash.turbine.test
import com.umrhsn.mmoire.repository.GameRepository
import com.umrhsn.mmoire.repository.UserImageListWithId
import com.umrhsn.mmoire.utils.SoundManager
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
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class BrowseViewModelTest {

    private val repository: GameRepository = mock()
    private val soundManager: SoundManager = mock()
    private lateinit var viewModel: BrowseViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testGames = listOf(
        UserImageListWithId("Banana", listOf("u1"), createdAt = 100),
        UserImageListWithId("Apple", listOf("u1", "u2"), createdAt = 200),
        UserImageListWithId("Cherry", listOf("u1", "u2", "u3"), createdAt = 50)
    )

    @BeforeEach
    fun setUp() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        whenever(repository.getAllLocalGames()).thenReturn(testGames)
        viewModel = BrowseViewModel(repository, soundManager)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads and sorts by latest`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(3, state.games.size)
            // Default sort LATEST: 200, 100, 50 -> Apple, Banana, Cherry
            assertEquals("Apple", state.filteredGames[0].name)
            assertEquals("Banana", state.filteredGames[1].name)
            assertEquals("Cherry", state.filteredGames[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search filters games`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // Initial
            viewModel.onSearchQueryChanged("app")
            val state = awaitItem()
            assertEquals(1, state.filteredGames.size)
            assertEquals("Apple", state.filteredGames[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `sorting by name works`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // Initial
            viewModel.onSortOrderChanged(SortOrder.NAME_ASC)
            val state = awaitItem()
            assertEquals("Apple", state.filteredGames[0].name)
            assertEquals("Banana", state.filteredGames[1].name)
            assertEquals("Cherry", state.filteredGames[2].name)

            viewModel.onSortOrderChanged(SortOrder.NAME_DESC)
            val stateDesc = awaitItem()
            assertEquals("Cherry", stateDesc.filteredGames[0].name)
            assertEquals("Banana", stateDesc.filteredGames[1].name)
            assertEquals("Apple", stateDesc.filteredGames[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `sorting by pairs count works`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // Initial
            viewModel.onSortOrderChanged(SortOrder.PAIRS_COUNT)
            val state = awaitItem()
            // Cherry (3), Apple (2), Banana (1)
            assertEquals("Cherry", state.filteredGames[0].name)
            assertEquals("Apple", state.filteredGames[1].name)
            assertEquals("Banana", state.filteredGames[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteGame calls repository and reloads`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // Initial

            // Mock new list after delete
            whenever(repository.getAllLocalGames()).thenReturn(testGames.take(2))

            viewModel.deleteGame("Cherry")

            // Wait for reload
            var lastState = awaitItem()
            while (lastState.games.size == 3) {
                lastState = awaitItem()
            }

            assertEquals(2, lastState.games.size)
            assertTrue(lastState.games.none { it.name == "Cherry" })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
