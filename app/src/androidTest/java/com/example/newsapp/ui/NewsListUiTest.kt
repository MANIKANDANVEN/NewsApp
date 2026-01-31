package com.example.newsapp.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.newsapp.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewsUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testNavigationToSourcesAndSelectOutlet() {
        // 1. Click on the 'Sources' bottom tab
        composeTestRule.onNodeWithText("Sources").performClick()

        // 2. WAIT for the content to load (waiting for "BBC News" to appear)
        composeTestRule.waitUntil(5000) {
            composeTestRule
                .onAllNodesWithText("BBC News", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 3. Verify and click the source
        composeTestRule
            .onNodeWithText("BBC News", substring = true)
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun testSavedArticlesEmptyState() {
        // 1. Navigate to Saved Tab
        composeTestRule.onNodeWithText("Saved").performClick()

        // 2. Verify empty state text
        composeTestRule.onNodeWithText("No saved articles yet.").assertIsDisplayed()
    }
}