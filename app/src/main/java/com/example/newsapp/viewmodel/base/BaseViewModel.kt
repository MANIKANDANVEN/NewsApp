package com.example.newsapp.viewmodel.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    // Helper to launch coroutines with automatic error handling and loading states
    protected fun <T> safeLaunch(
        stateFlow: MutableStateFlow<T>? = null,
        loadingState: T? = null,
        errorState: ((String) -> T)? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch {
            // 1. Set Loading state if provided
            loadingState?.let { stateFlow?.value = it }

            try {
                block()
            } catch (e: Exception) {
                // 2. Handle Error state if provided
                val errorMessage = e.localizedMessage ?: "An unknown error occurred"
                errorState?.let { stateFlow?.value = it(errorMessage) }
            }
        }
    }

    // 2. NEW version for simple launches (No StateFlow needed)
    protected fun safeLaunch(
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                // Just log it or handle globally
                Log.e("BaseViewModel", "Error in background task", e)
            }
        }
    }
}