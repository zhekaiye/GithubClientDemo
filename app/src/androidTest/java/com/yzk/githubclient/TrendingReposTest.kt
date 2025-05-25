package com.yzk.githubclient

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yzk.githubclient.data.Issue
import com.yzk.githubclient.data.Repository
import com.yzk.githubclient.data.Topic
import com.yzk.githubclient.data.UserProfile
import com.yzk.githubclient.error.ErrorAlertTags
import com.yzk.githubclient.github.IGithubAccessService
import com.yzk.githubclient.inject.HttpClientModule
import com.yzk.githubclient.ui.trending.TrendingScreenTags
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @description
 *
 * @author: yezhekai.256
 * @date: 5/25/25
 */

object TestConfig {
    // Test timeouts and delays
    const val LOADING_TIMEOUT = 5000L
    const val SCROLL_DELAY = 300L
    const val NETWORK_DELAY = 1000L
    const val LOAD_MORE_DELAY = 2000L

    // Pagination
    const val ITEMS_PER_PAGE = 20
    const val SCROLL_REPEAT_COUNT = 3

    // Test state
    var shouldFail = false

    // Test data generators
    private fun createMockUser(index: Int) = UserProfile(
        id = index.toLong(),
        login = "user$index",
        avatarUrl = "https://github.com/user$index.png"
    )

    fun createMockRepos(): List<Repository> = List(ITEMS_PER_PAGE) { index ->
        Repository(
            id = index.toLong(),
            name = "Repo $index",
            fullName = "Owner/Repo$index",
            description = "Description $index",
            owner = createMockUser(index),
            stargazersCount = index * 100,
            forksCount = index * 50,
            language = "Kotlin",
            htmlUrl = "https://github.com/owner$index/repo$index"
        )
    }
}

/**
 * UI tests for the Popular Repositories screen.
 * Tests various states and interactions of the screen including:
 * - Initial loading state
 * - Successful data loading
 * - Error handling
 * - Load more functionality
 */
@HiltAndroidTest
@UninstallModules(HttpClientModule::class)
@RunWith(AndroidJUnit4::class)
class TrendingReposTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var mockGithubAccessService: IGithubAccessService

    @Before
    fun setUp() {
        TestConfig.shouldFail = false
        hiltRule.inject()
    }

    @Test
    fun loadingIndicator_isVisible_whenScreenLaunches() {
        // Wait for loading indicator to appear
        waitForNodeByTag(TrendingScreenTags.LOADING_INDICATOR)

        // Verify loading state
        composeTestRule.onNodeWithTag(TrendingScreenTags.LOADING_INDICATOR).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TrendingScreenTags.REPO_LIST).assertDoesNotExist()
    }

    @Test
    fun repoList_isDisplayed_whenDataLoadedSuccessfully() {
        // Wait for data to load
        waitForNodeByTag(TrendingScreenTags.REPO_LIST)

        // Verify successful loading state
        composeTestRule.onNodeWithTag(TrendingScreenTags.LOADING_INDICATOR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TrendingScreenTags.REPO_LIST).assertIsDisplayed()
    }

    @Test
    fun errorMessage_isDisplayed_whenLoadingFails() {
        TestConfig.shouldFail = true

        // Wait for error container to appear
        waitForNodeByTag(ErrorAlertTags.CONTAINER)

        // Verify error state
        composeTestRule.onNodeWithTag(ErrorAlertTags.MESSAGE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ErrorAlertTags.BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TrendingScreenTags.LOADING_INDICATOR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TrendingScreenTags.REPO_LIST).assertDoesNotExist()
    }

    @Test
    fun loadMoreIndicator_isDisplayed_whenLoadingMoreData() {
        // Wait for initial data to load
        waitForNodeByTag(TrendingScreenTags.REPO_LIST)
        waitForNodeByText("Repo 0")

        // Scroll to bottom
        scrollToBottom()

        // Verify load more indicator
        waitForNodeByTag(TrendingScreenTags.LOAD_MORE_INDICATOR)
        composeTestRule.onNodeWithTag(TrendingScreenTags.LOAD_MORE_INDICATOR).assertIsDisplayed()
    }

    private fun waitForNodeByTag(tag: String, timeout: Long = TestConfig.LOADING_TIMEOUT) {
        composeTestRule.waitUntil(timeoutMillis = timeout) {
            composeTestRule
                .onAllNodesWithTag(tag)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForNodeByText(text: String, timeout: Long = TestConfig.LOADING_TIMEOUT) {
        composeTestRule.waitUntil(timeoutMillis = timeout) {
            composeTestRule
                .onAllNodesWithText(text)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun scrollToBottom() {
        val list = composeTestRule.onNodeWithTag(TrendingScreenTags.REPO_LIST)
        repeat(TestConfig.SCROLL_REPEAT_COUNT) {
            list.performScrollToIndex(TestConfig.ITEMS_PER_PAGE - 1)
            composeTestRule.mainClock.autoAdvance = false
            composeTestRule.mainClock.advanceTimeBy(TestConfig.SCROLL_DELAY)
            composeTestRule.mainClock.autoAdvance = true
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object TestGithubAccessServiceModule {

    @Provides
    @Singleton
    fun provideGithubAccessService(): IGithubAccessService {
        return object : IGithubAccessService {
            override suspend fun fetchTrendingRepos(page: Int, perPage: Int): Flow<Result<List<Repository>>> = flow {
                delay(TestConfig.NETWORK_DELAY)
                if (TestConfig.shouldFail) {
                    emit(Result.failure(Exception("Failed to load repositories")))
                } else if (page == 1) {
                    emit(Result.success(TestConfig.createMockRepos()))
                } else {
                    delay(TestConfig.LOAD_MORE_DELAY)
                    emit(Result.failure(Exception("Failed to load repositories")))
                }
            }

            override suspend fun fetchTopicList(
                page: Int,
                perPage: Int
            ): Flow<Result<List<Topic>>> = flow {
                emit(Result.failure(Exception("Not implemented in test")))
            }

            override suspend fun createNewIssue(
                owner: String,
                repoName: String,
                title: String,
                body: String?
            ): Flow<Result<Issue>> = flow {
                emit(Result.failure(Exception("Not implemented in test")))
            }

            override fun fetchAccessToken(code: String): Flow<Result<String>> = flow {
                emit(Result.failure(Exception("Not implemented in test")))
            }

            override suspend fun getUserProfile(): Flow<Result<UserProfile>> = flow {
                emit(Result.failure(Exception("Not implemented in test")))
            }

            override suspend fun searchRepositories(
                query: String,
                page: Int,
                perPage: Int
            ): Flow<Result<List<Repository>>> = flow {
                emit(Result.failure(Exception("Not implemented in test")))
            }

            override suspend fun getRecentlyPushedRepos(username: String): Flow<Result<List<Repository>>> = flow {
                emit(Result.failure(Exception("Not implemented in test")))
            }

            override fun getRepository(owner: String, repoName: String): Flow<Result<Repository>> = flow {
                emit(Result.failure(Exception("Not implemented in test")))
            }

            override fun saveAccessToken(token: String) {
            }

            override fun clearAccessToken() {
            }

            override fun checkAccessTokenExists(): Boolean = false

            override fun getAccessToken(): String? = null
        }
    }
}