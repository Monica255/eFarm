package com.example.efarm.ui.forum

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eFarm.R
import com.example.eFarm.databinding.ActivityHomeForumBinding
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.data.source.remote.model.Topic
import com.example.efarm.core.util.FORUM_POST_ID
import com.example.efarm.core.util.KategoriTopik
import com.example.efarm.core.util.ViewEventsForumPost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

interface OnGetDataTopic {
    fun handleDataTopic(data: Topic?)
}
@AndroidEntryPoint
class HomeForumActivity : AppCompatActivity(),OnGetDataTopic {
    private lateinit var binding: ActivityHomeForumBinding
    private lateinit var adapterForum: PagingForumAdapter
    private val viewModel: ForumViewModel by viewModels()
    private var tempPost:ForumPost?=null

    private val onCLick: ((ForumPost) -> Unit) = { post ->
        val intent = Intent(this, DetailForumPostActivity::class.java)
        intent.putExtra(FORUM_POST_ID, post.id_forum_post)
        startActivity(intent)

    }

    private val onCheckChanged: ((ForumPost) -> Unit) = { post ->
        tempPost=post
        viewModel.likeForumPost(post).observe(this){
            when(it){
                is Resource.Success->{
                    it.data?.let {
                        if(tempPost!=null)viewModel.onViewEvent(ViewEventsForumPost.Edit(tempPost!!,it.first));tempPost=null
                    }
                }
                is Resource.Error->{
                    Toast.makeText(binding.root.context, it.message.toString(),Toast.LENGTH_SHORT).show()
                    tempPost?.let { it ->
                        viewModel.onViewEvent(ViewEventsForumPost.Rebind(it))
                        tempPost=null
                    }
                }

                else -> {}
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeForumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar()


        val layoutManagerForumPost = LinearLayoutManager(this)
        binding.rvForumPost.layoutManager = layoutManagerForumPost


        adapterForum = PagingForumAdapter(onCLick,onCheckChanged,viewModel,this)

        binding.rvForumPost.adapter = adapterForum

        binding.btnTopikPostForum.setOnClickListener {
            val topicFragment= ForumTopicFragment()
            topicFragment.show(supportFragmentManager,"topic_dialog")
        }

        lifecycleScope.launch {
            adapterForum.loadStateFlow.collectLatest { loadStates ->
                showLoading(loadStates.refresh is LoadState.Loading)
            }
        }

        viewModel.getData()
        viewModel.pagingData.observe(this) { it ->
            it.observe(this) {
                if (it != null) {
                    adapterForum.submitData(lifecycle, it)
                }
            }

        }

        lifecycleScope.launch {
            viewModel.getListTopik(KategoriTopik.COMMON).observe(this@HomeForumActivity) {
                when (it) {
                    is Resource.Loading -> {
                        Log.d("TAG","Loading common topics")

                    }
                    is Resource.Success -> {
                        if(it.data==null||it.data.isEmpty()){
                            Log.d("TAG","common null")
                        }

                        it.data?.toMutableList()?.let { it1 ->
                            viewModel.topicsCommon.value=it1
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@HomeForumActivity, "Failed to get topics",Toast.LENGTH_SHORT).show()
                    }
                }
            }

            viewModel.getListTopik(KategoriTopik.COMMODITY).observe(this@HomeForumActivity) {
                when (it) {
                    is Resource.Loading -> {
                        Log.d("TAG","Loading commodity")
                    }
                    is Resource.Success -> {
                        if(it.data==null||it.data.isEmpty()){
                            Log.d("TAG","commodity nul")

//                            binding.tvLabelCommoditiesTopic.visibility=View.GONE
                        }
                        it.data?.toMutableList()?.let { it1 ->
                            viewModel.topicsCommodity.value=it1
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@HomeForumActivity, "Failed to get topics",Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }

    }

    private fun showLoading(isShowLoading: Boolean) {
        binding.pbLoading.visibility = if (isShowLoading) View.VISIBLE else View.GONE
    }


    private fun setActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
    }

    override fun handleDataTopic(data: Topic?) {
        binding.btnTopikPostForum.text= data?.topic_name ?: getString(R.string.semua)
        viewModel.mTopics=data
        viewModel.getData(data)
    }
}