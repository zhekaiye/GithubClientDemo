package com.yzk.githubclient

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.UriMatchers.hasHost
import androidx.test.espresso.intent.matcher.UriMatchers.hasPath
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yzk.githubclient.inject.HttpClientModule
import com.yzk.githubclient.ui.navigation.NavigationItem
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @description
 *
 * @author: yezhekai.256
 * @date: 5/25/25
 */
@HiltAndroidTest
@UninstallModules(HttpClientModule::class)
@RunWith(AndroidJUnit4::class)
class LoginGithubTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val intentsRule = IntentsRule()

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun loginScreen_displays_when_profileTabClicked_andNotLoggedIn() {
        // Arrange: MainActivity is launched, start destination is Popular.

        val profileNavLabel = NavigationItem.ProfileItem.label
        composeTestRule.onNodeWithText(profileNavLabel).performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText(composeTestRule.activity.getString(R.string.login_button_text))
                .fetchSemanticsNodes().isNotEmpty()
        }

        val logoDescription = composeTestRule.activity.getString(R.string.login_logo_description)
        val buttonText = composeTestRule.activity.getString(R.string.login_button_text)

        composeTestRule.onNodeWithContentDescription(logoDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(buttonText).assertIsDisplayed()
    }

    @Test
    fun loginButton_launchesCorrectIntent() {
        val profileNavLabel = NavigationItem.ProfileItem.label
        composeTestRule.onNodeWithText(profileNavLabel).performClick()

        val buttonText = composeTestRule.activity.getString(R.string.login_button_text)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText(buttonText)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(buttonText).assertIsDisplayed() // Double check

        Intents.intending(hasAction(Intent.ACTION_VIEW))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

        composeTestRule.onNodeWithText(buttonText).performClick()

        Intents.intended(
            allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData(hasHost("github.com")),
            hasData(hasPath("/login/oauth/authorize"))
        )
        )
    }
}