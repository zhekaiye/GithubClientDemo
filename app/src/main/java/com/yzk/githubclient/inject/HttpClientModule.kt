package com.yzk.githubclient.inject

import com.yzk.githubclient.github.IGithubApi
import com.yzk.githubclient.network.GithubApiInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * @description
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
@Module
@InstallIn(SingletonComponent::class)
object HttpClientModule {
    private const val URL_GITHUB_API = "https://api.github.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(
        githubInterceptor: GithubApiInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(githubInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(URL_GITHUB_API)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // Use Gson or Moshi
            .build()
    }

    @Provides
    @Singleton
    fun provideGithubApi(retrofit: Retrofit): IGithubApi {
        return retrofit.create(IGithubApi::class.java)
    }
}