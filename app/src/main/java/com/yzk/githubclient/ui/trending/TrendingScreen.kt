package com.yzk.githubclient.ui.trending

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.yzk.githubclient.R
import com.yzk.githubclient.error.ErrorAlert
import com.yzk.githubclient.ui.navigation.AppScreen
import com.yzk.githubclient.ui.detail.RepositoryCard
import java.net.URLEncoder

/**
 * @description trending repos screen
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
object TrendingScreenTags {
    const val LOADING_INDICATOR = "TrendingLoadingIndicator"
    const val REPO_LIST = "TrendingRepoList"
    const val LOAD_MORE_INDICATOR = "TrendingLoadMoreIndicator"
}

@Composable
fun TrendingScreen(
    viewModel: TrendingViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isLoading && !uiState.isLoadingMore)

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5 && !uiState.isLoading
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !uiState.isLoadingMore && uiState.hasMore) {
            viewModel.loadMoreTrendingRepos()
        }
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.refreshTrendingRepos() },
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading && uiState.trendingRepos.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag(TrendingScreenTags.LOADING_INDICATOR)
                    )
                }
                uiState.error != null && uiState.trendingRepos.isEmpty() -> {
                    ErrorAlert(
                        message = stringResource(R.string.app_name, uiState.error ?: ""),
                        onRetry = { viewModel.refreshTrendingRepos() }
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(TrendingScreenTags.REPO_LIST),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.trendingRepos) { repo ->
                            RepositoryCard(repo = repo, onClick = {
                                navController.navigate(AppScreen.Repository.createRoute(
                                    repo.owner.login,
                                    repo.name,
                                    URLEncoder.encode(repo.htmlUrl, "UTF-8")
                                ))
                            })
                        }

                        item {
                            if (uiState.isLoadingMore) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag(TrendingScreenTags.LOAD_MORE_INDICATOR),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (uiState.error != null && uiState.trendingRepos.isNotEmpty()) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) {
                    Text(text = stringResource(R.string.error_load_failed, uiState.error ?: ""))
                }
            }
        }
    }
}