package com.example.bitlist.activity

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.bitlist.Constants
import com.example.bitlist.Firestore.FireStoreClass
import com.example.bitlist.R
import com.example.bitlist.models.Board
import com.example.bitlist.models.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.android.synthetic.main.activity_create_new_task.*
import kotlinx.android.synthetic.main.activity_task_list.*
import kotlinx.android.synthetic.main.activity_task_list.toolbar_task_list
import java.io.FileDescriptor
import java.io.IOException
import java.util.*

class CreateNewTaskActivity : BaseActivity() {
    private var bitmap: Bitmap? = null

    private lateinit var mBoardDetails:Board
    private lateinit var mTaskDetails:Task
    private var position:Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_task)
        setUpActionBar()

        //get the current board details
        if(intent.hasExtra(Constants.BOARD_DETAILS)){
           mBoardDetails= intent.getParcelableExtra<Board>(Constants.BOARD_DETAILS)!!
        }
        if(intent.hasExtra(Constants.BOARD_DETAILS ) && intent.hasExtra(Constants.POSITION) && intent.hasExtra(Constants.TASK_DETAILS)){
            mBoardDetails=intent.getParcelableExtra<Board>(Constants.BOARD_DETAILS)!!
            mTaskDetails=intent.getParcelableExtra<Task>(Constants.TASK_DETAILS)!!
            position=intent.getIntExtra(Constants.POSITION,-1)

            btn_create_new_task_list.text=resources.getString(R.string.update)
            et_new_task_list_title.setText(mTaskDetails.title)
            et_new_task_list_description.setText(mTaskDetails.description)
        }

        btn_create_new_task_list.setOnClickListener {
            if(position==-1){
                //when create button is clicked, add new task in the mBoardDetails and send to fire store to update firestore
                if(et_new_task_list_description.text.isNotEmpty()   && et_new_task_list_title.text.isNotEmpty() ){
                    val task= Task(
                        et_new_task_list_title.text.toString(),
                        FireStoreClass().getCurrentUserId(),
                        et_new_task_list_description.text.toString()
                    )
                    mBoardDetails.taskList.add(task)
                    progressDialog.show(this)
                    FireStoreClass().addUpdateTaskList(this,mBoardDetails)
                    //TODO ADD TASK TO BOARD'S TASK LIST ARRAY IN FIRE STORE THEN FINISH THIS ACTIVITY
                }else{
                    showErrorSnackBar("Please enter something")
                }
            }
            //means activity is opened to update the task
            if(position!=-1){
                if(et_new_task_list_description.text.isNotEmpty() && et_new_task_list_title.text.isNotEmpty()){
                    val task= Task(
                        et_new_task_list_title.text.toString(),
                        FireStoreClass().getCurrentUserId(),
                        et_new_task_list_description.text.toString()
                    )
                    mBoardDetails.taskList[position]=task
                    progressDialog.show(this)
                    FireStoreClass().addUpdateTaskList(this,mBoardDetails)
                }
            }

        }
        //for image to text
        iv_image_search.setOnClickListener {
            if (!checkStoragePermission()) {
                requestPermissions(listOf(storage_permission).toTypedArray(), 100)
            } else {
                pickImage()
            }
        }
        iv_mic.setOnClickListener{
            askSpeechInput()
        }

    }
    //text to speech implementation
    private fun askSpeechInput() {
        if(!SpeechRecognizer.isRecognitionAvailable(this)){
            Toast.makeText(this, "speech recognition not available", Toast.LENGTH_SHORT).show()
        }else{
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say something!!!")
            startActivityForResult(i, RQ_SPEECH_REC)
        }
    }

    //check storage permission
    private fun checkStoragePermission(): Boolean {
        return checkSelfPermission(storage_permission) == PackageManager.PERMISSION_GRANTED
    }
    //fun to open gallery and pick image
    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
    }
    //after user have selected an image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_IMAGE -> {
                when (resultCode) {
                    RESULT_OK -> {
                        data?.data?.let {
                            //path_et.setText(it.path)
                            Log.e(ContentValues.TAG, "Uri: $it")
                            bitmap = null
                            bitmap = getBitmapFromUri(it);
                            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                            bitmap?.let {
                                val image = InputImage.fromBitmap(it, 0)
                                recognizer.process(image)
                                    .addOnSuccessListener { visionText ->
                                        // textBlocks -> will return list of block of detected text
                                        // lines -> will return list of detected lines
                                        // elements -> will return list of detected words
                                        // boundingBox -> will return rectangle box area in bitmap
                                        if(et_new_task_list_description.text.isNotEmpty()){
                                            et_new_task_list_description.append("\n"+visionText.text)
                                        }else{
                                            et_new_task_list_description.append(visionText.text)
                                        }
                                        //Toast.makeText(this, visionText.text, Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error: " + e.message, Toast.LENGTH_SHORT).show()
                                    }
                            }
                            if (bitmap == null) Toast.makeText(this, "Please select image!", Toast.LENGTH_SHORT)
                                .show()
                        }

                    }
                    RESULT_CANCELED -> {
                        bitmap = null
                        Toast.makeText(this, "Please select valid image!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            RQ_SPEECH_REC->{
                when(resultCode){
                    RESULT_OK->{
                        val result=data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        if(et_new_task_list_description.text.isNotEmpty()){
                            et_new_task_list_description.append("\n"+result?.get(0).toString())
                        }else{
                            et_new_task_list_description.append(result?.get(0).toString())
                        }

                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                if (isAllPermissionGranted(permissions)) {
                    pickImage()
                } else {
                    Toast.makeText(this, "Please grant storage permission!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
    private fun isAllPermissionGranted(permissions: Array<out String>): Boolean {
        permissions.forEach {
            if (checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    fun addUpdateTaskListSuccess(){
        setResult(Activity.RESULT_OK)//task list activity will know that user have created a new task
        progressDialog.dialog.dismiss()
        finish()

    }
    private fun setUpActionBar() {
        setSupportActionBar(toolbar_new_task_list)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolbar_new_task_list.setNavigationOnClickListener {
            onBackPressed()
            finish()
        }
    }

    companion object {
        const val storage_permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
        const val PICK_IMAGE = 101
        const val RQ_SPEECH_REC=102
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

}