package com.yzk.githubclient.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yzk.githubclient.data.Repository
import com.yzk.githubclient.github.IGithubAccessService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @description viewmodel of search repos
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
data class SearchUiState(
    val searchQuery: String = "",
    val selectedLanguage: String? = null,
    val repos: List<Repository> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true
)

/**
 * ViewModel for the Search screen.
 * Handles searching repositories based on user query and language filters,
 * manages pagination, and holds the UI state ([SearchUiState]).
 *
 * @param repository The [GithubRepository] used for searching repositories.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val iGithubAccessService: IGithubAccessService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    /** StateFlow emitting the current [SearchUiState]. */
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    /** Updates the search query text in the UI state. */
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    /** Updates the selected language filter in the UI state. */
    fun updateSelectedLanguage(language: String?) {
        _uiState.value = _uiState.value.copy(selectedLanguage = language)
    }

    /**
     * Initiates a repository search based on the current query and language filter.
     * Handles both initial searches and loading more results for pagination.
     *
     * @param isLoadMore True if this call is for loading the next page, false for a new search.
     */
    fun searchRepos(isLoadMore: Boolean = false) {
        // Prevent concurrent load more or loading more when there are no more results
        if (isLoadMore && (!_uiState.value.hasMore || _uiState.value.isLoadingMore)) {
            return
        }

        // If it's a new search and the query is blank, clear results and return
        if (!isLoadMore && _uiState.value.searchQuery.isBlank()) {
            _uiState.value = _uiState.value.copy(
                repos = emptyList(),
                isLoading = false,
                isLoadingMore = false,
                error = null,
                page = 1,
                hasMore = true
            )
            return
        }

        // Determine the page number for the API call
        val currentPage = if (isLoadMore) _uiState.value.page else 1
        val perPage = 20 // Define items per page

        viewModelScope.launch {
            // Update loading state
            if (isLoadMore) {
                _uiState.value = _uiState.value.copy(isLoadingMore = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, page = 1)
            }

            try {
                // Construct the query string including language if provided
                val searchQuery = buildString {
                    append(_uiState.value.searchQuery)
                    if (!_uiState.value.selectedLanguage.isNullOrBlank()) {
                        append(" language:${_uiState.value.selectedLanguage}")
                    }
                }

                // Call the repository to search
                iGithubAccessService.searchRepositories(searchQuery, currentPage, perPage)
                    .collect { result ->
                        result.onSuccess { newRepos ->
                            val currentRepos = if (isLoadMore) _uiState.value.repos else emptyList()
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                repos = currentRepos + newRepos,
                                error = null,
                                // Assume more pages if we received a full page of results
                                hasMore = newRepos.size == perPage,
                                page = currentPage + 1
                            )
                        }.onFailure { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                error = e.message
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = e.message
                )
            }
        }
    }

    /** Convenience function to trigger loading the next page of results. */
    fun loadMore() {
        searchRepos(isLoadMore = true)
    }
}