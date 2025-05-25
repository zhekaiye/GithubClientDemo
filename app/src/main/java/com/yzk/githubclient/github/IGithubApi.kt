package com.yzk.githubclient.github

import com.yzk.githubclient.data.AccessTokenResp
import com.yzk.githubclient.data.Issue
import com.yzk.githubclient.data.Repository
import com.yzk.githubclient.data.SearchRepoResp
import com.yzk.githubclient.data.SearchTopicResp
import com.yzk.githubclient.data.SubmitIssueData
import com.yzk.githubclient.data.UserProfile
import com.yzk.githubclient.utils.Consts
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * @description github api
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
interface IGithubApi {
    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("sort") sort: String = "stars",
        @Query("order") order: String = "desc",
        @Query("page") page: Int = Consts.COUNT_PAGE,
        @Query("per_page") perPage: Int = Consts.COUNT_PER_PAGE
    ): Response<SearchRepoResp>

    @GET("search/topics")
    suspend fun searchTopics(
        @Query("q") query: String,
        @Query("page") page: Int = Consts.COUNT_PAGE,
        @Query("per_page") perPage: Int = Consts.COUNT_PER_PAGE
    ): Response<SearchTopicResp>

    @POST("repos/{owner}/{repo}/issues")
    suspend fun createNewIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body issueRequestBody: SubmitIssueData // Define IssueRequestBody data class
    ): Response<Issue>

    @FormUrlEncoded // Important: Send data as form-urlencoded
    @Headers("Accept: application/json") // Request JSON response
    @POST("https://github.com/login/oauth/access_token") // Full URL
    suspend fun fetchAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String // Send the same redirect_uri used in auth request
    ): Response<AccessTokenResp>

    @GET("user")
    suspend fun getUserProfile(): Response<UserProfile>

    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(@Path("owner") owner: String, @Path("repo") repo: String): Response<Repository>

    @GET("users/{username}/repos")
    suspend fun getRecentlyPushedRepos(
        @Path("username") username: String,
        @Query("sort") sort: String = "pushed",
        @Query("per_page") perPage: Int = 6
    ): Response<List<Repository>>
}