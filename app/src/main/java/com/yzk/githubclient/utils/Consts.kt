package com.yzk.githubclient.utils

import com.yzk.githubclient.BuildConfig

/**
 * @description define const value
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
object Consts {
    const val URL_GITHUB_LOGIN_AUTH = "https://github.com/login/oauth/authorize"
    const val URI_CALLBACK = "yzkapp://oauth/callback"
    const val KEY_ACCESS_TOKEN = "github_access_token"
    const val GITHUB_APP_CLIENT_ID = BuildConfig.GITHUB_APP_CLIENT_ID
    const val GITHUB_APP_CLIENT_SECRET = BuildConfig.GITHUB_APP_CLIENT_SECRET
    const val SCOPE_REPO_AND_USER = "repo,user"
    const val COUNT_PAGE = 1
    const val COUNT_PER_PAGE = 10
}