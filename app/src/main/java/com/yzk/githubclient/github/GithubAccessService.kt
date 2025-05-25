package com.yzk.githubclient.github

import android.util.Log
import com.yzk.githubclient.data.Issue
import com.yzk.githubclient.data.Repository
import com.yzk.githubclient.data.SubmitIssueData
import com.yzk.githubclient.data.Topic
import com.yzk.githubclient.data.UserProfile
import com.yzk.githubclient.security.SecuritySharedPreference
import com.yzk.githubclient.utils.Consts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @description implementation service to access github
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
@Singleton
class GithubAccessService @Inject constructor(
    private val githubApi: IGithubApi
) : IGithubAccessService {

    companion object {
        private const val TAG = "GithubAccessService"
    }

    override fun fetchAccessToken(code: String): Flow<Result<String>> = flow {
        try {
            val response = githubApi.fetchAccessToken(
                clientId = Consts.GITHUB_APP_CLIENT_ID,
                clientSecret = Consts.GITHUB_APP_CLIENT_SECRET,
                code = code,
                redirectUri = Consts.URI_CALLBACK
            )
            if (response.isSuccessful && response.body()?.accessToken != null) {
                val token = response.body()!!.accessToken
                saveAccessToken(token)
                emit(Result.success(token))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "fetchAccessToken failed: ${response.code()} - $errorBody")
                emit(Result.failure(Exception("fetchAccessToken failed: ${response.code()} - ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchAccessToken exception", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getUserProfile(): Flow<Result<UserProfile>> = flow {
        val token = getAccessToken()
        if (token == null) {
            emit(Result.failure(Exception("Not authenticated")))
            return@flow
        }
        try {
            val response = githubApi.getUserProfile()
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("API Error getting user: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun searchRepositories(
        query: String,
        page: Int,
        perPage: Int
    ): Flow<Result<List<Repository>>> = flow {
        try {
            val response = githubApi.searchRepositories(
                query = query,
                sort = "stars",
                order = "desc",
                page = page,
                perPage = perPage
            )
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.items))
            } else {
                emit(Result.failure(Exception("API Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun fetchTrendingRepos(
        page: Int,
        perPage: Int
    ): Flow<Result<List<Repository>>> = flow {
        try {
            val response =
                githubApi.searchRepositories(query = "stars:>1", sort = "stars", order = "desc", page = page, perPage = perPage)
            Log.i("###GithubTest", "response: ${response.body()}")
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.items))
            } else {
                emit(Result.failure(Exception("API Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun fetchTopicList(page: Int, perPage: Int): Flow<Result<List<Topic>>> = flow {
        try {
            val response =
                githubApi.searchTopics(query = "top", page = page, perPage = perPage)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.items))
            } else {
                emit(Result.failure(Exception("API Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun createNewIssue(
        owner: String,
        repoName: String,
        title: String,
        body: String?
    ): Flow<Result<Issue>> = flow {
        try {
            val requestBody = SubmitIssueData(title, body)
            val response = githubApi.createNewIssue(
                owner,
                repoName,
                requestBody
            )
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("API Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getRecentlyPushedRepos(username: String): Flow<Result<List<Repository>>> = flow {
        try {
            val response = githubApi.getRecentlyPushedRepos(username)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("API Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getRepository(owner: String, repoName: String): Flow<Result<Repository>> = flow {
        try {
            val response = githubApi.getRepository(owner, repoName)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Error fetching repository: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun saveAccessToken(token: String) {
        SecuritySharedPreference.saveData(Consts.KEY_ACCESS_TOKEN, token)
    }

    override fun clearAccessToken() {
        SecuritySharedPreference.clearData(Consts.KEY_ACCESS_TOKEN)
    }

    override fun checkAccessTokenExists(): Boolean {
        return getAccessToken()?.isNotEmpty() ?: false
    }

    override fun getAccessToken(): String? {
        return SecuritySharedPreference.getData(Consts.KEY_ACCESS_TOKEN)
    }
}