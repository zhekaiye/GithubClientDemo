package com.yzk.githubclient.ui.login

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.yzk.githubclient.R
import com.yzk.githubclient.utils.Consts
import java.net.URLEncoder
import java.util.UUID

/**
 * @description login screen page
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
@Composable
fun LoginScreen(
    onLoginClick: (Context, String) -> Unit = ::startWebviewToLoginGithub
) {
    val context = LocalContext.current
    // Generate a unique state parameter for CSRF protection
    val state = remember { UUID.randomUUID().toString() }
    // TODO: Store the 'state' variable securely (e.g., ViewModel, SharedPreferences)
    //       to verify it upon receiving the OAuth callback.

    val authUrlStr = Consts.URL_GITHUB_LOGIN_AUTH +
            "?client_id=${Consts.GITHUB_APP_CLIENT_ID}" +
            "&redirect_uri=${URLEncoder.encode(Consts.URI_CALLBACK, "UTF-8")}" +
            "&scope=${Consts.SCOPE_REPO_AND_USER}" +
            "&state=$state"

    // Configure status bar appearance for this screen
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true // Use dark icons on light status bar
        )
        // Optional: Hide navigation bar if desired for a more immersive login screen
        // systemUiController.isNavigationBarVisible = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Set background to white
        horizontalAlignment = Alignment.CenterHorizontally // Center children horizontally
    ) {
        // Top space for the Logo
        Box(
            modifier = Modifier
                .weight(1f) // Occupy remaining vertical space, pushing the button to the bottom
                .fillMaxWidth(),
            contentAlignment = Alignment.Center // Center the Logo within this Box
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier.size(100.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { onLoginClick(context, authUrlStr) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.LightGray,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = stringResource(id = R.string.login_button_text),
                    fontSize = 20.sp
                )
            }
        }
    }
}

fun startWebviewToLoginGithub(context: Context, url: String) {
    val authUri = url.toUri()
    try {
        val intent = Intent(Intent.ACTION_VIEW, authUri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("LoginScreen", "startWebviewToLoginGithub error for url: $url", e)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF) // Preview with white background
@Composable
fun LoginScreenPreview() {
    LoginScreen(onLoginClick = { _, _ -> /* Do nothing in preview click */ })
}