package com.example.bitlist.activity

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.bitlist.R
import com.example.bitlist.dialogs.CustomProgressDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.progress_dialog_view.*

open class BaseActivity : AppCompatActivity() {
    private var doubleBackToExitPressedOnce=false
    private lateinit var mProgressDialog: Dialog
    val progressDialog = CustomProgressDialog()

    fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }
    //get user id from fire base
    fun getCurrentUserId():String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
    //double back press to exit
    fun doubleBackToExit(){
        if(doubleBackToExitPressedOnce){
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce=true
        Toast.makeText(this, "Please click back again to exit", Toast.LENGTH_SHORT).show()

        //after 3 seconds reset the variable to false
        Handler().postDelayed({doubleBackToExitPressedOnce=false},3000)
    }

    fun showErrorSnackBar(message:String){
        val snackBar= Snackbar.make(findViewById(android.R.id.content),message, Snackbar.LENGTH_LONG)
        val snackBarView=snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this,R.color.mehroon))//changing snackbar background color
        snackBar.show()
    }
}