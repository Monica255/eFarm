package com.example.efarm.core.data.source.repository

import androidx.paging.PagingData
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.firebase.FirebaseDataSource
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.data.source.remote.model.Topic
import com.example.efarm.core.domain.repository.IForumRepository
import com.example.efarm.core.util.KategoriTopik
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ForumRepository @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource
):IForumRepository {
    override val currentUser: FirebaseUser?=firebaseDataSource.currentUser
    override fun getPagingForum(topic: Topic?): Flow<PagingData<ForumPost>> =firebaseDataSource.getPagingForum(topic)
    override suspend fun getListTopikForum(kategoriTopik: KategoriTopik): Flow<Resource<List<Topic>>> =firebaseDataSource.getListTopikForum(kategoriTopik)
    override fun likeForumPost(forumPost: ForumPost): Flow<Resource<Pair<Boolean, String?>>> =firebaseDataSource.likeForumPost(forumPost)
}