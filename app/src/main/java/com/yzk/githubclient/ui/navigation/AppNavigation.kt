package com.yzk.githubclient.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yzk.githubclient.issue.CreateNewIssueScreen
import com.yzk.githubclient.ui.detail.InAppWebViewWithProgress
import com.yzk.githubclient.ui.login.AuthState
import com.yzk.githubclient.ui.login.LoginScreen
import com.yzk.githubclient.ui.login.LoginViewModel
import com.yzk.githubclient.ui.search.SearchScreen
import com.yzk.githubclient.ui.topic.TopicScreen
import com.yzk.githubclient.ui.trending.TrendingScreen
import com.yzk.githubclient.ui.userprofile.ProfileScreen
import com.yzk.githubclient.ui.userprofile.UserRepoScreen
import java.net.URLDecoder

/**
 * @description the main navigation of app
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */

// 定义导航的路由
object NavigationRoutes {
    const val Repository = "Repository"
    const val Topic = "Topic"
    const val Search = "Search"
    const val Profile = "Profile"
}

sealed class NavigationItem(val route: String, val icon: ImageVector, val label: String) {
    object RepositoryItem : NavigationItem(NavigationRoutes.Repository, Icons.Default.Home, NavigationRoutes.Repository)
    object TopicItem : NavigationItem(NavigationRoutes.Topic, Icons.Default.Topic, NavigationRoutes.Topic)
    object SearchItem : NavigationItem(NavigationRoutes.Search, Icons.Default.Search, NavigationRoutes.Search)
    object ProfileItem : NavigationItem(NavigationRoutes.Profile, Icons.Default.Person, NavigationRoutes.Profile)
}

@Composable
fun AppNavigation(loginViewModel: LoginViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val authState = loginViewModel.authState.collectAsState().value
    val items = listOf(
        NavigationItem.RepositoryItem,
        NavigationItem.TopicItem,
        NavigationItem.SearchItem,
        NavigationItem.ProfileItem
    )

    Scaffold(
        bottomBar = {
            BottomNavigation(
                backgroundColor = Color.White,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    BottomNavigationItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        selectedContentColor = MaterialTheme.colors.primary,
                        unselectedContentColor = Color.Gray,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = NavigationItem.RepositoryItem.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(NavigationItem.RepositoryItem.route) {
                    TrendingScreen(navController = navController)
                }
                composable(NavigationItem.TopicItem.route) {
                    TopicScreen(navController = navController)
                }
                composable(NavigationItem.SearchItem.route) {
                    SearchScreen(navController = navController)
                }
                composable(NavigationItem.ProfileItem.route) {
                    when (authState) {
                        is AuthState.LoggedInState -> ProfileScreen(
                            navController = navController,
                            loginViewModel = loginViewModel
                        )
                        else -> LoginScreen()
                    }
                }
                composable(AppScreen.Login.route) {
                    LoginScreen()
                }
                composable(
                    route = AppScreen.Repository.route,
                    arguments = listOf(
                        navArgument("owner") { type = NavType.StringType },
                        navArgument("repoName") { type = NavType.StringType },
                        navArgument("repoUrl") { type = NavType.StringType },
                    )
                ) { backStackEntry ->
                    val owner = backStackEntry.arguments?.getString("owner").orEmpty()
                    val repoName = backStackEntry.arguments?.getString("repoName").orEmpty()
                    val repoUrl = URLDecoder.decode(backStackEntry.arguments?.getString("repoUrl").orEmpty(), "UTF-8")
                    if (repoUrl.isNotEmpty()) {
                        InAppWebViewWithProgress(
                            url = repoUrl
                        )
                    } else {
                        UserRepoScreen(
                            owner = owner,
                            repoName = repoName,
                            onNavigateBack = { navController.navigateUp() },
                            onNavigateToRaiseIssue = { ownerParam, repoNameParam ->
                                navController.navigate(AppScreen.RaiseIssue.createRoute(ownerParam, repoNameParam))
                            }
                        )
                    }
                }

                composable(
                    route = AppScreen.RaiseIssue.route,
                    arguments = listOf(
                        navArgument("owner") { type = NavType.StringType },
                        navArgument("repoName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val owner = backStackEntry.arguments?.getString("owner").orEmpty()
                    val repoName = backStackEntry.arguments?.getString("repoName").orEmpty()
                    CreateNewIssueScreen(
                        owner = owner,
                        repoName = repoName,
                        onNavigateBack = { navController.navigateUp() },
                        onIssueCreated = { issueNumber ->
                            navController.navigateUp()
                        }
                    )
                }
            }
        }
    }
}
