package com.yzk.githubclient.network

import com.yzk.githubclient.security.SecuritySharedPreference
import com.yzk.githubclient.utils.Consts
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @description okhttp interceptor of github api
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
@Singleton
class GithubApiInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
            .header("Accept", "application/vnd.github+json")

        val accessToken = SecuritySharedPreference.getData(Consts.KEY_ACCESS_TOKEN)
        if (accessToken?.isNotEmpty() == true) {
            requestBuilder.header("Authorization", "token $accessToken")
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}