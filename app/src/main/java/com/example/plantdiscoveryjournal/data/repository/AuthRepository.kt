package com.example.plantdiscoveryjournal.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Repository pour gérer l'authentification Firebase
 */
class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /**
     * Connexion avec email et mot de passe
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Utilisateur non trouvé après la connexion"))
            }
        } catch (e: Exception) {
            // Log l'erreur pour déboguer
            println("❌ Erreur signInWithEmail: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Inscription avec email et mot de passe
     */
    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Utilisateur non créé après l'inscription"))
            }
        } catch (e: Exception) {
            // Log l'erreur pour déboguer
            println("❌ Erreur signUpWithEmail: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Connexion avec Google
     */
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user

            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Utilisateur non trouvé après la connexion Google"))
            }
        } catch (e: Exception) {
            // Log l'erreur pour déboguer
            println("❌ Erreur signInWithGoogle: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Déconnexion
     */
    fun signOut() {
        firebaseAuth.signOut()
    }
}