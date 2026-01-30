package com.example.newsapp.ui.screens

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun WebViewScreen(url: String) {
    // 1. Decode the URL back to its original form
    val decodedUrl = remember(url) {
        URLDecoder.decode(url, StandardCharsets.UTF_8.toString())
    }

    // 2. Wrap the Android WebView
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true // Required for most news sites
                webViewClient = WebViewClient()  // Forces links to open in the app
                loadUrl(decodedUrl)
            }
        },
        update = { webView ->
            webView.loadUrl(decodedUrl)
        }
    )
}