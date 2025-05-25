package com.yzk.githubclient.ui.userprofile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yzk.githubclient.data.Repository
import com.yzk.githubclient.data.UserProfile
import com.yzk.githubclient.github.IGithubAccessService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @description view model of user profile
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
data class UserProfileUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val pinnedRepos: List<Repository> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val iGithubAccessService: IGithubAccessService
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState(isLoading = true))
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    /**
     * Loads the authenticated user's profile information.
     * On success, proceeds to load pinned repositories.
     * Updates the [uiState] with loading status, data, and errors.
     */
    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = UserProfileUiState(isLoading = true) // Start fresh on load/reload
            iGithubAccessService.getUserProfile()
                // No need for onStart here as we set isLoading=true above
                .catch { e ->
                    // Catch exceptions during the user profile flow itself
                    _uiState.value = UserProfileUiState(isLoading = false, error = e.message ?: "Unknown error loading profile")
                    // Don't proceed to load pinned repos if user profile failed
                }
                .collect { result ->
                    result.onSuccess { user ->
                        // User loaded successfully, update state (isLoading will be handled by pinned repo load)
                        _uiState.value = UserProfileUiState(isLoading = true, userProfile = user) // Keep loading true while fetching pinned repos
                        // Load pinned repos now that we have the username
                        loadRecentRepos(user.login)
                    }.onFailure { e ->
                        // Handle failure from the API response for user profile
                        _uiState.value = UserProfileUiState(isLoading = false, error = e.message ?: "Failed to load profile")
                        // Don't proceed to load pinned repos if user profile failed
                    }
                }
        }
    }

    /**
     * Loads the user's recent repositories (or recently pushed, see note).
     * Updates the [uiState], preserving existing user data and handling loading/error states.
     *
     * Note: Relies on `repository.getRecentlyPushedRepos` which might actually fetch recently pushed repos.
     * Verify the implementation in [GithubRepository] and [GithubApiService].
     *
     * @param username The login name of the user whose recent repos should be loaded.
     */
    private fun loadRecentRepos(username: String) {
        viewModelScope.launch {
            // Fetch recently pushed repos (method name corrected)
            iGithubAccessService.getRecentlyPushedRepos(username)
                .catch { e ->
                    // Catch exceptions during the repo flow
                    Log.e("ProfileViewModel", "Error loading recently pushed repos (catch)", e)
                    // Keep existing user data, update error and loading status
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error loading recently pushed repos")
                    // TODO: Consider combining errors if profile also had an error?
                }
                .collect { result ->
                    result.onSuccess { repos ->
                        // Repos loaded successfully, update state
                        _uiState.value = _uiState.value.copy(isLoading = false, pinnedRepos = repos, error = null) // Clear error on success
                    }.onFailure { e ->
                        // Handle failure from the API response
                        Log.e("ProfileViewModel", "Error loading recently pushed repos (failure)", e)
                        // Keep existing user data, update error and loading status
                        _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to load recently pushed repos")
                        // TODO: Consider combining errors if profile also had an error?
                    }
                }
        }
    }
}