package com.yzk.githubclient.ui.userprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TagFaces
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.yzk.githubclient.R
import com.yzk.githubclient.error.ErrorAlert
import com.yzk.githubclient.ui.login.LoginViewModel
import com.yzk.githubclient.ui.navigation.AppScreen
import java.net.URLEncoder
import com.yzk.githubclient.ui.detail.RepositoryCard

/**
 * @description screen of user profile
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by profileViewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.error != null -> {
                ErrorAlert(
                    message = stringResource(id = R.string.app_name, uiState.error ?: ""),
                    onRetry = { profileViewModel.loadUserProfile() }
                )
            }
            uiState.userProfile != null -> {
                UserProfileContent(
                    uiState = uiState,
                    navController = navController,
                    onLogout = { loginViewModel.logout() }
                )
            }
        }
    }
}

@Composable
fun UserProfileContent(
    uiState: UserProfileUiState,
    navController: NavController,
    onLogout: () -> Unit
) {
    val user = uiState.userProfile!!
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header section (Avatar, Name, Username, Logout Button)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = rememberAsyncImagePainter(user.avatarUrl),
                contentDescription = stringResource(id = R.string.profile_avatar_description, user.login),
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name ?: user.login, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = user.login, fontSize = 16.sp, color = Color.Gray)
            }
            Button(
                onClick = { onLogout }
            ) {
                Text(stringResource(id = R.string.profile_logout_button))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bio or Status
        user.bio?.let { InfoRow(icon = Icons.Default.TagFaces, text = it) }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Other details like Company, Location, Blog, Email
        user.company?.let { InfoRow(icon = Icons.Default.Business, text = it) }
        user.location?.let { InfoRow(icon = Icons.Default.LocationOn, text = it) }
        user.blog?.let { if (it.isNotEmpty()) InfoRow(icon = Icons.Default.Link, text = it) }
        user.email?.let { InfoRow(icon = Icons.Default.Email, text = it) }

        Spacer(modifier = Modifier.height(8.dp))

        // Followers / Following
        val followers = user.followers ?: 0
        val following = user.following ?: 0
        InfoRow(icon = Icons.Default.People, text = stringResource(id = R.string.profile_followers_following, followers, following))

        Spacer(modifier = Modifier.height(16.dp))

        // Repository Section
        RepositorySection(
            uiState = uiState,
            navController = navController
        )
    }
}

@Composable
fun RepositorySection(
    uiState: UserProfileUiState,
    navController: NavController
) {
    Column {
        if (uiState.pinnedRepos.isNotEmpty()) {
            Text(
                text = stringResource(id = R.string.profile_view_all_repos),
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.pinnedRepos.forEach { repo ->
                    RepositoryCard(repo = repo, onClick = {
                        navController.navigate(AppScreen.Repository.createRoute(
                            repo.owner.login,
                            repo.name,
                            ""))
                    })
                }
            }
        } else if (!uiState.isLoading) {
            Text(stringResource(id = R.string.app_name), modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)
        }
    }
}



/**
 * A helper composable to display a row of information with an icon and text.
 *
 * @param icon The leading [ImageVector] icon.
 * @param text The text content to display.
 */
@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, contentDescription = text, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.body1)
    }
}