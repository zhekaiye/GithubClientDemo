package com.yzk.githubclient.ui.login

import android.util.Log
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
 * @description viewModel for login state
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */

sealed class AuthState {
    object UnInitState : AuthState() // init state
    object LoggingState : AuthState() // logging state
    object LoggedInState : AuthState() // logged in state
    data class LoginFailState(val error: String) : AuthState() // login fail state with error msg
    object LoggedOutState : AuthState() // logged out state
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val iGithubService: IGithubAccessService
) : ViewModel() {

    private val TAG = "LoginViewModel"

    private val _authState = MutableStateFlow<AuthState>(AuthState.UnInitState)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            _authState.value = if (iGithubService.checkAccessTokenExists()) {
                AuthState.LoggedInState
            } else {
                AuthState.LoggedOutState
            }
        }
    }

    fun fetchAccessToken(code: String) {
        viewModelScope.launch {
            _authState.value = AuthState.LoggingState
            iGithubService.fetchAccessToken(code)
                .collect { result ->
                    result.onSuccess { token ->
                        _authState.value = AuthState.LoggedInState
                        Log.i(TAG, "fetch access token success, result: $token")
                    }.onFailure { e ->
                        _authState.value = AuthState.LoginFailState("${e.message}")
                        Log.e(TAG, "fetch access token failed", e)
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            iGithubService.clearAccessToken()
            _authState.value = AuthState.LoggedOutState
        }
    }
}