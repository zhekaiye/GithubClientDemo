package com.yzk.githubclient.ui.navigation

/**
 * @description
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
sealed class AppScreen(val route: String) {
    object Login : AppScreen("login")
    object Repositories : AppScreen("repositories")

    object Repository : AppScreen("repository/{owner}/{repoName}/{repoUrl}") {
        fun createRoute(owner: String, repoName: String, repoUrl: String) = "repository/$owner/$repoName/$repoUrl"
    }

    object RaiseIssue : AppScreen("repository/{owner}/{repoName}/issues/new") {
        /** Creates the navigation route for the RaiseIssue screen with the given parameters. */
        fun createRoute(owner: String, repoName: String) = "repository/$owner/$repoName/issues/new"
    }
}