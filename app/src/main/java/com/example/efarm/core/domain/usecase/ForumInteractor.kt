package com.example.efarm.core.domain.usecase

import androidx.paging.PagingData
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.data.source.remote.model.Topic
import com.example.efarm.core.domain.repository.IForumRepository
import com.example.efarm.core.util.KategoriTopik
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ForumInteractor @Inject constructor(
    private val repo: IForumRepository
) :ForumUseCase{
    override val currentUser: FirebaseUser?=repo.currentUser

    override fun getPagingForum(topic: Topic?): Flow<PagingData<ForumPost>> =repo.getPagingForum(topic)

    override suspend fun getListTopikForum(kategoriTopik: KategoriTopik): Flow<Resource<List<Topic>>> =repo.getListTopikForum(kategoriTopik)

    override fun likeForumPost(forumPost: ForumPost): Flow<Resource<Pair<Boolean, String?>>> =repo.likeForumPost(forumPost)

}