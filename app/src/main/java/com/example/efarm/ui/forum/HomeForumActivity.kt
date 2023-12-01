package com.example.efarm.ui.forum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.eFarm.R
import com.example.eFarm.databinding.ActivityHomeForumBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeForumActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeForumBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityHomeForumBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}