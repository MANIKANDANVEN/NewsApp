package com.example.newsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.connectivity.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// App-wide connectivity status for the offline banner in MainActivity. Separate
// from NewsViewModel so every screen (not just Headlines) can react to it.
@HiltViewModel
class ConnectivityViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver
) : ViewModel() {
    val isConnected: StateFlow<Boolean> = connectivityObserver.isConnected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
}
