package com.example.kaizenfrontend.feature.auth.presentation.signup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.di.hiltServiceEntryPoint
import com.example.kaizenfrontend.feature.auth.data.repository.AuthRepositoryImpl
import com.example.kaizenfrontend.feature.auth.domain.usecase.RegisterUseCase
import com.example.kaizenfrontend.feature.auth.domain.validation.AuthInputValidator
import com.example.kaizenfrontend.feature.auth.domain.validation.AuthValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SignUpUiState {
    object Idle : SignUpUiState()
    object Loading : SignUpUiState()
    data class Success(val needsCalibration: Boolean) : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()
}

class SignUpViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext
    private val serviceEntryPoint = appContext.hiltServiceEntryPoint()
    private val sessionManager = SessionManager(context)
    private val authRepository = AuthRepositoryImpl(serviceEntryPoint.authApiService(), sessionManager)
    private val registerUseCase = RegisterUseCase(authRepository)

    private val userRepository = com.example.kaizenfrontend.feature.user.data.repository.UserRepositoryImpl(
        serviceEntryPoint.userApiService(),
        sessionManager
    )

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, confirmPassword: String) {
        val normalizedEmail = AuthInputValidator.normalizeEmail(email)
        val validationResult = AuthInputValidator.validateRegistration(
            email = normalizedEmail,
            password = password,
            confirmPassword = confirmPassword
        )

        if (validationResult != AuthValidationResult.Valid) {
            _uiState.value = SignUpUiState.Error(validationResult.toMessage())
            return
        }
        viewModelScope.launch {
            _uiState.value = SignUpUiState.Loading
            val result = registerUseCase(normalizedEmail, password)
            if (result.isSuccess) {
                val token = result.getOrThrow()
                sessionManager.awaitTokenPersistence(token)
                checkCalibrationAndEmitSuccess(token)
            } else {
                _uiState.value = SignUpUiState.Error(result.exceptionOrNull()?.message ?: appContext.getString(com.example.kaizenfrontend.R.string.auth_error_registration_failed))
            }
        }
    }

    private fun AuthValidationResult.toMessage(): String = when (this) {
        AuthValidationResult.Valid -> appContext.getString(com.example.kaizenfrontend.R.string.auth_error_registration_failed)
        AuthValidationResult.EmptyFields -> appContext.getString(com.example.kaizenfrontend.R.string.auth_error_fill_all_fields)
        AuthValidationResult.InvalidEmail -> appContext.getString(com.example.kaizenfrontend.R.string.auth_error_invalid_email)
        AuthValidationResult.EmailTooLong -> appContext.getString(com.example.kaizenfrontend.R.string.auth_error_email_too_long)
        AuthValidationResult.PasswordTooShort -> appContext.getString(com.example.kaizenfrontend.R.string.auth_error_password_too_short)
        AuthValidationResult.PasswordTooLong -> appContext.getString(com.example.kaizenfrontend.R.string.auth_error_password_too_long)
        AuthValidationResult.PasswordsDoNotMatch -> appContext.getString(com.example.kaizenfrontend.R.string.auth_error_passwords_do_not_match)
    }

    /**
     * Fetches the user profile using [token] directly so the Authorization header
     * is guaranteed fresh, not read from SharedPreferences after an async write.
     * Calibration gate: only primaryGoal matters — null means brand-new account.
     */
    private suspend fun checkCalibrationAndEmitSuccess(
        token: String,
        fromGoogleSignUp: Boolean = false
    ) {
        val profileResult = runCatching {
            userRepository.getCurrentUserWithToken(token)
        }.getOrElse { e ->
            android.util.Log.e("SignUpVM", "Profile fetch threw: ${e.message}", e)
            Result.failure(e)
        }

        if (profileResult.isSuccess) {
            val user = profileResult.getOrNull()
            val isCalibrated = user?.primaryGoal?.isNotBlank() == true
            val showGoogleWelcome = fromGoogleSignUp && !isCalibrated
            android.util.Log.d("SignUpVM", "Profile fetched OK. primaryGoal=${user?.primaryGoal}, isCalibrated=$isCalibrated")
            sessionManager.saveCalibrationComplete(isCalibrated)
            sessionManager.setShouldShowGoogleWelcomePrompt(showGoogleWelcome)
            _uiState.value = SignUpUiState.Success(needsCalibration = !isCalibrated)
        } else {
            val err = profileResult.exceptionOrNull()
            android.util.Log.e("SignUpVM", "Profile fetch FAILED after successful auth: ${err?.message}", err)
            sessionManager.saveCalibrationComplete(false)
            sessionManager.setShouldShowGoogleWelcomePrompt(false)
            _uiState.value = SignUpUiState.Success(needsCalibration = true)
        }
    }

    fun resetState() {
        _uiState.value = SignUpUiState.Idle
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = SignUpUiState.Loading
            try {
                val rawNonce = java.util.UUID.randomUUID().toString()
                val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(rawNonce.toByteArray())
                val hashedNonce = bytes.joinToString("") { "%02x".format(it) }

                val credentialManager = androidx.credentials.CredentialManager.create(context)
                val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("864644262862-5p3b6cvaj31ee7adcl5tfq9d1rmt8sls.apps.googleusercontent.com")
                    .setAutoSelectEnabled(false)
                    .setNonce(hashedNonce)
                    .build()

                val request = androidx.credentials.GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is androidx.credentials.CustomCredential && 
                    credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    
                    val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    
                    val loginResult = authRepository.googleLogin(idToken)
                    if (loginResult.isSuccess) {
                        val token = loginResult.getOrThrow()
                        sessionManager.awaitTokenPersistence(token)
                        checkCalibrationAndEmitSuccess(
                            token = token,
                            fromGoogleSignUp = true
                        )
                    } else {
                        _uiState.value = SignUpUiState.Error(loginResult.exceptionOrNull()?.message ?: appContext.getString(com.example.kaizenfrontend.R.string.auth_error_google_sign_in_failed))
                    }
                } else {
                    _uiState.value = SignUpUiState.Error(appContext.getString(com.example.kaizenfrontend.R.string.auth_error_unexpected_credential_type))
                }
            } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                _uiState.value = SignUpUiState.Idle
            } catch (e: Exception) {
                _uiState.value = SignUpUiState.Error(e.message ?: appContext.getString(com.example.kaizenfrontend.R.string.auth_error_google_sign_in_failed))
            }
        }
    }
}
