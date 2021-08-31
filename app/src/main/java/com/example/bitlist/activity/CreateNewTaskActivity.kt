package com.example.bitlist.activity

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bitlist.Constants
import com.example.bitlist.Firestore.FireStoreClass
import com.example.bitlist.R
import com.example.bitlist.models.Board
import com.example.bitlist.models.Task
import kotlinx.android.synthetic.main.activity_create_new_task.*
import kotlinx.android.synthetic.main.activity_task_list.*
import kotlinx.android.synthetic.main.activity_task_list.toolbar_task_list

class CreateNewTaskActivity : BaseActivity() {
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

}