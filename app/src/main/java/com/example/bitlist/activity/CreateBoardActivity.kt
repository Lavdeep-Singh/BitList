package com.example.bitlist.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.bitlist.Constants
import com.example.bitlist.Firestore.FireStoreClass
import com.example.bitlist.R
import com.example.bitlist.models.Board
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create_board.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CreateBoardActivity : BaseActivity() {
    //private lateinit var fileUri: Uri
    //private var saveImageToInternalStorageUri:Uri?=null
    //var for date picker dialog
    private var priority: Int = 1
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var cal = Calendar.getInstance() //instance of calendar class
    private lateinit var startDate: String //local variable to store start date
    private lateinit var endDate: String  //local variable to store end date
    private lateinit var dateSetListener2: DatePickerDialog.OnDateSetListener
    private var cal2 = Calendar.getInstance() //instance of calendar class
    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserName: String
    private var mBoardImageUrl: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)
        setUpActionBar()

        //get user name of user, will be used when creating model of Board and created by is the user name
        if (intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }

        //click listener on board image
        iv_board_image.setOnClickListener {
            //if permission to write external storage is granted
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //open image chooser
                Constants.showImageChooser(this)
            } else { //ask for permissions
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }

        }


        //open datePickerDialog and wait for someone to select date
        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        //open datePickerDialog and wait for someone to select date
        dateSetListener2 = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal2.set(Calendar.YEAR, year)
            cal2.set(Calendar.MONTH, month)
            cal2.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView2()
        }


        //click listener on start date
        tv_board_start_date.setOnClickListener {
            DatePickerDialog(
                this@CreateBoardActivity,
                dateSetListener, cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        //click listener on end date
        tv_board_end_date.setOnClickListener {
            DatePickerDialog(
                this@CreateBoardActivity,
                dateSetListener2, cal2.get(Calendar.YEAR),
                cal2.get(Calendar.MONTH),
                cal2.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        //selecting priority
        sp_priority.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (parent != null) {
                    priority = position
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //TODO("Not yet implemented")
            }

        }

        btn_create.setOnClickListener {
            if (mSelectedImageFileUri != null) { //user have selected an image
                uploadBoardImage()
            } else { //not selected any image
                progressDialog.show(
                    this,
                    resources.getString(R.string.please_wait)
                ) //show progress dialog
                createBoard() //call this function
            }
        }


    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_create_board_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back)
            actionBar.title = resources.getString(R.string.board_name)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolbar_create_board_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    companion object{
        private const val IMAGE_DIRECTORY="BitListImages"
    }
 /*   //saving image into device
    private fun saveImageToInternalStorage(bitmap: Bitmap):Uri{
        val wrapper= ContextWrapper(applicationContext) //get context
        //create a file,wrapper is used to get directory of our application
        //getDir= used to retrieve or create file in which application can place it's own data files
        //IMAGE_DIRECTORY is the name of directory we are creating
        var file=wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        //create a File using directory(file's details) and give it random name
        file= File(file,"${UUID.randomUUID()}.jpeg")

        //storing file in device
        try{
            val stream: OutputStream =
                FileOutputStream(file)//save a file with above provided details using file o/p stream
            bitmap.compress(Bitmap.CompressFormat.JPEG,40,stream) //compress file into JPEG format
            stream.flush()
            stream.close()
        }catch(e:IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath) //parse file path/details into Uri format and return it
    }

  */

    //will be called when user has returned after selecting an image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //user has selected an image and image is not null
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            mSelectedImageFileUri = data.data
         /*   try{    progressDialog.show(this)
                    //get the bitmap from the gallery where the name/location of data is specified by mSelectedImageFileUri
                val source =
                    mSelectedImageFileUri?.let { ImageDecoder.createSource(contentResolver, it) }
                val bitmap: Bitmap? = source?.let { ImageDecoder.decodeBitmap(it) }
                   // val selectedImageBitmap=
                     //   MediaStore.Images.Media.getBitmap(this.contentResolver,mSelectedImageFileUri)   //convert selected file uri into bitmap
                    saveImageToInternalStorageUri=
                        bitmap?.let { saveImageToInternalStorage(it) }//send bitmap to compress it and save into device and get saved image uri
                progressDialog.dialog.dismiss()

            }catch(e:IOException){
                e.printStackTrace()
                progressDialog.dialog.dismiss()
                Toast.makeText(this, "something wrong in on Activity result", Toast.LENGTH_SHORT).show()
            }

          */

            //update the image
            try {
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.logotwo)
                    .into(iv_board_image);
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    //set start date from calendar
    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        startDate = sdf.format(cal.time).toString()
        tv_board_start_date.setText(startDate)
    }

    //set end date from calender
    private fun updateDateInView2() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        endDate = sdf.format(cal2.time).toString()
        tv_board_end_date.setText(endDate)
    }

    //asking user to access external storage
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this) //open image chooser
            } else {//permission not granted
                showErrorSnackBar("You denied permissions, they can be allowed from the settings")
            }
        }
    }

    //upload board image to database
    private fun uploadBoardImage() {

        progressDialog.show(this, resources.getString(R.string.please_wait)) //show progress dialog
        if (mSelectedImageFileUri != null) { //if user have selected an image
                val sRef: StorageReference =
                    FirebaseStorage.getInstance().reference
                        .child(
                            "BOARD_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                                this@CreateBoardActivity,
                                mSelectedImageFileUri
                            )
                        ) //name of the file uploaded in the firebase

                sRef.putFile(mSelectedImageFileUri!!)
                    .addOnSuccessListener {   //upload file in the firebase and on Success
                            taskSnapshot ->
                        Log.i(
                            "board Image url",
                            taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                        )
                        taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { //get downloadable link of image uploaded to storage
                                uri ->
                            mBoardImageUrl =
                                uri.toString()  //storing download link of image in local variable
                            createBoard() //call this function
                        }
                    }.addOnFailureListener { //not able to upload file
                            exception ->
                        Toast.makeText(
                            this@CreateBoardActivity,
                            exception.message,
                            Toast.LENGTH_LONG
                        ).show()
                        progressDialog.dialog.dismiss() //hide progress dialog
                    }



        }
    }

    //fun to create a board instance
    private fun createBoard() {
        val assignedUserArrayList: ArrayList<String> =
            ArrayList()  //arraylist of members assigned to the board
        assignedUserArrayList.add(getCurrentUserId())
        var board = Board(
            et_board_name.text.toString(),
            mBoardImageUrl,
            mUserName,
            assignedUserArrayList,
            "",
            startDate,
            endDate,
            priority
        )
        FireStoreClass().createBoard(this, board)
    }

    fun boardCreatedSuccessfully() {
        progressDialog.dialog.dismiss() //hide progress dialog
        setResult(Activity.RESULT_OK) //center activity will know that user have made some changes
        finish()
    }

}