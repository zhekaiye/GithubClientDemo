package com.yzk.githubclient.ui.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yzk.githubclient.data.Topic
import com.yzk.githubclient.github.IGithubAccessService
import com.yzk.githubclient.utils.Consts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @description
 *
 * @author: yezhekai.256
 * @date: 5/25/25
 */
data class TopicUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val topics: List<Topic> = emptyList(),
    val error: String? = null,
    val hasMore: Boolean = true,
    val page: Int = 1
)

@HiltViewModel
class TopicViewModel @Inject constructor(
    private val iGithubAccessService: IGithubAccessService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicUiState(isLoading = true))
    val uiState: StateFlow<TopicUiState> = _uiState.asStateFlow()

    init {
        loadTopics(isRefresh = false, isLoadMore = false)
    }

    private fun loadTopics(isRefresh: Boolean = false, isLoadMore: Boolean = false) {
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

            iGithubAccessService.fetchTopicList(page = currentPage, perPage = perPage)
                .collect { result ->
                    result.onSuccess { newRepos ->
                        val currentRepos = if (isRefresh || !isLoadMore) emptyList() else uiState.value.topics
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            topics = currentRepos + newRepos,
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

    fun refreshTopics() {
        loadTopics(isRefresh = true)
    }

    fun loadMoreTopics() {
        loadTopics(isLoadMore = true)
    }
}