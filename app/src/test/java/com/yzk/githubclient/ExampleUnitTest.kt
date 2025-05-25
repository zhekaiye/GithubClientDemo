package com.yzk.githubclient

import com.yzk.githubclient.data.Issue
import com.yzk.githubclient.data.Repository
import com.yzk.githubclient.data.SearchRepoResp
import com.yzk.githubclient.data.SubmitIssueData
import com.yzk.githubclient.data.UserProfile
import com.yzk.githubclient.github.GithubAccessService
import com.yzk.githubclient.github.IGithubAccessService
import com.yzk.githubclient.github.IGithubApi
import com.yzk.githubclient.security.SecuritySharedPreference
import com.yzk.githubclient.utils.Consts
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@ExperimentalCoroutinesApi
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    companion object {
        // Test data constants
        private const val FAKE_TOKEN = "fake-token"
        private const val TEST_OWNER = "testowner"
        private const val TEST_REPO = "testrepo"
        private const val TEST_ISSUE_TITLE = "Test Issue"
        private const val TEST_ISSUE_BODY = "Test Body"
        private const val ERROR_NOT_AUTHENTICATED = "Not authenticated"

        // API parameters
        private const val SORT_STARS = "stars"
        private const val ORDER_DESC = "desc"
        private const val DEFAULT_PAGE = 1
        private const val DEFAULT_PER_PAGE = 20
        private const val POPULAR_REPOS_QUERY = "stars:>1"

        // Mock user data
        private val TEST_USER = UserProfile(
            login = "testuser",
            id = 1,
            avatarUrl = "http://example.com/avatar.jpg"
        )

        // Mock repository data
        private fun createMockRepo(id: Long, name: String, stars: Int) = Repository(
            id = id,
            name = name,
            fullName = "user$id/$name",
            owner = UserProfile(login = "user$id", id = id, avatarUrl = ""),
            description = "desc$id",
            stargazersCount = stars,
            forksCount = stars / 2,
            language = if (id.toInt() % 2 == 0) "Java" else "Kotlin",
            htmlUrl = "https://github.com/user$id/$name"
        )

        // Mock issue data
        private val TEST_ISSUE = Issue(
            id = 1,
            number = 1,
            title = TEST_ISSUE_TITLE,
            body = TEST_ISSUE_BODY,
            state = "open",
            createdAt = "2024-03-20T10:00:00Z",
            updatedAt = "2024-03-20T10:00:00Z",
            user = TEST_USER
        )
    }

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var mockGithubApi: IGithubApi

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var mockGithubAccessService: IGithubAccessService

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        mockGithubAccessService = GithubAccessService(mockGithubApi)
    }

    @Test
    fun `getUserProfile returns failure when not authenticated`() = runTest(testDispatcher) {
        // Given
        whenever(SecuritySharedPreference.getData(Consts.KEY_ACCESS_TOKEN)).thenReturn(null)

        // When
        val result = mockGithubAccessService.getUserProfile().first()

        // Then
        assert(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, ERROR_NOT_AUTHENTICATED)
        verify(mockGithubApi, never()).getUserProfile()
    }

    @Test
    fun `getUserProfile returns user on successful API call`() = runTest(testDispatcher) {
        // Given
        val successResponse: Response<UserProfile> = Response.success(TEST_USER)
        whenever(SecuritySharedPreference.getData(Consts.KEY_ACCESS_TOKEN)).thenReturn(FAKE_TOKEN)
        whenever(mockGithubApi.getUserProfile()).thenReturn(successResponse)

        // When
        val result = mockGithubAccessService.getUserProfile().first()

        // Then
        assert(result.isSuccess)
        assertEquals(result.getOrNull(), TEST_USER)
        verify(mockGithubApi).getUserProfile()
    }

    @Test
    fun `getUserProfile returns failure on API error`() = runTest(testDispatcher) {
        // Given
        val errorResponse: Response<UserProfile> = Response.error(404, "Not Found".toResponseBody(null))
        whenever(SecuritySharedPreference.getData(Consts.KEY_ACCESS_TOKEN)).thenReturn(FAKE_TOKEN)
        whenever(mockGithubApi.getUserProfile()).thenReturn(errorResponse)

        // When
        val result = mockGithubAccessService.getUserProfile().first()

        // Then
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message?.contains("API Error getting user: 404") ?: false)
        verify(mockGithubApi).getUserProfile()
    }

    @Test
    fun `getUserProfile returns failure on API exception`() = runTest(testDispatcher) {
        // Given
        val exception = RuntimeException("Network error")
        whenever(SecuritySharedPreference.getData(Consts.KEY_ACCESS_TOKEN)).thenReturn(FAKE_TOKEN)
        whenever(mockGithubApi.getUserProfile()).doSuspendableAnswer { throw exception }

        // When
        val result = mockGithubAccessService.getUserProfile().first()

        // Then
        assert(result.isFailure)
        assertEquals(result.exceptionOrNull(), exception)
        verify(mockGithubApi).getUserProfile()
    }

    // Repository Search Tests
    @Test
    fun `searchRepositories returns success with repositories list`() = runTest(testDispatcher) {
        // Given
        val mockRepos = listOf(
            createMockRepo(1, "repo1", 100),
            createMockRepo(2, "repo2", 200)
        )
        val searchResponse = SearchRepoResp(
            totalCount = mockRepos.size,
            incompleteResults = false,
            items = mockRepos
        )
        val successResponse: Response<SearchRepoResp> = Response.success(searchResponse)
        whenever(mockGithubApi.searchRepositories(
            query = "test",
            sort = SORT_STARS,
            order = ORDER_DESC,
            page = DEFAULT_PAGE,
            perPage = DEFAULT_PER_PAGE
        )).thenReturn(successResponse)

        // When
        val result = mockGithubAccessService.searchRepositories("test").first()

        // Then
        assert(result.isSuccess)
        assertEquals(result.getOrNull(), mockRepos)
    }

    @Test
    fun `searchPopularRepos returns success with popular repositories`() = runTest(testDispatcher) {
        // Given
        val mockRepos = listOf(
            createMockRepo(1, "trending1", 1000),
            createMockRepo(2, "trending1", 2000)
        )
        val searchResponse = SearchRepoResp(
            totalCount = mockRepos.size,
            incompleteResults = false,
            items = mockRepos
        )
        val successResponse: Response<SearchRepoResp> = Response.success(searchResponse)
        whenever(mockGithubApi.searchRepositories(
            query = POPULAR_REPOS_QUERY,
            sort = SORT_STARS,
            order = ORDER_DESC,
            page = DEFAULT_PAGE,
            perPage = DEFAULT_PER_PAGE
        )).thenReturn(successResponse)

        // When
        val result = mockGithubAccessService.searchRepositories("trending1").first()

        // Then
        assert(result.isSuccess)
        assertEquals(result.getOrNull(), mockRepos)
    }

    // Create New Issue Tests

    @Test
    fun `createNewIssue returns success with created issue`() = runTest(testDispatcher) {
        // Given
        val successResponse: Response<Issue> = Response.success(TEST_ISSUE)
        whenever(mockGithubApi.createNewIssue(
            owner = TEST_OWNER,
            repo = TEST_REPO,
            issueRequestBody = SubmitIssueData(title = TEST_ISSUE_TITLE, body = TEST_ISSUE_BODY)
        )).thenReturn(successResponse)

        // When
        val result = mockGithubAccessService.createNewIssue(
            owner = TEST_OWNER,
            repoName = TEST_REPO,
            title = TEST_ISSUE_TITLE,
            body = TEST_ISSUE_BODY
        ).first()

        // Then
        assert(result.isSuccess)
        assertEquals(result.getOrNull(), TEST_ISSUE)
    }

    @Test
    fun `createNewIssue returns failure on API error`() = runTest(testDispatcher) {
        // Given
        val errorResponse: Response<Issue> = Response.error(403, "Forbidden".toResponseBody(null))
        whenever(mockGithubApi.createNewIssue(
            owner = TEST_OWNER,
            repo = TEST_REPO,
            issueRequestBody = SubmitIssueData(title = TEST_ISSUE_TITLE, body = TEST_ISSUE_BODY)
        )).thenReturn(errorResponse)

        // When
        val result = mockGithubAccessService.createNewIssue(
            owner = TEST_OWNER,
            repoName = TEST_REPO,
            title = TEST_ISSUE_TITLE,
            body = TEST_ISSUE_BODY
        ).first()

        // Then
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message?.contains("API Error: 403") ?: false)
    }
}