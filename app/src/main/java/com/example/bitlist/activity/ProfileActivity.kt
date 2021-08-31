package com.example.bitlist.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.bitlist.Constants
import com.example.bitlist.Firestore.FireStoreClass
import com.example.bitlist.R
import com.example.bitlist.models.User
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.appbar_main.*
import java.io.IOException

class ProfileActivity : BaseActivity() {
    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL: String = ""
    private lateinit var LoggedInUserDetails: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setUpActionBar()
        progressDialog.show(this, resources.getString(R.string.please_wait))
        FireStoreClass().loadUserData(this)  //load user data into navigation view

        //click listener on user image to update user image
        iv_user_image.setOnClickListener {
            //if permission to write external storage is granted
            if (ContextCompat.checkSelfPermission(
                    this@ProfileActivity,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this) //open gallery to choose image
            } else { //ask for permissions
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        //button to update profile
        btn_update_profile.setOnClickListener {
            if (mSelectedImageFileUri != null) { //
                uploadUserImage()
            } else {
                progressDialog.show(
                    this,
                    resources.getString(R.string.please_wait)
                ) //show progress dialog
                updateUserProfileData()
            }
        }
    }

    private fun uploadUserImage() {
        progressDialog.show(this, resources.getString(R.string.please_wait)) //show progress dialog
        if (mSelectedImageFileUri != null) { //if user have selected an image
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference
                    .child(
                        "USER_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                            this,
                            mSelectedImageFileUri
                        )
                    ) //name of the file uploaded in the firebase
            sRef.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener {   //upload file in the firebase and on Success
                        taskSnapshot ->
                    Log.i(
                        "Firebase Image url",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { //get downloadable link of image uploaded to storage
                            uri ->
                        mProfileImageURL =
                            uri.toString()  //storing download link of image in local variable
                        updateUserProfileData()  //call function to check if any changes have been made by the user
                    }
                }.addOnFailureListener { //not able to upload file
                    exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                progressDialog.dialog.dismiss() //hide progress dialog
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    fun updateUserProfileData() {  //creating hashmap of updated values by the user
        val userHashMap = HashMap<String, Any>()  //create a hashmap
        println("inside updateUserProfileData")
        var anyChangesMade = false
        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != LoggedInUserDetails.image) { //if imageUrl is not empty and imageUrl is not same as in database
            userHashMap[Constants.IMAGE] =
                mProfileImageURL   //then assign new url of image in hashMap which will be updated in fire store
            anyChangesMade = true                             //changes have been made
        }
        if (tv_user_name_profile_activity.text.toString() != LoggedInUserDetails.name) {
            userHashMap[Constants.NAME] = tv_user_name_profile_activity.text.toString()
            anyChangesMade = true
        }
        if (tv_user_mobile_profile_activity.text.toString() != LoggedInUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = tv_user_mobile_profile_activity.text.toString().toLong()
            anyChangesMade = true
        }
        if (anyChangesMade) {
            FireStoreClass().updateUserProfileData(
                this,
                userHashMap
            )   //fun to upload data in fire store
        }
        else{
            showErrorSnackBar("No changes have been made")
            progressDialog.dialog.dismiss() //hide progress dialog
        }
    }

    //fun to get result if permission was granted by the user
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)  //call this function to show gallery to user to choose image
            } else {//permission not granted
                Toast.makeText(
                    this,
                    "You denied permissions, they can be allowed from the settings",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // user returns to current activity after choosing image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //user have selected an image and data is not empty then get image uri in variable
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            mSelectedImageFileUri = data.data
            //then try to set image uri to our image view
            try {
                Glide
                    .with(this@ProfileActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.logotwo)
                    .into(iv_user_image);
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun profileUpdateSuccess() {
        progressDialog.dialog.dismiss() //hide progress dialog
        setResult(Activity.RESULT_OK) //set result code ok so that center activity will know user have made some changes
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_profile_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back)
            tv_profile_activity_toolbar_title.text = resources.getString(R.string.profile)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolbar_profile_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setContentOnProfile(loggedInUserData: User) {
        LoggedInUserDetails = loggedInUserData   //global variable to hold user details
        tv_user_name_profile_activity.setText(loggedInUserData.name)
        tv_user_email_profile_activity.text = loggedInUserData.email
        tv_user_mobile_profile_activity.setText(loggedInUserData.mobile.toString())
        //use glide to populate header image
        Glide
            .with(this@ProfileActivity)
            .load(loggedInUserData.image)     //load this image
            .centerCrop()
            .placeholder(R.drawable.logotwo)  //default image
            .into(iv_user_image);             //populate this image view here
        progressDialog.dialog.dismiss() //hide progress dialog

    }

}