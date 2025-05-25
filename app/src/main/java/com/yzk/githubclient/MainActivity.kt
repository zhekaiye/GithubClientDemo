package com.yzk.githubclient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.yzk.githubclient.ui.login.LoginViewModel
import com.yzk.githubclient.ui.navigation.AppNavigation
import com.yzk.githubclient.utils.Consts
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val QUERY_CODE = "code"
        private const val QUERY_ERROR = "error"
        private const val QUERY_ERROR_DESC = "error_description"
    }

    private val loginViewModel : LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AppNavigation()
            }
        }

        parseIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        parseIntent(intent)
    }

    private fun parseIntent(intent: Intent) {
        val uri = intent.data
        if (uri != null && uri.toString().startsWith(Consts.URI_CALLBACK)) {
            val code = uri.getQueryParameter(QUERY_CODE)
            if (code != null) {
                // 使用code向GitHub获取access_token
                lifecycleScope.launch {
                    loginViewModel.fetchAccessToken(code)
                }
            } else {
                // 处理错误（如用户拒绝授权）
                Log.e(TAG, "oauth failed code: code" +
                        ", error: ${uri.getQueryParameter(QUERY_ERROR)}" +
                        ", desc: ${uri.getQueryParameter(QUERY_ERROR_DESC)}")
            }
        }
    }
}