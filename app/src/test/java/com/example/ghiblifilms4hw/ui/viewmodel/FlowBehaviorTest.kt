package com.example.ghiblifilms4hw.ui.viewmodel

import app.cash.turbine.test
import com.example.ghiblifilms4hw.MainDispatcherRule
import com.example.ghiblifilms4hw.data.Repository
import com.example.ghiblifilms4hw.model.Film
import com.example.ghiblifilms4hw.ui.state.FilmListUiState
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class FlowBehaviorTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var repository: Repository

    private lateinit var viewModel: FilmListViewModel

    private val sampleFilms = listOf(
        Film(id = "1", title = "Film 1"),
        Film(id = "2", title = "Film 2")
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun completeEmissionSequenceShouldBeLoadingThenEmptyThenSuccess() = runTest {
        val filmFlow = MutableStateFlow<List<Film>>(emptyList())
        coEvery { repository.getAllFilms() } returns filmFlow
        coEvery { repository.refreshFilms() } returns Result.success(Unit)

        viewModel = FilmListViewModel(repository)

        viewModel.uiState.test {
            assertEquals(FilmListUiState.Loading, awaitItem())

            filmFlow.value = emptyList()
            advanceUntilIdle()
            assertEquals(FilmListUiState.Empty, awaitItem())

            filmFlow.value = sampleFilms
            advanceUntilIdle()
            val successState = awaitItem() as FilmListUiState.Success
            assertEquals(2, successState.films.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun noDuplicateEmissionsForSameState() = runTest {
        val filmFlow = MutableStateFlow<List<Film>>(emptyList())
        coEvery { repository.getAllFilms() } returns filmFlow
        coEvery { repository.refreshFilms() } returns Result.success(Unit)

        viewModel = FilmListViewModel(repository)

        var emissionCount = 0
        viewModel.uiState.test {
            awaitItem()
            emissionCount++

            filmFlow.value = emptyList()
            advanceUntilIdle()
            awaitItem()
            emissionCount++

            filmFlow.value = emptyList()
            advanceUntilIdle()

            expectNoEvents()
            assertEquals(2, emissionCount)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun newSubscriberReceivesCurrentStateImmediately() = runTest {
        coEvery { repository.getAllFilms() } returns flowOf(sampleFilms)
        coEvery { repository.refreshFilms() } returns Result.success(Unit)

        viewModel = FilmListViewModel(repository)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem() as FilmListUiState.Success
            assertEquals(2, state.films.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchQueryUpdatesCorrectly() = runTest {
        coEvery { repository.getAllFilms() } returns flowOf(sampleFilms)
        coEvery { repository.refreshFilms() } returns Result.success(Unit)

        viewModel = FilmListViewModel(repository)
        advanceUntilIdle()

        viewModel.updateSearchQuery("Film 1")

        val state = viewModel.uiState.value as FilmListUiState.Success
        assertEquals("Film 1", state.searchQuery)
        assertEquals(1, state.filteredFilms.size)
    }
}