package com.example.efarm.ui.forum.chatbot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.eFarm.R
import com.example.eFarm.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {
    lateinit var binding:ActivityChatBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClose.setOnClickListener{
            finish()
        }
    }
}