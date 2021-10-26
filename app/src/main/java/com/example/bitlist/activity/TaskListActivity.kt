package com.example.bitlist.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bitlist.Constants
import com.example.bitlist.Firestore.FireStoreClass
import com.example.bitlist.R
import com.example.bitlist.adapters.TaskListAdapter
import com.example.bitlist.models.Board
import com.example.bitlist.models.Task
import com.example.bitlist.utils.SwipeToDeleteCallback
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_task_list.*
import kotlinx.android.synthetic.main.center_activity_content.*

class TaskListActivity : BaseActivity() {
    lateinit var mBoardDetails: Board
    lateinit var mBoardDocumentId:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)
        setUpActionBar()
        //get document id
        if(intent.hasExtra(Constants.DOCUMENT_ID)){
            mBoardDocumentId= intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }
        progressDialog.show(this)
        FireStoreClass().getBoardDetails(this,mBoardDocumentId)//get board details from fire base

        fab_add_task.setOnClickListener {
            val intent=Intent(this,CreateNewTaskActivity::class.java)
            intent.putExtra(Constants.BOARD_DETAILS,mBoardDetails)
            startActivityForResult(intent,
                CREATE_NEW_TASK_RESULT)
        }
        //swipe down to refresh on click listener
        srl_taskList.setOnRefreshListener {
            //every time user swipe down , load board details
            progressDialog.show(this)
            FireStoreClass().getBoardDetails(this,mBoardDocumentId)
            srl_taskList.isRefreshing=false
        }
    }
    companion object{
       private const val CREATE_NEW_TASK_RESULT=1
       private const val UPDATE_TASK_RESULT=2
       private const val ADD_MEMBER_RESULT=2
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_task_list)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back)
            actionBar.title = resources.getString(R.string.board_name)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolbar_task_list.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    //inflate menu on toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.task_list_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
    //click listener on add member icon of toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.add_member->{
                val intent=Intent(this@TaskListActivity,AddDeleteMembersFromBoard::class.java)
                intent.putExtra(Constants.DOC,mBoardDocumentId)
                startActivityForResult(intent, ADD_MEMBER_RESULT)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //set up adapter of task list
    private fun setUpAdapterTaskDetails(board: Board){
        if(board.taskList.size>0){
            tv_nothing_here.visibility=View.GONE
            rv_task_list.visibility=View.VISIBLE
            rv_task_list.layoutManager= StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
            rv_task_list.setHasFixedSize(true)
            val taskList=board.taskList
            val adapter= TaskListAdapter(this,taskList)
            rv_task_list.adapter=adapter
            //swipe to delete functionality
            val deleteSwipeHandler=object: SwipeToDeleteCallback(this){
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val tempTask:Task=mBoardDetails.taskList[viewHolder.adapterPosition]
                   // progressDialog.show(this@TaskListActivity)
                    mBoardDetails.taskList.removeAt(viewHolder.adapterPosition)  //remove task from position given by swipe to delete class
                    adapter.notifyItemRemoved(viewHolder.adapterPosition) //notify item about removing of item
                    FireStoreClass().addUpdateTaskList(this@TaskListActivity,mBoardDetails)  //update fire store class
                   // progressDialog.dialog.dismiss()
                    val actionSnackBar:Snackbar= Snackbar.make(rl_taskList,"Task has been deleted",Snackbar.LENGTH_LONG).setAction(
                        "UNDO",object :View.OnClickListener{
                            override fun onClick(v: View?) {
                                mBoardDetails.taskList.add(tempTask)
                                //adapter.notifyItemInserted(viewHolder.adapterPosition)
                                FireStoreClass().addUpdateTaskList(this@TaskListActivity,mBoardDetails)
                                progressDialog.show(this@TaskListActivity)
                                FireStoreClass().getBoardDetails(this@TaskListActivity,mBoardDocumentId)//get board details from fire base
                            }

                        }
                    )
                    actionSnackBar.show()

                }
            }
            val deleteItemTouchHelper= ItemTouchHelper(deleteSwipeHandler)
            deleteItemTouchHelper.attachToRecyclerView(rv_task_list)

            //on click listener on individual tasks
            adapter.setOnClickListener(object :TaskListAdapter.OnClickListener{
                override fun onClick(position: Int, model: Task) {
                    val intent=Intent(this@TaskListActivity,CreateNewTaskActivity::class.java)
                    intent.putExtra(Constants.POSITION,position)
                    intent.putExtra(Constants.BOARD_DETAILS,board)
                    intent.putExtra(Constants.TASK_DETAILS,model)
                    startActivityForResult(intent,UPDATE_TASK_RESULT)
                }
            })
        }

    }
  //  fun reloadBoardAfterUndo(board: Board){
  //      setUpAdapterTaskDetails(board)
  //  }


    fun boardDetails(board: Board){
        mBoardDetails=board
        progressDialog.dialog.dismiss()
        setUpAdapterTaskDetails(board)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode== Activity.RESULT_OK && requestCode== CREATE_NEW_TASK_RESULT){
            progressDialog.show(this)
            FireStoreClass().getBoardDetails(this,mBoardDocumentId)
        }else if(resultCode== Activity.RESULT_OK && requestCode== UPDATE_TASK_RESULT){
            progressDialog.show(this)
            FireStoreClass().getBoardDetails(this,mBoardDocumentId)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
    

}