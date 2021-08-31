package com.example.bitlist.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.example.bitlist.Firestore.FireStoreClass
import com.example.bitlist.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this,R.color.yellow_primary);
        //covering full screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        //delay
        Handler().postDelayed({
            //goto fire store class , call fun getCurrentUserId() if current user id have something then user is already logged in
            var currentUserId= FireStoreClass().getCurrentUserId()
            if(currentUserId.isNotEmpty()){
                startActivity(Intent(this,CenterActivity::class.java))
            }else{
                startActivity(Intent(this,SignInActivity::class.java))
            }
            finish()
        },1200)

    }

}