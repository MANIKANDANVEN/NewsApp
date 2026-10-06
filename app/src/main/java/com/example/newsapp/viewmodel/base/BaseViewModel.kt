package com.example.newsapp.viewmodel.base

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel(private val appContext: Context) : ViewModel() {

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
                val errorMessage = e.localizedMessage ?: appContext.getString(R.string.BaseViewModel_unknownError)
                errorState?.let { stateFlow?.value = it(errorMessage) }
            }
        }
    }

    // simple launches (No StateFlow needed)
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