package com.example.efarm.core.data.source.remote.firebase

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.eFarm.R
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.CommentForumPost
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.data.source.remote.model.Topic
import com.example.efarm.core.data.source.remote.model.UserData
import com.example.efarm.core.util.KategoriTopik
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
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
    private val commentRef = firebaseFirestore.collection("forum_comments")

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

    fun getUserData(uid: String?): MutableLiveData<UserData?> {
        val userData = MutableLiveData<UserData?>()
        val x = uid ?: currentUser?.uid
        x?.let {
            userDataRef.child(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            try {
                                val user = snapshot.getValue(UserData::class.java)
                                userData.value = user
                            } catch (e: Exception) {
                                userData.value = null
                                Log.d("TAG", e.message.toString())
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        userData.value = null
                        Log.d("TAG", error.toString())
                    }

                })
        }
        return userData
    }
    fun signOut() {
        if (currentUser != null) {
            firebaseAuth.signOut()
        }
    }

    private val forumRef = firebaseFirestore.collection("forum_posts")
    fun getPagingForum(
        topic: Topic? = null
    ): Flow<PagingData<ForumPost>> {
        var query:Query
        if(topic!=null){
            query = forumRef.orderBy("timestamp", Query.Direction.DESCENDING).whereArrayContains("topics",topic.topic_id)
                .limit(4)
        }else{
            query = forumRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(4)
        }

        val pager = Pager(
            config = PagingConfig(
                pageSize = 4
            ),
            pagingSourceFactory = {
                ForumPostPagingStore(query)
            }
        )
        return pager.flow
    }

    fun getComments(comments:List<String>,idBestComment:CommentForumPost?): Flow<PagingData<CommentForumPost>> {
        val query:Query=commentRef.orderBy("timestamp",Query.Direction.DESCENDING).whereIn("id_comment",comments).limit(3)
        val pager = Pager(
            config = PagingConfig(
                pageSize = 3
            ),
            pagingSourceFactory = {
                CommentForumPagingStore(query,idBestComment)
            }
        )
        return pager.flow
    }

//    fun getComments(idForum:String,idBestComment:CommentForumPost?): Flow<PagingData<CommentForumPost>> {
//        val query:Query=commentRef.orderBy("timestamp",Query.Direction.DESCENDING).whereIn("id_comment",idForum).limit(3)
//        val pager = Pager(
//            config = PagingConfig(
//                pageSize = 3
//            ),
//            pagingSourceFactory = {
//                CommentForumPagingStore(query,idBestComment)
//            }
//        )
//        return pager.flow
//    }

    private val topicRef = firebaseFirestore.collection("topics")
    private fun getQueryTopicByCategory(kategori: KategoriTopik): Query {
        return if (kategori != KategoriTopik.SEMUA) {
            topicRef.orderBy("topic_name", Query.Direction.ASCENDING)
                .whereEqualTo("topic_category", kategori.printable)
        } else {
            topicRef.orderBy("topic_name", Query.Direction.ASCENDING)
        }
    }

    suspend fun getListTopikForum(kategory: KategoriTopik): Flow<Resource<List<Topic>>> {
        var list = mutableListOf<Topic>()
        var msg: String? = null
        return flow {
            emit(Resource.Loading())
            val query = getQueryTopicByCategory(kategory)
            query.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    try {
                        for (i in it.result) {
                            val x = i.toObject<Topic>()
                            list.add(x)
                        }
                    } catch (e: Exception) {
                        msg = e.message
                        list = mutableListOf()
                    }
                } else {
                    msg = "Error"
                    list = mutableListOf()
                }
            }.await()

            if (msg != null) {
                emit(Resource.Error(msg!!))
            } else {
                emit(Resource.Success(list))
            }
        }
    }

    suspend fun getTopics(topics:List<String>): Flow<Resource<List<Topic>>>{
        var list= mutableListOf<Topic>()
        var msg:String?=context.getString(R.string.gagal_mendapatkan_data)
        return flow{
            topicRef.whereIn("topic_id",topics).get().addOnCompleteListener {
                if(it.isSuccessful){
                    msg=null
                    try{
                        for(i in it.result){
                            val x=i.toObject<Topic>()
                            list.add(x)
                        }
                    }catch (e:Exception){
                        Log.d("TAG","unsuccessful")
                    }
                }
            }.await()
            if (msg != null) {
                emit(Resource.Error(msg!!))
            } else {
                emit(Resource.Success(list))
            }
        }
    }

    private fun isContainUid(list: MutableList<String>): Boolean {
        return list.contains(currentUser?.uid)
    }
    fun likeForumPost(forumPost: ForumPost): Flow<Resource<Pair<Boolean, String?>>> {
        return flow {
            emit(Resource.Loading())
            val favorite: Boolean
            val list = forumPost.likes ?: mutableListOf()
            val s: FieldValue = if (isContainUid(list)) {
                favorite = false
                FieldValue.arrayRemove(currentUser?.uid)
            } else {
                favorite = true
                FieldValue.arrayUnion(currentUser?.uid)
            }
            var successMsg: Pair<Boolean, String?>? = null
            var errorMsg = ""
            forumRef.document(forumPost.id_forum_post).update("likes", s)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        forumRef.document(forumPost.id_forum_post)
                            .update("like_count", FieldValue.increment(if (favorite) 1 else -1))
                        successMsg = Pair(
                            favorite,
                            currentUser?.uid
                        )
                    } else {
                        errorMsg =
                            if (favorite) "Gagal menyukai postingan" else "Gagal menghapus suka paka postingan"
                    }
                }.await()
            if (successMsg != null) {
                emit(Resource.Success(successMsg!!))
            } else emit(Resource.Error(errorMsg))
        }
    }

    fun getDetailForum(idForum:String): Flow<Resource<ForumPost>> {
        var x:ForumPost?=null
        var msg = context.getString(R.string.gagal_mendapatkan_data)
        return  flow{
            emit(Resource.Loading())
            forumRef.document(idForum).get().addOnCompleteListener {
                if(it.isSuccessful){
                    try {
                        x = it.result.toObject<ForumPost>()
                    } catch (e: Exception) {
                        msg = e.message.toString()                   }
                }else{
                    Log.d("TAG","unsuccessful")
                }
            }.await()
            if (x != null) {
                emit(Resource.Success(x!!))
            } else {
                emit(Resource.Error(msg))
            }
        }
    }

    suspend fun getBestComment(idComment:String):Flow<Resource<CommentForumPost>>{
        var x:CommentForumPost?=null
        var msg = context.getString(R.string.gagal_mendapatkan_data)
        return  flow{
            emit(Resource.Loading())
            commentRef.document(idComment).get().addOnCompleteListener {
                if(it.isSuccessful){
                    try {
                        x = it.result.toObject<CommentForumPost>()
                    } catch (e: Exception) {
                        msg = e.message.toString()                   }
                }else{
                    msg="Unsuccesful"
                    Log.d("TAG","unsuccessful")
                }
            }.await()
            if (x != null) {
                emit(Resource.Success(x!!))
            } else {
                emit(Resource.Error(msg))
            }
        }
    }
}