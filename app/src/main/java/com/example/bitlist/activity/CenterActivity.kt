package com.example.bitlist.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bitlist.Constants
import com.example.bitlist.Firestore.FireStoreClass
import com.example.bitlist.R
import com.example.bitlist.adapters.BoardAdapter
import com.example.bitlist.models.Board
import com.example.bitlist.models.User
import com.example.bitlist.utils.SwipeToDeleteCallback
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_center.*
import kotlinx.android.synthetic.main.activity_task_list.*
import kotlinx.android.synthetic.main.appbar_main.*
import kotlinx.android.synthetic.main.center_activity_content.*
import kotlinx.android.synthetic.main.nav_header_layout.*
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

//Extend NavigationView.onNavigationItemSelectedListener to set on click listener on items
class CenterActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener{
    companion object{
        const val MY_PROFILE_REQUEST_CODE:Int=11
        const val CREATE_BOARD_REQUEST_CODE=12
    }
    //compare priority of two boards
    var boardComparator = Comparator<Board>{board1, board2 ->

        if (board1.priority.compareTo(board2.priority) == 0) {
            // sort according to size of assigned to if priority is same
            board1.assignedTo.size.compareTo(board2.assignedTo.size)
        } else {//sort according to priority
            board1.priority.compareTo(board2.priority)
        }
    }
    private lateinit var mBoardList:ArrayList<Board>
    private lateinit var mUserName:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_center)
        setUpActionBar()
        //item selected listener for navigation items
        nav_view.setNavigationItemSelectedListener(this)

        //check if internet is available
        if(Constants.isNetworkAvailable(this)){
            FireStoreClass().loadUserData(this,true)  //call this function
        }else{
            showErrorSnackBar("No internet connection, please connect to internet and try again")
        }

        //on click listener on fab button to create a new board
        fab_create_board.setOnClickListener {
            val intent= Intent(this,CreateBoardActivity::class.java)
            //mUserName="Lav Singh"
            intent.putExtra(Constants.NAME,mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }

        srl_center_activity.setOnRefreshListener {
            FireStoreClass().loadUserData(this,true)  //call this function
            srl_center_activity.isRefreshing=false
        }
    }

    private fun setUpActionBar(){
        setSupportActionBar(toolbar_center_activity)
        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.hamburger)
            tv_center_activity_toolbar_title.text=resources.getString(R.string.home)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolbar_center_activity.setNavigationOnClickListener {
            toggleDrawer() //click listener on hamburger icon
        }
    }

    private fun toggleDrawer(){
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){ //if drawer open
            drawer_layout.closeDrawer(GravityCompat.START)   //then close drawer to gravity start(left side)
        }else{
            drawer_layout.openDrawer(GravityCompat.START)    //else open drawer from gravity start(left side)
        }
    }

    override fun onBackPressed() {
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){ //if drawer is open then close it on back pressed
            drawer_layout.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()//else implement double back to exit
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //if result is ok the user have made changes in the profile so load user data again to update navigation view
        if(resultCode== Activity.RESULT_OK && requestCode== MY_PROFILE_REQUEST_CODE){
            FireStoreClass().loadUserData(this)

        }
           //if result is okay and user have made a new board then get  board list
           else if(resultCode== Activity.RESULT_OK && requestCode== CREATE_BOARD_REQUEST_CODE){
            FireStoreClass().getBoardsList(this)  //get list of boards which are assigned to current user

        }
        else{
            Log.e("Cancelled","cancelled")
        }
    }
    //inflate menu items on center activity toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.center_activity_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.sort_boards->{
                Collections.sort(mBoardList,boardComparator)
                //progressDialog.show(this)
                populateBoardsListToUi(mBoardList)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean { //click listener on NavigationView items
        when(item.itemId){
            //start activity profile for result , if result is ok then user have made some changes
            //so we have to update user data in navigation view
            R.id.nav_my_profile->{
                startActivityForResult(Intent(this,ProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out->{
                //alert dialog for user to warn of sign out
                val builder = AlertDialog.Builder(this@CenterActivity)
                //set title for alert dialog
                builder.setTitle(R.string.signOutDialogTitle)
                //set message for alert dialog
                builder.setMessage(R.string.signOutDialogMsg)
                builder.setIcon(android.R.drawable.ic_dialog_alert)

                //performing positive action
                builder.setPositiveButton("Yes"){dialogInterface, which ->
                    FirebaseAuth.getInstance().signOut() //sign out user
                    val intent= Intent(this,SignInActivity::class.java)
                    //IF Sign in ACTIVITY IS ALREADY IN ACTIVITY STACK THEN CLEAR ALL OTHER ACTIVITIES ON TOP OF IT AND BRING INTRO ACTIVITY FORWARD
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()

                }
                //performing cancel action
                builder.setNeutralButton("Cancel"){dialogInterface , which ->
                    dialogInterface.dismiss()

                }
                //performing negative action
                builder.setNegativeButton("No"){dialogInterface, which ->
                    dialogInterface.dismiss()
                }
                // Create the AlertDialog
                val alertDialog: AlertDialog = builder.create()
                // Set other dialog properties
                alertDialog.setCancelable(false)
                alertDialog.show()


            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)//close drawer after operations end
        return true
    }

    fun updateNavigationUserDetails(loggedInUser: User,readDataFromDatabase:Boolean){
      //  progressDialog.dialog.dismiss()
        mUserName=loggedInUser.name//store user name in local variable
        Glide
            .with(this)
            .load(loggedInUser.image)
            .centerCrop()
            .placeholder(R.drawable.logo)
            .into(iv_nav_user_image)
        tv_nav_username.text=loggedInUser.name
        if(readDataFromDatabase){    //if true then only update board list
            //progressDialog.show(this,getString(R.string.please_wait)) //show progress dialog
            FireStoreClass().getBoardsList(this)
        }
    }

    //handle adapter and populate it
    fun populateBoardsListToUi(boardList:ArrayList<Board>){
        //progressDialog.dialog.dismiss() //hide progress dialog
        mBoardList=boardList    //get board list in global variable
        if(mBoardList.size>0){
            rv_boards_list.visibility= View.VISIBLE
            tv_no_boards_available.visibility= View.GONE
            rv_boards_list.layoutManager= LinearLayoutManager(this)
            rv_boards_list.setHasFixedSize(true)
            val adapter= BoardAdapter(this,mBoardList)
            rv_boards_list.adapter=adapter

            //click listener on adapter rows
            adapter.setOnClickListener(object :BoardAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent=Intent(this@CenterActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentId)
                    startActivity(intent)
                }
            })
            //swipe to delete functionality
            val deleteSwipeHandler=object: SwipeToDeleteCallback(this){
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                    val builder = AlertDialog.Builder(this@CenterActivity)
                    //set title for alert dialog
                    builder.setTitle(R.string.dialogTitle)
                    //set message for alert dialog
                    builder.setMessage(R.string.dialogMessage)
                    builder.setIcon(android.R.drawable.ic_dialog_alert)

                    //performing positive action
                    builder.setPositiveButton("Yes"){dialogInterface, which ->
                        FireStoreClass().deleteBoard(this@CenterActivity,mBoardList[viewHolder.adapterPosition]) //delete board from board list
                        FireStoreClass().loadUserData(this@CenterActivity,true)  //reload whole data

                    }
                    //performing cancel action
                    builder.setNeutralButton("Cancel"){dialogInterface , which ->
                        FireStoreClass().loadUserData(this@CenterActivity,true)  //reload whole data
                        dialogInterface.dismiss()

                    }
                    //performing negative action
                    builder.setNegativeButton("No"){dialogInterface, which ->
                        FireStoreClass().loadUserData(this@CenterActivity,true)  //reload whole data
                        dialogInterface.dismiss()
                    }
                    // Create the AlertDialog
                    val alertDialog: AlertDialog = builder.create()
                    // Set other dialog properties
                    alertDialog.setCancelable(false)
                    alertDialog.show()

                }
            }
            val deleteItemTouchHelper= ItemTouchHelper(deleteSwipeHandler)
            deleteItemTouchHelper.attachToRecyclerView(rv_boards_list)
        }else{
            rv_boards_list.visibility= View.GONE
            tv_no_boards_available.visibility= View.VISIBLE
            //progressDialog.dialog.dismiss()
        }


    }

    override fun onResume() {
        super.onResume()
        //check if internet is available
        if(Constants.isNetworkAvailable(this)){
            FireStoreClass().loadUserData(this,true)  //call this function
        }else{
            showErrorSnackBar("No internet connection, please connect to internet and try again")
        }

    }
}