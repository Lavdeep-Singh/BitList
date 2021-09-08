package com.example.bitlist.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.bitlist.R
import com.example.bitlist.activity.CenterActivity
import com.example.bitlist.activity.ForgetPasswordActivity
import com.example.bitlist.activity.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_sign_in_tab.*
import kotlinx.android.synthetic.main.fragment_sign_up_tab.*

class SignInTabFragment : Fragment() {
    lateinit var auth: FirebaseAuth//instance of firebase auth to sign in

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth=FirebaseAuth.getInstance() //instance of fire base authenticator
        btn_sign_in.setOnClickListener {
            signInRegisteredUser()
        }
        tv_forget_password.setOnClickListener {
            activity?.let{
                val intent = Intent (it, ForgetPasswordActivity::class.java)
                it.startActivity(intent)
            }
        }
    }

    private fun signInRegisteredUser(){
        val email:String=et_email_sign_in.text.toString().trim { it<=' ' }
        val password:String=et_password_sign_in.text.toString().trim { it<=' ' }

        if(validateForm(email,password)){
            (activity as SignInActivity).progressDialog.show((activity as SignInActivity),resources.getString(R.string.please_wait)) //show progress dialog
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener((activity as SignInActivity)){
                    task->
                (activity as SignInActivity).progressDialog.dialog.dismiss() //hide progress dialog
                if(task.isSuccessful){//sign in successful
                    val user=auth.currentUser
                    Toast.makeText((activity as SignInActivity), "Successfully signed in", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireActivity(),CenterActivity::class.java))
                    (activity as SignInActivity).finish()

                }else{
                    //sign in fails
                    Toast.makeText((activity as SignInActivity), "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    //check if credentials are not empty
    private fun validateForm(email:String,password:String):Boolean{
        return when{
            TextUtils.isEmpty(email)->{
                (activity as SignInActivity).showErrorSnackBar("Please enter a email address")
                false
            }
            TextUtils.isEmpty(password)->{
                (activity as SignInActivity).showErrorSnackBar("Please enter a password")
                false
            }else ->{
                true
            }
        }
    }


}