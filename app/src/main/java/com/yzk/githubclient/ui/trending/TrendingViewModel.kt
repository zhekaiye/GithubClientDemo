package com.yzk.githubclient.ui.trending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yzk.githubclient.data.Repository
import com.yzk.githubclient.github.IGithubAccessService
import com.yzk.githubclient.utils.Consts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @description trending repos viewmodel
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
data class TrendingReposUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val trendingRepos: List<Repository> = emptyList(),
    val error: String? = null,
    val hasMore: Boolean = true,
    val page: Int = 1
)

@HiltViewModel
class TrendingViewModel @Inject constructor(
    private val iGithubAccessService: IGithubAccessService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrendingReposUiState(isLoading = true))
    val uiState: StateFlow<TrendingReposUiState> = _uiState.asStateFlow()

    init {
        loadTrendingRepos(isRefresh = false, isLoadMore = false)
    }

    private fun loadTrendingRepos(isRefresh: Boolean = false, isLoadMore: Boolean = false) {
        if (isLoadMore && (!uiState.value.hasMore || uiState.value.isLoadingMore)) {
            return
        }

        val currentPage = if (isRefresh) Consts.COUNT_PAGE else uiState.value.page
        val perPage = Consts.COUNT_PER_PAGE

        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isLoading = true, page = 1)
            } else if (isLoadMore) {
                _uiState.value = _uiState.value.copy(isLoadingMore = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }

            iGithubAccessService.fetchTrendingRepos(page = currentPage, perPage = perPage)
                .collect { result ->
                    result.onSuccess { newRepos ->
                        val currentRepos = if (isRefresh || !isLoadMore) emptyList() else uiState.value.trendingRepos
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            trendingRepos = currentRepos + newRepos,
                            error = null,
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
        }
    }

    fun refreshTrendingRepos() {
        loadTrendingRepos(isRefresh = true)
    }

    fun loadMoreTrendingRepos() {
        loadTrendingRepos(isLoadMore = true)
    }
}