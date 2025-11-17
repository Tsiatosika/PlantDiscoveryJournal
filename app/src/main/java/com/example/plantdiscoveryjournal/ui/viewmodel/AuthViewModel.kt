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
            authRepository.signInWithEmail(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user.uid)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(
                        error.localizedMessage ?: "Erreur de connexion"
                    )
                }
            _isLoading.value = false
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.signUpWithEmail(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user.uid)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(
                        error.localizedMessage ?: "Erreur d'inscription"
                    )
                }
            _isLoading.value = false
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.signInWithGoogle(account)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user.uid)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(
                        error.localizedMessage ?: "Erreur de connexion Google"
                    )
                }
            _isLoading.value = false
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