package com.example.newsapp.ui.screens

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.newsapp.ui.components.ErrorContent
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(url: String, navController: NavController) {
    val decodedUrl = remember(url) {
        URLDecoder.decode(url, StandardCharsets.UTF_8.toString())
    }

    var webView: WebView? by remember { mutableStateOf(null) }

    // State to track if the web page failed to load
    var hasError by remember { mutableStateOf(false) }

    BackHandler {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (webView?.canGoBack() == true) {
                            webView?.goBack()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (hasError) {
                ErrorContent(
                    message = "Failed to load the article. Please check your connection.",
                    onRetry = {
                        hasError = false
                        webView?.reload()
                    }
                )
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            webViewClient = object : WebViewClient() {
                                // For modern Android (API 23+)
                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    // Only trigger error if it's the main frame (not an ad failing)
                                    if (request?.isForMainFrame == true) {
                                        hasError = true
                                    }
                                }

                                // For older Android versions
                                override fun onReceivedError(
                                    view: WebView?,
                                    errorCode: Int,
                                    description: String?,
                                    failingUrl: String?
                                ) {
                                    hasError = true
                                }
                            }
                            loadUrl(decodedUrl)
                            webView = this
                        }
                    }
                )
            }
        }
    }
}