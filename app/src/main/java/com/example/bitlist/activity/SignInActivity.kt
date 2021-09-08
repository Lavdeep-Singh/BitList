package com.example.bitlist.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.example.bitlist.R
import com.example.bitlist.adapters.ViewPagerAdapter
import com.example.bitlist.dialogs.CustomProgressDialog
import com.example.bitlist.fragments.SignInTabFragment
import com.example.bitlist.fragments.SignUpTabFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.fragment_sign_in_tab.*
import kotlinx.android.synthetic.main.fragment_sign_up_tab.*

class SignInActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        setUpTab()
        //tv_forget_password.setOnClickListener {
        //         supportFragmentManager.beginTransaction().replace(R.id.view_pager,FragmentForgetPassword()).commit()
       // }

    }

    private fun setUpTab(){
        val adapter=ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(SignInTabFragment(),"Sign In")
        adapter.addFragment(SignUpTabFragment(),"Sign Up")
        view_pager.adapter=adapter
        tab_layout.setupWithViewPager(view_pager)

    }

    override fun onBackPressed() {
        doubleBackToExit()
    }


}