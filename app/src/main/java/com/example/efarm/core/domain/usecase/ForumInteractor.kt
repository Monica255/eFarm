package com.example.efarm.core.domain.usecase

import androidx.paging.PagingData
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.CommentForumPost
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
    override fun getDetailForum(idForum: String): Flow<Resource<ForumPost>> = repo.getDetailForum(idForum)
    override suspend fun getTopics(topics: List<String>): Flow<Resource<List<Topic>>> = repo.getTopics(topics)
    override fun getComments(comments:List<String>,idBestComment:CommentForumPost?): Flow<PagingData<CommentForumPost>> = repo.getComments(comments,idBestComment)
    override suspend fun getBestComment(idComment: String): Flow<Resource<CommentForumPost>> = repo.getBestComment(idComment)

}