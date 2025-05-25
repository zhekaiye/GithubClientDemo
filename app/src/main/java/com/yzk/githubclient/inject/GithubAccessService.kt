package com.yzk.githubclient.inject

import com.yzk.githubclient.github.GithubAccessService
import com.yzk.githubclient.github.IGithubAccessService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @description
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class GithubAccessService {
    @Binds
    @Singleton
    abstract fun bindGithubAccessService(service: GithubAccessService): IGithubAccessService
}