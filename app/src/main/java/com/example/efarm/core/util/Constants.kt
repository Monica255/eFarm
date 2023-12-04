package com.example.efarm.core.util

import com.example.efarm.core.data.source.remote.model.ForumPost

const val FORUM_POST_ID="forum_post_id"
enum class KategoriTopik(val printable:String) {
    SEMUA(""),COMMON("common topics"), COMMODITY("commodity")
}

sealed class ViewEventsForumPost {
    data class Edit(val entity: ForumPost, val isLiked:Boolean) : ViewEventsForumPost()
    data class Remove(val entity: ForumPost) : ViewEventsForumPost()
    data class Rebind(val entity: ForumPost) : ViewEventsForumPost()
}