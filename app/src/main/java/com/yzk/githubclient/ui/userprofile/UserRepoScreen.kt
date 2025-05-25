package com.yzk.githubclient.ui.userprofile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yzk.githubclient.R
import com.yzk.githubclient.error.ErrorAlert
import com.yzk.githubclient.ui.detail.RepositoryCard

/**
 * @description
 *
 * @author: yezhekai.256
 * @date: 5/25/25
 */
@Composable
fun UserRepoScreen(
    owner: String,
    repoName: String,
    viewModel: UserRepoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToRaiseIssue: (owner: String, repoName: String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(owner, repoName) {
        viewModel.loadUserRepository(owner, repoName)
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.profile_view_all_repos), color = Color.Black) },
                backgroundColor = Color.White,
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.action_back),
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onNavigateToRaiseIssue(owner, repoName) }
                    ) {
                        Text(stringResource(id = R.string.repository_new_issue_button), color = MaterialTheme.colors.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Loading state
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                // Error state
                uiState.error != null -> {
                    ErrorAlert(
                        message = uiState.error ?: stringResource(id = R.string.error_unknown),
                        onRetry = { viewModel.loadUserRepository(owner, repoName) }
                    )
                }
                uiState.repository != null -> {
                    uiState.repository?.let {
                        RepositoryCard(it) { }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Text(stringResource(id = R.string.error_repo_load_failed))
                    }
                }
            }
        }
    }
}