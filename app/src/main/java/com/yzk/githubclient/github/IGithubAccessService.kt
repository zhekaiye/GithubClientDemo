package com.yzk.githubclient.github

import com.yzk.githubclient.data.Issue
import com.yzk.githubclient.data.Repository
import com.yzk.githubclient.data.Topic
import com.yzk.githubclient.data.UserProfile
import com.yzk.githubclient.utils.Consts
import kotlinx.coroutines.flow.Flow

/**
 * @description github api access service
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
interface IGithubAccessService {
    fun fetchAccessToken(code: String): Flow<Result<String>>

    /**
     * Fetches the currently authenticated user's profile information.
     */
    suspend fun getUserProfile(): Flow<Result<UserProfile>>

    /**
     * Searches for repositories on GitHub based on a query.
     */
    suspend fun searchRepositories(query: String, page: Int = Consts.COUNT_PAGE, perPage: Int = Consts.COUNT_PER_PAGE): Flow<Result<List<Repository>>>

    suspend fun fetchTrendingRepos(page: Int = Consts.COUNT_PAGE, perPage: Int = Consts.COUNT_PER_PAGE): Flow<Result<List<Repository>>>

    suspend fun fetchTopicList(page: Int = Consts.COUNT_PAGE, perPage: Int = Consts.COUNT_PER_PAGE): Flow<Result<List<Topic>>>

    suspend fun createNewIssue(
        owner: String,
        repoName: String,
        title: String,
        body: String?
    ): Flow<Result<Issue>>

    suspend fun getRecentlyPushedRepos(username: String): Flow<Result<List<Repository>>>

    /**
     * Fetches details for a single repository.
     */
    fun getRepository(owner: String, repoName: String): Flow<Result<Repository>>

    /** Saves the authentication token. */
    fun saveAccessToken(token: String)

    /** Clears the authentication token. */
    fun clearAccessToken()

    /** Checks if an authentication token exists. */
    fun checkAccessTokenExists(): Boolean

    /** Retrieves the authentication token. */
    fun getAccessToken(): String?
}