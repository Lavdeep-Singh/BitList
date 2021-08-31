package com.example.bitlist.fragments

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.bitlist.Constants
import com.example.bitlist.Firestore.FireStoreClass
import com.example.bitlist.R
import com.example.bitlist.activity.CenterActivity
import com.example.bitlist.activity.SignInActivity
import com.example.bitlist.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.fragment_sign_up_tab.*


class SignUpTabFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_sign_up.setOnClickListener {
            registerUser()
        }

    }
    //function to generate fcm token and update in fire store
    fun generateFCMtoken(userInfo: User){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
            }
            // Get new FCM registration token
            val token = task.result
            userInfo.fcmToken=token.toString()
            mFirestore.collection(Constants.USER)  //creating collection and passing collections name
                .document(getCurrentUserId())  //inside collections create a document named uuid of user
                .set(userInfo, SetOptions.merge()).addOnSuccessListener {
                   // Toast.makeText(activity, "fcm token updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{
                        e->
                    Log.e(activity?.javaClass?.simpleName,"Error adding user to firestore")
                }
        })
    }
    private fun userRegisteredSuccess(){
        Toast.makeText((activity as? SignInActivity), "Successfully registered", Toast.LENGTH_SHORT).show()
        (activity as SignInActivity).progressDialog.dialog.dismiss()   //hide progress dialog
        //FirebaseAuth.getInstance().signOut()
        (activity as SignInActivity).startActivity(Intent(requireActivity(),CenterActivity::class.java))
        (activity as SignInActivity).finish()
    }

    private fun registerUser(){ //function to register user into firebase auth
        val name:String=et_Name_sign_up.text.toString().trim{it <= ' '} //get name and trim it from end
        val email:String=et_email_sign_up.text.toString().trim{it <= ' '}
        val password:String=et_password_sign_up.text.toString().trim{it <= ' '}
        val number:String=et_Number_sign_up.text.toString().trim{it <= ' '}

        if(validateForm(name,email,password,number)){ //validate if credentials are not empty
            (activity as SignInActivity).progressDialog.show((activity as SignInActivity),resources.getString(R.string.please_wait)) //show progress dialog
            //register user with firebase
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                if (task.isSuccessful) { //if user credentials are verified
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!
                    val user= User(firebaseUser.uid,name,registeredEmail) //creating object of User model and passing uuid and email
                    registerUser(user) //passing User object to store data in database(fire store)

                } else {
                    (activity as SignInActivity).progressDialog.dialog.dismiss()   //hide progress dialog
                    Toast.makeText((activity  as SignInActivity),"Registration failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //fun to check if passed parameters are empty
    private fun validateForm(name:String,email:String,password:String,number:String):Boolean{
        return when{
            TextUtils.isEmpty(name)->{
                (activity  as SignInActivity).showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email)->{
                (activity  as SignInActivity).showErrorSnackBar("Please enter a email address")
                false
            }
            TextUtils.isEmpty(password)->{
                (activity  as SignInActivity).showErrorSnackBar("Please enter a password")
                false
            }
            TextUtils.isEmpty(number)->{
                (activity  as SignInActivity).showErrorSnackBar("Please enter a number")
                false
            }else ->{
                true
            }
        }
    }

    private val mFirestore= FirebaseFirestore.getInstance() //instance of firestore
    private fun registerUser(userInfo: User){
        mFirestore.collection(Constants.USER)  //creating collection and passing collections name
            .document(getCurrentUserId())  //inside collections create a document named uuid of user
            .set(userInfo, SetOptions.merge()).addOnSuccessListener {
                println("Success uploading data")
                generateFCMtoken(userInfo)
                userRegisteredSuccess()

            }
            .addOnFailureListener{
                    e->
                Log.e(activity?.javaClass?.simpleName,"Error adding user to firestore")
            }
    }

    //function to get uuid of current logged in user
    private fun getCurrentUserId():String{
        var currentUser= FirebaseAuth.getInstance().currentUser
        var currentUserId=""
        if(currentUser!=null){
            currentUserId=currentUser.uid
        }
        return currentUserId
    }

}