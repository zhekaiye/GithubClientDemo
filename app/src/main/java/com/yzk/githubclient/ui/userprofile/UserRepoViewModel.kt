package com.yzk.githubclient.ui.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yzk.githubclient.data.Repository
import com.yzk.githubclient.github.IGithubAccessService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @description
 *
 * @author: yezhekai.256
 * @date: 5/25/25
 */
data class UserRepoUiState(
    val isLoading: Boolean = false,
    val repository: Repository? = null,
    val error: String? = null
)

@HiltViewModel
class UserRepoViewModel @Inject constructor(
    private val iGithubAccessService: IGithubAccessService
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserRepoUiState())
    val uiState: StateFlow<UserRepoUiState> = _uiState.asStateFlow()

    fun loadUserRepository(owner: String, repoName: String) {
        viewModelScope.launch {
            iGithubAccessService.getRepository(owner, repoName).onStart {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }.catch { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load repository data: ${e.message}")
            }.collect { repoResult ->
                val repo = repoResult.getOrNull()
                val repoError = if (repoResult.isFailure) repoResult.exceptionOrNull()?.message else null
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    repository = repo,
                    error = repoError
                )
            }
        }
    }
}