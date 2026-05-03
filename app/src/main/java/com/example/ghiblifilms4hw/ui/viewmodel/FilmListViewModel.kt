package com.example.ghiblifilms4hw.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ghiblifilms4hw.data.Repository
import com.example.ghiblifilms4hw.ui.state.FilmListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilmListViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FilmListUiState>(FilmListUiState.Loading)
    val uiState: StateFlow<FilmListUiState> = _uiState

    init {
        loadFilms()
    }

    fun loadFilms() {
        viewModelScope.launch {
            _uiState.value = FilmListUiState.Loading
            repository.refreshFilms().fold(
                onSuccess = {
                    repository.getAllFilms()
                        .catch { e ->
                            _uiState.value = FilmListUiState.Error(e.message ?: "Unknown error")
                        }
                        .collect { films ->
                            if (films.isEmpty()) {
                                _uiState.value = FilmListUiState.Empty
                            } else {
                                val currentState = _uiState.value
                                if (currentState is FilmListUiState.Success) {
                                    _uiState.value = currentState.copy(films = films)
                                } else {
                                    _uiState.value = FilmListUiState.Success(films = films)
                                }
                            }
                        }
                },
                onFailure = { e ->
                    _uiState.value = FilmListUiState.Error(e.message ?: "Failed to load films")
                }
            )
        }
    }

    fun updateSearchQuery(query: String) {
        val currentState = _uiState.value
        if (currentState is FilmListUiState.Success) {
            _uiState.value = currentState.copy(searchQuery = query)
        }
    }

    fun updateDirectorFilter(director: String?) {
        val currentState = _uiState.value
        if (currentState is FilmListUiState.Success) {
            _uiState.value = currentState.copy(selectedDirector = director)
        }
    }

    fun toggleFilters() {
        val currentState = _uiState.value
        if (currentState is FilmListUiState.Success) {
            _uiState.value = currentState.copy(showFilters = !currentState.showFilters)
        }
    }

    fun resetFilters() {
        val currentState = _uiState.value
        if (currentState is FilmListUiState.Success) {
            _uiState.value = currentState.copy(searchQuery = "", selectedDirector = null)
        }
    }

    fun toggleFavorite(filmId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(filmId)
            val currentState = _uiState.value
            if (currentState is FilmListUiState.Success) {
                repository.getAllFilms().collect { updatedFilms ->
                    _uiState.value = currentState.copy(films = updatedFilms)
                }
            }
        }
    }
}