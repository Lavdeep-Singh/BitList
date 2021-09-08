package com.example.bitlist.activity

import android.app.Activity
import android.app.Dialog
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
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.bitlist.Constants
import com.example.bitlist.Firestore.FireStoreClass
import com.example.bitlist.R
import com.example.bitlist.adapters.TaskImagesAdapter
import com.example.bitlist.adapters.TaskListAdapter
import com.example.bitlist.models.Board
import com.example.bitlist.models.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_create_new_task.*
import kotlinx.android.synthetic.main.activity_task_list.*
import kotlinx.android.synthetic.main.activity_task_list.toolbar_task_list
import kotlinx.android.synthetic.main.image_dialog_for_task_layout.*
import kotlinx.android.synthetic.main.task_images_single_column_for_rv.view.*
import java.io.FileDescriptor
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class CreateNewTaskActivity : BaseActivity() {
    private var bitmap: Bitmap? = null
    private var taskImageUri:Uri?=null
    private var mTaskImageUrl: String = ""
    private var mTaskImages:ArrayList<String> = ArrayList()

    private lateinit var mBoardDetails: Board
    private lateinit var mTaskDetails: Task
    private var position: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_task)
        setUpActionBar()

        //get the current board details
        if (intent.hasExtra(Constants.BOARD_DETAILS)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAILS)!!
        }
        if (intent.hasExtra(Constants.BOARD_DETAILS) && intent.hasExtra(Constants.POSITION) && intent.hasExtra(
                Constants.TASK_DETAILS
            )
        ) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAILS)!!
            mTaskDetails = intent.getParcelableExtra<Task>(Constants.TASK_DETAILS)!!
            position = intent.getIntExtra(Constants.POSITION, -1)

            btn_create_new_task_list.text = resources.getString(R.string.update)
            et_new_task_list_title.setText(mTaskDetails.title)
            et_new_task_list_description.setText(mTaskDetails.description)
            mTaskImages=mTaskDetails.taskImages
            if(mTaskImages.size>0){
               for(i in mTaskImages){
                   Log.d("Images are",i)
               }

                setUpRecyclerView(mTaskImages)
            }

        }

        btn_create_new_task_list.setOnClickListener {
            if (position == -1) { //activity is opened to create new task
                //when create button is clicked, add new task in the mBoardDetails and send to fire store to update fire store
                if (et_new_task_list_description.text.isNotEmpty() && et_new_task_list_title.text.isNotEmpty()) {
                    if(taskImageUri!=null){ //user have selected an image
                        uploadImageToFirebase(false)
                    }else{ //user have not selected any image
                        createTask(false)
                    }
                } else {
                    showErrorSnackBar("Please enter something")
                }
            }
            //means activity is opened to update the task
            if (position != -1) {
                if (et_new_task_list_description.text.isNotEmpty() && et_new_task_list_title.text.isNotEmpty()) {
                    if(taskImageUri!=null){ //user have selected an image
                        uploadImageToFirebase(true)
                    }else{ //user have not selected any image
                        createTask(true)
                    }
                } else {
                    showErrorSnackBar("Please enter something")
                }
            }

        }
        //for image to text
        iv_image_search.setOnClickListener {
            if (!checkStoragePermission()) {
                requestPermissions(listOf(storage_permission).toTypedArray(), 100)
            } else {
                pickImage(PICK_IMAGE)
            }
        }
        iv_mic.setOnClickListener {
            askSpeechInput()
        }

        iv_add_image.setOnClickListener{
            if(!checkStoragePermission()){
                requestPermissions(listOf(storage_permission).toTypedArray(), 103)
            }else{
                pickImage(PICK_IMAGE_FOR_TASK)
            }
        }

    }
    private fun setUpRecyclerView(taskImages:ArrayList<String>){
        rv_task_images.layoutManager= LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        //rv_task_images.setHasFixedSize(true)
        //val taskImages=mTaskDetails.taskImages
        val adapter= TaskImagesAdapter(this,taskImages)
        rv_task_images.adapter=adapter
        rv_task_images.visibility=View.VISIBLE
        adapter.setOnClickListener(object:TaskImagesAdapter.OnClickListener{
            override fun onClick(position: Int, image: String) {
                if(image.isNotEmpty()){
                    showDialog(image)

                }

            }

        })
    }
    //dialog box to show images
    private fun showDialog(image:String) {
        val dialog= Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.image_dialog_for_task_layout)
        dialog.setCancelable(true)
        val lp=WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width= WindowManager.LayoutParams.WRAP_CONTENT
        lp.height= WindowManager.LayoutParams.WRAP_CONTENT
        try {
            Glide
                .with(this@CreateNewTaskActivity)
                .load(image)
                .fitCenter()
                .placeholder(R.drawable.logo)
                .into(dialog.findViewById<View>(R.id.iv_dialog_task) as ImageView);
        } catch (e: IOException) {
            e.printStackTrace()
        }
        dialog.show()
        dialog.window!!.attributes=lp
    }
    private fun uploadImageToFirebase(flag:Boolean=false) {
        progressDialog.show(this) //show progress dialog
        if (taskImageUri != null) { //if user have selected an image
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference
                    .child(
                        "TASK_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                            this@CreateNewTaskActivity,
                            taskImageUri
                        )
                    ) //name of the file uploaded in the firebase

            sRef.putFile(taskImageUri!!)
                .addOnSuccessListener {   //upload file in the firebase and on Success
                        taskSnapshot ->
                    Log.i(
                        "task Image url",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { //get downloadable link of image uploaded to storage
                            uri ->
                        mTaskImageUrl =
                            uri.toString()  //storing download link of image in local variable
                        if(flag){
                            createTask(true) //call this function, update the task
                        }else{
                            createTask(false) //call this function,create new task
                        }
                        progressDialog.dialog.dismiss()
                    }
                }.addOnFailureListener { //not able to upload file
                        exception ->
                    Toast.makeText(
                        this@CreateNewTaskActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()
                    progressDialog.dialog.dismiss() //hide progress dialog
                }



        }
    }

    private fun createTask(flag:Boolean=false) {
        //flag is true if activity is opened to update task
        if(mTaskImageUrl.isNotEmpty()){
            mTaskImages.add(mTaskImageUrl)
            for(i in mTaskImages){
                Log.d("Images after addition is ",i)
            }

        }
        val task = Task(
            et_new_task_list_title.text.toString(),
            FireStoreClass().getCurrentUserId(),
            et_new_task_list_description.text.toString(),
            mTaskImages
        )
        //update task
        if(flag){
            mBoardDetails.taskList[position]=task
            progressDialog.show(this)
            FireStoreClass().addUpdateTaskList(this,mBoardDetails)
        }else{ //create new task
            mBoardDetails.taskList.add(task)
            progressDialog.show(this)
            FireStoreClass().addUpdateTaskList(this, mBoardDetails)
        }

    }

    //text to speech implementation
    private fun askSpeechInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "speech recognition not available", Toast.LENGTH_SHORT).show()
        } else {
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something!!!")
            startActivityForResult(i, RQ_SPEECH_REC)
        }
    }

    //check storage permission
    private fun checkStoragePermission(): Boolean {
        return checkSelfPermission(storage_permission) == PackageManager.PERMISSION_GRANTED
    }

    //fun to open gallery and pick image
    private fun pickImage(CODE:Int) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), CODE)
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
                            val recognizer =
                                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                            bitmap?.let {
                                val image = InputImage.fromBitmap(it, 0)
                                recognizer.process(image)
                                    .addOnSuccessListener { visionText ->
                                        // textBlocks -> will return list of block of detected text
                                        // lines -> will return list of detected lines
                                        // elements -> will return list of detected words
                                        // boundingBox -> will return rectangle box area in bitmap
                                        if (et_new_task_list_description.text.isNotEmpty()) {
                                            et_new_task_list_description.append("\n" + visionText.text)
                                        } else {
                                            et_new_task_list_description.append(visionText.text)
                                        }
                                        //Toast.makeText(this, visionText.text, Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this,
                                            "Error: " + e.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            if (bitmap == null) Toast.makeText(
                                this,
                                "Please select image!",
                                Toast.LENGTH_SHORT
                            )
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
            RQ_SPEECH_REC -> {
                when (resultCode) {
                    RESULT_OK -> {
                        val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        if (et_new_task_list_description.text.isNotEmpty()) {
                            et_new_task_list_description.append("\n" + result?.get(0).toString())
                        } else {
                            et_new_task_list_description.append(result?.get(0).toString())
                        }

                    }
                }
            }
            PICK_IMAGE_FOR_TASK->{
                when(resultCode){
                    RESULT_OK->{
                        if(data!!.data != null){
                             taskImageUri=data.data //uri of selected image
                            //set image in task
                            //mTaskDetails.taskImages.add(taskImageUri.toString())
                            val taskImages:ArrayList<String> = mTaskImages.clone() as ArrayList<String>
                            taskImages.add(taskImageUri.toString())
                            setUpRecyclerView(taskImages)
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
                    pickImage(PICK_IMAGE)
                } else {
                    Toast.makeText(this, "Please grant storage permission!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            103->{
                if (isAllPermissionGranted(permissions)) {
                    pickImage(PICK_IMAGE_FOR_TASK)
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

    fun addUpdateTaskListSuccess() {
        setResult(Activity.RESULT_OK)//task list activity will know that user have created a new task or updated the task
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
        const val RQ_SPEECH_REC = 102
        const val PICK_IMAGE_FOR_TASK=103
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