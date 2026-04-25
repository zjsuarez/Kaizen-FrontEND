package com.example.kaizenfrontend.feature.auth.presentation.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.feature.auth.data.repository.AuthRepositoryImpl
import com.example.kaizenfrontend.feature.auth.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.util.UUID

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(context: Context) : ViewModel() {

    private val sessionManager = SessionManager(context)
    private val authRepository = AuthRepositoryImpl(sessionManager)
    private val loginUseCase = LoginUseCase(authRepository)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Please fill in all fields")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = loginUseCase(email, password)
            _uiState.value = result.fold(
                onSuccess = { LoginUiState.Success },
                onFailure = { LoginUiState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val rawNonce = UUID.randomUUID().toString()
                val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(rawNonce.toByteArray())
                val hashedNonce = bytes.joinToString("") { "%02x".format(it) }

                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("864644262862-5p3b6cvaj31ee7adcl5tfq9d1rmt8sls.apps.googleusercontent.com")
                    .setAutoSelectEnabled(false)
                    .setNonce(hashedNonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && 
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    
                    val loginResult = authRepository.googleLogin(idToken)
                    _uiState.value = loginResult.fold(
                        onSuccess = { LoginUiState.Success },
                        onFailure = { LoginUiState.Error(it.message ?: "Google Login failed") }
                    )
                } else {
                    _uiState.value = LoginUiState.Error("Unexpected credential type")
                }
            } catch (e: GetCredentialCancellationException) {
                _uiState.value = LoginUiState.Idle
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Google Sign-In failed")
            }
        }
    }
}
