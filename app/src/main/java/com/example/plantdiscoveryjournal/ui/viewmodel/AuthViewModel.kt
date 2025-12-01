package com.example.plantdiscoveryjournal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.example.plantdiscoveryjournal.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer l'authentification
 */
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val user = authRepository.currentUser
        _authState.value = if (user != null) {
            AuthState.Authenticated(user.uid)
        } else {
            AuthState.Unauthenticated
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Unauthenticated // Reset l'état d'erreur précédent

            authRepository.signInWithEmail(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user.uid)
                    _isLoading.value = false
                }
                .onFailure { error ->
                    val rawMessage = error.message ?: ""

                    val errorMessage = when {
                        // Email ou mot de passe incorrect
                        rawMessage.contains("password is invalid", ignoreCase = true) ||
                                rawMessage.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                                rawMessage.contains("The supplied auth credential is incorrect", ignoreCase = true) ->
                            "Email or password incorrect"

                        // Aucun compte avec cet email
                        rawMessage.contains("no user record", ignoreCase = true) ||
                                rawMessage.contains("USER_NOT_FOUND", ignoreCase = true) ->
                            "No account found with this email"

                        // Email mal formé
                        rawMessage.contains("badly formatted", ignoreCase = true) ||
                                rawMessage.contains("INVALID_EMAIL", ignoreCase = true) ->
                            "Invalid email format"

                        // Erreur réseau
                        rawMessage.contains("network", ignoreCase = true) ||
                                rawMessage.contains("A network error", ignoreCase = true) ->
                            "Network connection error"

                        else -> "Unknown authentication error"
                    }

                    _authState.value = AuthState.Error(errorMessage)
                    _isLoading.value = false
                }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Unauthenticated // Reset l'état d'erreur précédent

            authRepository.signUpWithEmail(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user.uid)
                    _isLoading.value = false
                }
                .onFailure { error ->
                    val rawMessage = error.message ?: ""

                    val errorMessage = when {
                        rawMessage.contains("email address is already in use", ignoreCase = true) ||
                                rawMessage.contains("EMAIL_EXISTS", ignoreCase = true) ->
                            "This email is already in use"

                        rawMessage.contains("password should be at least 6 characters", ignoreCase = true) ||
                                rawMessage.contains("WEAK_PASSWORD", ignoreCase = true) ->
                            "Password must be at least 6 characters"

                        rawMessage.contains("badly formatted", ignoreCase = true) ||
                                rawMessage.contains("INVALID_EMAIL", ignoreCase = true) ->
                            "Invalid email format"

                        rawMessage.contains("network", ignoreCase = true) ||
                                rawMessage.contains("A network error", ignoreCase = true) ->
                            "Network connection error"

                        else -> "Unknown sign up error"
                    }

                    _authState.value = AuthState.Error(errorMessage)
                    _isLoading.value = false
                }
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Unauthenticated // Reset l'état d'erreur précédent

            authRepository.signInWithGoogle(account)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user.uid)
                    _isLoading.value = false
                }
                .onFailure { error ->
                    val rawMessage = error.message ?: ""

                    val errorMessage = when {
                        rawMessage.contains("network", ignoreCase = true) ||
                                rawMessage.contains("A network error", ignoreCase = true) ->
                            "Network connection error"

                        rawMessage.contains("cancelled", ignoreCase = true) ->
                            "Google sign in cancelled"

                        else -> "Unknown Google sign in error"
                    }

                    _authState.value = AuthState.Error(errorMessage)
                    _isLoading.value = false
                }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
