package com.example.efarm.core.domain.repository

import androidx.paging.PagingData
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.data.source.remote.model.Topic
import com.example.efarm.core.util.KategoriTopik
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface IForumRepository {
    val currentUser:FirebaseUser?
    fun getPagingForum(
        topic: Topic?
    ): Flow<PagingData<ForumPost>>

    suspend fun getListTopikForum(kategoriTopik: KategoriTopik): Flow<Resource<List<Topic>>>

    fun likeForumPost(forumPost: ForumPost): Flow<Resource<Pair<Boolean, String?>>>
}