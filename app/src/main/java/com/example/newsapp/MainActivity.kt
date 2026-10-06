package com.example.newsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.newsapp.ui.components.AppBottomBar
import com.example.newsapp.ui.components.OfflineBanner
import com.example.newsapp.ui.navigation.NewsNavGraph
import com.example.newsapp.ui.theme.NewsAppTheme
import com.example.newsapp.viewmodel.ConnectivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val connectivityViewModel: ConnectivityViewModel = hiltViewModel()
            val isConnected by connectivityViewModel.isConnected.collectAsState()

            NewsAppTheme {
                Scaffold(
                    topBar = {
                        if (!isConnected) {
                            OfflineBanner()
                        }
                    },
                    bottomBar = { AppBottomBar(navController) }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NewsNavGraph(navController = navController)
                    }
                }
            }
        }
    }
}