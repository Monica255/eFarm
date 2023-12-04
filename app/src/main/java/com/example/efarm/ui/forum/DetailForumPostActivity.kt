package com.example.efarm.ui.forum

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.eFarm.R
import com.example.eFarm.databinding.ActivityDetailForumPostBinding
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.CommentForumPost
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.util.FORUM_POST_ID
import com.example.efarm.core.util.TextFormater
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailForumPostActivity : AppCompatActivity() {
    lateinit var binding: ActivityDetailForumPostBinding
    private lateinit var adapterTopic: FilterTopicAdapter
    private lateinit var adapterComment: PagingCommentAdapter
    private val viewModel: ForumViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailForumPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapterTopic = FilterTopicAdapter { }

        val layoutManagerCommonTopic = FlexboxLayoutManager(this)
        layoutManagerCommonTopic.flexDirection = FlexDirection.ROW
        binding.rvTopic.layoutManager = layoutManagerCommonTopic
        binding.rvTopic.adapter = adapterTopic

        val layoutManager = LinearLayoutManager(this)
        binding.rvKomentar.layoutManager=layoutManager


        binding.btnClose.setOnClickListener {
            finish()
        }



        val id = intent.getStringExtra(FORUM_POST_ID)
        id?.let {
            viewModel.getDetailForum(it).observe(this) {
                when (it) {
                    is Resource.Loading -> {}
                    is Resource.Error -> {}
                    is Resource.Success -> {
                        it.data?.let {
                            setData(it)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setData(data: ForumPost) {
        viewModel.getUserdata(data.user_id).observe(this) { it ->
            it?.let {
                binding.tvUserName.text = it.name
                Glide.with(this)
                    .load(it.img_profile)
                    .placeholder(R.drawable.placeholder)
                    .into(binding.imgProfilePicture)
            }
        }
        binding.tvPostTitle.text = data.title
        binding.tvContentPost.text = data.content
        binding.iconVerified.visibility = if (data.verified != null) View.VISIBLE else View.GONE
        Glide.with(this)
            .load(data.img_header)
            .placeholder(R.drawable.placeholder)
            .into(binding.imgHeaderPost)

        binding.tvTimastamp.text = TextFormater.toPostTime(data.timestamp, this)


        adapterComment = PagingCommentAdapter(data.verified,viewModel,this)
        binding.rvKomentar.adapter = adapterComment

        lifecycleScope.launch {
            adapterComment.loadStateFlow.collectLatest { loadStates ->
                showLoading(loadStates.refresh is LoadState.Loading)
            }
        }

        lifecycleScope.launch {
            data.topics?.let {
                viewModel.getTopics(it).observe(this@DetailForumPostActivity) {
                    when (it) {
                        is Resource.Loading -> {}
                        is Resource.Error -> {
                            it.message?.let {
                                binding.tvLabelTopic.visibility = View.GONE
                            }
                        }
                        is Resource.Success -> {
                            binding.tvLabelTopic.visibility = View.VISIBLE
                            it.data?.let {
                                adapterTopic.submitList(it.toMutableList())
                            }
                        }
                    }
                    lifecycleScope.launch {
                        data.comments?.let {list->
                            if(!list.isEmpty()){
                                data.verified?.let { it1 ->
                                    viewModel.getBestComment(it1).observe(this@DetailForumPostActivity){
                                        when(it){
                                            is Resource.Loading->{showLoading(true)}
                                            is Resource.Error->{
                                                showLoading(false)
                                                it.message?.let {
                                                    Log.d("TAG","error bc "+it)
                                                }
                                            }
                                            is Resource.Success->{
                                                showLoading(false)
                                                getComments(list,it.data)
                                                Log.d("TAG","best "+it.data.toString())
                                            }
                                        }
                                    }}
                            }
                        }
                    }
                }
            }

        }

//        val test=CommentForumPost("ULabDPh14FIWwR4XA2yr","6oRRHA189WfEU3n7q1wQ","Caranya adalah sebagai berikut....","WAJzkhfQHUf6iu6KKETG9t6c4Gs1",1665589699000)

    }

    private fun getComments(comments:List<String>,bestCommnet:CommentForumPost?){
        viewModel.getComments(comments,bestCommnet).observe(this) {
            adapterComment.submitData(lifecycle,it)

        }
    }
    private fun showLoading(isShowLoading: Boolean) {
        binding.pbLoading.visibility = if (isShowLoading) View.VISIBLE else View.GONE
    }
}