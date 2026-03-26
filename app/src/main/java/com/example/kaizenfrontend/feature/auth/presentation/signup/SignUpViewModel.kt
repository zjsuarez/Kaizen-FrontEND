package com.example.kaizenfrontend.feature.auth.presentation.signup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.feature.auth.data.repository.AuthRepositoryImpl
import com.example.kaizenfrontend.feature.auth.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SignUpUiState {
    object Idle : SignUpUiState()
    object Loading : SignUpUiState()
    object Success : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()
}

class SignUpViewModel(context: Context) : ViewModel() {

    private val sessionManager = SessionManager(context)
    private val registerUseCase = RegisterUseCase(AuthRepositoryImpl(sessionManager))

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, confirmPassword: String) {
        when {
            email.isBlank() || password.isBlank() -> {
                _uiState.value = SignUpUiState.Error("Please fill in all fields")
                return
            }
            password != confirmPassword -> {
                _uiState.value = SignUpUiState.Error("Passwords don't match")
                return
            }
        }
        viewModelScope.launch {
            _uiState.value = SignUpUiState.Loading
            val result = registerUseCase(email, password)
            _uiState.value = result.fold(
                onSuccess = { SignUpUiState.Success },
                onFailure = { SignUpUiState.Error(it.message ?: "Registration failed") }
            )
        }
    }

    fun resetState() {
        _uiState.value = SignUpUiState.Idle
    }
}
