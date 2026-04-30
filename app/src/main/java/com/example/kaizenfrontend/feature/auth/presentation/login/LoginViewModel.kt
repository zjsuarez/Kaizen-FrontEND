package com.example.kaizenfrontend.feature.auth.presentation.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.feature.auth.data.repository.AuthRepositoryImpl
import com.example.kaizenfrontend.feature.auth.domain.validation.AuthInputValidator
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
    data class Success(val needsCalibration: Boolean) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(context: Context) : ViewModel() {

    private val sessionManager = SessionManager(context)
    private val authRepository = AuthRepositoryImpl(sessionManager)
    private val loginUseCase = LoginUseCase(authRepository)

    private val userRepository = com.example.kaizenfrontend.feature.user.data.repository.UserRepositoryImpl(sessionManager)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        val normalizedEmail = AuthInputValidator.normalizeEmail(email)

        AuthInputValidator.validateEmail(normalizedEmail)?.let { errorMessage ->
            _uiState.value = LoginUiState.Error(errorMessage)
            return
        }

        AuthInputValidator.validatePassword(password)?.let { errorMessage ->
            _uiState.value = LoginUiState.Error(errorMessage)
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = loginUseCase(normalizedEmail, password)
            if (result.isSuccess) {
                // Token is returned directly from the repository — no SessionManager read needed.
                val token = result.getOrThrow()
                sessionManager.awaitTokenPersistence(token)
                checkCalibrationAndEmitSuccess(token)
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                if (errorMessage.contains("OAUTH_ONLY_ACCOUNT", ignoreCase = true)) {
                    _uiState.value = LoginUiState.Error("This account uses Google Sign-In. Please use the Google button or Reset Password to create a local key.")
                } else if (errorMessage.contains("INVALID_CREDENTIALS", ignoreCase = true)) {
                    _uiState.value = LoginUiState.Error("Wrong password")
                } else {
                    _uiState.value = LoginUiState.Error(errorMessage)
                }
            }
        }
    }

    /**
     * Fetches the user profile using the provided [token] directly (avoids any
     * SessionManager read timing issues). Routing decision:
     *   - primaryGoal == null  → new user, needs Calibration
     *   - primaryGoal != null  → returning user, go straight to Dashboard
     * unitSystem / effortMeasurement have server-side defaults and must NOT gate routing.
     */
    private suspend fun checkCalibrationAndEmitSuccess(
        token: String,
        fromGoogleAuth: Boolean = false
    ) {
        val profileResult = runCatching {
            userRepository.getCurrentUserWithToken(token)
        }.getOrElse { e ->
            android.util.Log.e("LoginVM", "Profile fetch threw: ${e.message}", e)
            Result.failure(e)
        }

        if (profileResult.isSuccess) {
            val user = profileResult.getOrNull()
            // Only primaryGoal gates calibration — it is strictly null for brand-new accounts.
            val isCalibrated = user?.primaryGoal?.isNotBlank() == true
            val showGoogleWelcome = fromGoogleAuth && !isCalibrated
            android.util.Log.d(
                "LoginVM",
                "Profile fetched OK. primaryGoal=${user?.primaryGoal}, authProvider=${user?.authProvider}, unitSystem=${user?.unitSystem}, effort=${user?.effortMeasurement}, isCalibrated=$isCalibrated"
            )
            sessionManager.saveCalibrationComplete(isCalibrated)
            sessionManager.setShouldShowGoogleWelcomePrompt(showGoogleWelcome)
            _uiState.value = LoginUiState.Success(needsCalibration = !isCalibrated)
        } else {
            val err = profileResult.exceptionOrNull()
            android.util.Log.e("LoginVM", "Profile fetch FAILED after successful login: ${err?.message}", err)
            // Fallback: route to Calibration rather than crashing or silently going to Dashboard.
            sessionManager.saveCalibrationComplete(false)
            sessionManager.setShouldShowGoogleWelcomePrompt(false)
            _uiState.value = LoginUiState.Success(needsCalibration = true)
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
                    if (loginResult.isSuccess) {
                        val token = loginResult.getOrThrow()
                        sessionManager.awaitTokenPersistence(token)
                        checkCalibrationAndEmitSuccess(token, fromGoogleAuth = true)
                    } else {
                        _uiState.value = LoginUiState.Error(loginResult.exceptionOrNull()?.message ?: "Google Login failed")
                    }
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
