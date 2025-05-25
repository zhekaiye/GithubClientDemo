package com.yzk.githubclient.issue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yzk.githubclient.github.IGithubAccessService
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

data class IssueUiState(
    val isLoading: Boolean = false
)

@HiltViewModel
class IssueViewModel @Inject constructor(
    private val iGithubAccessService: IGithubAccessService
) : ViewModel() {

    private val _uiState = MutableStateFlow(IssueUiState())
    val uiState: StateFlow<IssueUiState> = _uiState.asStateFlow()

    fun createNewIssue(
        owner: String,
        repo: String,
        title: String,
        body: String?,
        onSuccess: (issueNumber: Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            iGithubAccessService.createNewIssue(owner, repo, title, body)
                .collect { result ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    result.fold(
                        onSuccess = { issue ->
                            onSuccess(issue.number)
                        },
                        onFailure = { error ->
                            onError(error.message ?: "Failed to create issue")
                        }
                    )
                }
        }
    }
}