package com.canteen.data.repository

import com.canteen.domain.model.AuthUser
import com.canteen.domain.repository.AuthRepository
import com.canteen.utils.awaitResult
import com.google.firebase.auth.FirebaseAuth

class FirebaseAuthRepository(
    private val auth: FirebaseAuth
) : AuthRepository {

    override suspend fun register(email: String, password: String): Result<AuthUser> =
        auth.createUserWithEmailAndPassword(email, password)
            .awaitResult()
            .map { result ->
                val user = requireNotNull(result.user)
                AuthUser(id = user.uid, email = user.email)
            }

    override suspend fun login(email: String, password: String): Result<AuthUser> =
        auth.signInWithEmailAndPassword(email, password)
            .awaitResult()
            .map { result ->
                val user = requireNotNull(result.user)
                AuthUser(id = user.uid, email = user.email)
            }

    override fun currentUserId(): String? = auth.currentUser?.uid
}
