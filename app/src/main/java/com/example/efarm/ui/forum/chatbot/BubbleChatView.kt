package com.example.efarm.ui.forum.chatbot

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.eFarm.R
import com.example.efarm.core.data.source.remote.model.Chat
import com.google.firebase.auth.FirebaseAuth

class BubbleChatView : RelativeLayout {

    private lateinit var ll_bot: LinearLayout
    private lateinit var ll_user: LinearLayout
    private val uid = FirebaseAuth.getInstance().currentUser?.uid
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //setBackgroundResource(R.drawable.bg_card_home)
        gravity = Gravity.CENTER
    }

    private fun init(context: Context) {
        val rootView = inflate(context, R.layout.view_chat, this)


        ll_bot = rootView.findViewById(R.id.ll_chat_bot)
        ll_user = rootView.findViewById(R.id.ll_chat_user)

    }

    fun setData(data: Chat) {
        if(data.id_user!=null&&data.id_user==uid){
            ll_bot.visibility= View.GONE
            ll_user.visibility= View.VISIBLE
        }else {
            ll_bot.visibility= View.VISIBLE
            ll_user.visibility= View.GONE
        }
    }
}