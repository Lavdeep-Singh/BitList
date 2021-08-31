package com.example.bitlist.activity

import android.os.Bundle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bitlist.Constants
import com.example.bitlist.Firestore.FireStoreClass
import com.example.bitlist.R
import com.example.bitlist.adapters.MembersAdapter
import com.example.bitlist.models.Board
import com.example.bitlist.models.User
import com.example.bitlist.utils.SwipeToDeleteCallback
import kotlinx.android.synthetic.main.activity_add_delete_members_from_board.*
import kotlinx.android.synthetic.main.activity_task_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

//todo add delete member funtion
class AddDeleteMembersFromBoard : BaseActivity() {
    private lateinit var mDocumentId:String
    private lateinit var mBoardDetails:Board
    lateinit var mMembers:ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_delete_members_from_board)
        setUpActionBar()

        //get board details from intent
        if(intent.hasExtra(Constants.DOC)){
            mDocumentId= intent.getStringExtra(Constants.DOC).toString()
        }

        progressDialog.show(this)
        FireStoreClass().getBoardDetails(this,mDocumentId)//get board details from fire base

        btn_add_member.setOnClickListener {
            if(et_member_email.text!!.isNotEmpty()){
                //pass email to get user details
                FireStoreClass().getMemberDetails(this,et_member_email.text.toString())
            }else{
                showErrorSnackBar("Please enter something")
            }
        }

    }

    //getting member details from the passed email
    fun memberDetails(user:User) {
        val message = "added you to the board"
        mMembers.add(user)
        setUpAdapter(mMembers)
        mBoardDetails.assignedTo.add(user.id)
        FireStoreClass().assignMemberToBoard(this, mBoardDetails)

    }


    private fun setUpActionBar() {
        setSupportActionBar(toolbar_add_delete_member)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolbar_add_delete_member.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    fun getBoardDetails(board: Board){
        progressDialog.dialog.dismiss()
        mBoardDetails=board
        //get details of only those users who are in assigned to array of Board
        FireStoreClass().getAssignedMembersListDetails(this,board.assignedTo)
    }

    fun setUpAdapter(members:ArrayList<User>) {
        rv_add_delete_member.layoutManager = LinearLayoutManager(this)
        rv_add_delete_member.setHasFixedSize(true)
        val adapter = MembersAdapter(this, members)
        rv_add_delete_member.adapter = adapter
        //swipe to delete functionality
        //todo remove from recycler view and then remove member if from firebase from assigned to
        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val removedId:String=members[viewHolder.adapterPosition].id
                members.removeAt(viewHolder.adapterPosition)
                mBoardDetails.assignedTo.remove(removedId)
                adapter.notifyItemRemoved(viewHolder.adapterPosition) //notify item about removing of item
                FireStoreClass().assignMemberToBoard(this@AddDeleteMembersFromBoard,mBoardDetails)

            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rv_add_delete_member)

    }
}