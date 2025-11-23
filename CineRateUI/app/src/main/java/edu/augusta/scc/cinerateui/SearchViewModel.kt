package edu.augusta.scc.cinerateui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val errorMessage: String? = null
)

class SearchViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    var uiState: SearchUiState = SearchUiState()
        private set

    init {
        // load initial list
        loadPopular()
    }

    private fun loadPopular() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            val result = repository.getPopularMovies()
            uiState = result.fold(
                onSuccess = { movies -> uiState.copy(isLoading = false, movies = movies) },
                onFailure = { ex -> uiState.copy(isLoading = false, errorMessage = ex.message) }
            )
        }
    }

    fun onQueryChange(newQuery: String) {
        uiState = uiState.copy(query = newQuery)
    }

    fun onSearch() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            val result = repository.searchMovies(uiState.query)
            uiState = result.fold(
                onSuccess = { movies -> uiState.copy(isLoading = false, movies = movies) },
                onFailure = { ex -> uiState.copy(isLoading = false, errorMessage = ex.message) }
            )
        }
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun factory(repository: MovieRepository) = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return SearchViewModel(repository) as T
            }
        }
    }
}