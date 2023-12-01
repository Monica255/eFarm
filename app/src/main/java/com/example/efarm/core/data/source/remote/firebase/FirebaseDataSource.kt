package com.example.efarm.core.data.source.remote.firebase

import android.content.Context
import android.util.Log
import com.example.eFarm.R
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDataSource @Inject constructor(
    firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth,
    firebaseFirestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) {
    val currentUser: FirebaseUser?
        get() {
            return firebaseAuth.currentUser
        }
    private val userDataRef = firebaseDatabase.reference.child("user_data/")
    suspend fun registerAccount(
        email: String,
        pass: String,
        name: String,
        telepon: String,
    ): Flow<Resource<String>> {
        return flow {
            try {
                emit(Resource.Loading())
                val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
                if (result.user != null) {
                    val x = setUserData(email, name, telepon)
//                    setConnectionData(true)
                    emit(Resource.Success(x.second))
                } else {
                    emit(Resource.Error<String>("Error"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.toString()))
                Log.e("TAG", e.toString())
            }
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun setUserData(
        email: String = "",
        name: String = "",
        telepon: String = ""
    ): Pair<Boolean, String> {
        if (name != "") {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            currentUser?.updateProfile(profileUpdates)
        }

        val photo = if (currentUser?.photoUrl != null) currentUser?.photoUrl.toString() else ""
        val user = UserData(
            currentUser?.email ?: email,
            currentUser?.displayName ?: name,
            telepon,
            currentUser?.uid ?: "",
            photo
        )
        var result: Pair<Boolean, String> = Pair(true, "Gagal masuk")
        currentUser?.uid?.let { it ->
            userDataRef.child(it).setValue(user).addOnCompleteListener { v ->
                result = if (v.isSuccessful) {
                    if (name != "") {
                        Pair(false, "Berhasil masuk")
                    } else {
                        Pair(false, "Berhasil masuk dengan akun google")
                    }
                } else {
                    Pair(true, v.exception?.message.toString()).also {
                        currentUser?.delete()
                        signOut()
                    }
                }
            }.await()
        }
        return result
    }

    fun login(email: String, pass: String): Flow<Resource<String>> {
        return flow {
            try {
                emit(Resource.Loading())
                val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
                result?.let {
                    if (result.user != null) {
                        emit(Resource.Success(context.getString(R.string.berhasil_login)))
//                        setConnectionData(true)
                    } else {
                        emit(Resource.Error(context.getString(R.string.email_atau_password_mungkin_salah)))
                    }
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.toString()))
                Log.e("TAG", e.toString())
            }
        }
    }

    fun signOut() {
        if (currentUser != null) {
            firebaseAuth.signOut()
        }
    }
}