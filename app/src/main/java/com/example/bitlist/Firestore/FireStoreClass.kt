package com.example.bitlist.Firestore

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.bitlist.Constants
import com.example.bitlist.activity.*
import com.example.bitlist.fragments.SignUpTabFragment
import com.example.bitlist.models.Board
import com.example.bitlist.models.Task
import com.example.bitlist.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FireStoreClass {
    private val mFirestore= FirebaseFirestore.getInstance() //instance of firestore
    //function to get uuid of current logged in user
    fun getCurrentUserId():String{
        var currentUser= FirebaseAuth.getInstance().currentUser
        var currentUserId=""
        if(currentUser!=null){
            currentUserId=currentUser.uid
        }
        return currentUserId
    }

    fun loadUserData(activity: Activity,readDataFromDatabase:Boolean=false){ //function to get user data in form of User data class
        mFirestore.collection(Constants.USER)  //accessing collection by passing collections name
            .document(getCurrentUserId())  //inside collections access a document named uuid of user
            .get()
            .addOnSuccessListener { document->  //on success
                val loggedInUser= document.toObject(User::class.java) //get all data of document in form of User model object
                when(activity){
                    is ProfileActivity->{   //if calling activity is profile activity
                            if (loggedInUser != null) {
                                activity.setContentOnProfile(loggedInUser)
                        }
                    }
                    is CenterActivity->{     //if calling activity is center activity, we don't want lo load data everytime center activity is initialised so using boolean
                        if(loggedInUser!=null){
                            activity.updateNavigationUserDetails(loggedInUser,readDataFromDatabase)
                        }
                    }
                }

            }
            .addOnFailureListener{
                    e->
                Log.e(activity.javaClass.simpleName,"Error retrieving user data from firestore")
                (activity as SignInActivity).progressDialog.dialog.dismiss() //hide progress dialog
            }
    }
    //hashmap is passed to update user details in database
    fun updateUserProfileData(activity: Activity,userHashMap:HashMap<String,Any>){
        mFirestore.collection(Constants.USER)
            .document(getCurrentUserId())
            .update(userHashMap)            //update user details with the passed hash map
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Profile data updated successfully")
                Toast.makeText(activity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                when(activity){
                    is ProfileActivity->{
                        activity.profileUpdateSuccess()  //call this function to
                    }
                }
            }.addOnFailureListener {
                    e->
                when(activity){
                    is ProfileActivity->{
                        activity.progressDialog.dialog.dismiss() //hide progress dialog
                    }
                }
                Log.i(activity.javaClass.simpleName,"Error updating profile")
                BaseActivity().showErrorSnackBar("Error while updating profile")

            }
    }
    //function to create board collection
    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFirestore.collection(Constants.BOARD)       //create collection named boards or access it if already exist
            .document()                               //create a document in the collection
            .set(board, SetOptions.merge())           //merge data if already exist
            .addOnSuccessListener {
                Toast.makeText(activity, "Board created successfully", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()   //call this function when board created successfully
            }.addOnFailureListener {
                    exception->
                activity.progressDialog.dialog.dismiss()
                Log.e(activity.javaClass.simpleName,"Error creating board",exception)
            }
    }

    //fun to get boards whose assigned to array has current user id
    fun getBoardsList(activity: CenterActivity){
        mFirestore.collection(Constants.BOARD) //go to the boards collection
            .whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserId()) //get the array whose assigned to variable contains current user id
            .get().addOnSuccessListener{
                    document->
                Log.e(activity.javaClass.simpleName,document.documents.toString())
                val boardList:ArrayList<Board> = ArrayList()
                for(i in document.documents){               //iterate in the document(document is arraylist of Board type)
                    var board=i.toObject(Board::class.java)  //convert each element to Board type
                    if (board != null) {
                        board.documentId=i.id                //get document id
                        boardList.add(board)                 //add to the arraylist of board
                    }
                    activity.populateBoardsListToUi(boardList) //call this function to populate

                }
            }.addOnFailureListener {
                    e->
               // activity.progressDialog.dialog.dismiss()//hide progress dialog
                Log.e(activity.javaClass.simpleName,"Error while creating a board")
            }
    }
    fun getBoardDetails(activity: Activity,documentId:String){
        mFirestore.collection(Constants.BOARD) //go to the boards collection
            .document(documentId)    //find in documents the passed id
            .get().addOnSuccessListener{
                    document->
                Log.e(activity.javaClass.simpleName,document.toString())
                val board= document.toObject(Board::class.java)!!
                board.documentId=document.id
                if(activity is TaskListActivity){
                    activity.boardDetails(board)
                }else if(activity is AddDeleteMembersFromBoard){
                    activity.getBoardDetails(board)
                }


            }.addOnFailureListener {
                    e->
                if(activity is TaskListActivity){
                    activity.progressDialog.dialog.dismiss()
                }
                else if(activity is AddDeleteMembersFromBoard){
                    activity.progressDialog.dialog.dismiss()
                }

                Log.e(activity.javaClass.simpleName,"Error while getting task list details")
            }
    }

    fun addUpdateTaskList(activity:Activity,board: Board){
        val taskListHashMap=HashMap<String,Any>()
        taskListHashMap[Constants.TASK_LIST]=board.taskList
        mFirestore.collection(Constants.BOARD)
            .document(board.documentId)//get the document with passed id
            .update(taskListHashMap)  //update fires store with hashmap
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Task List Updated")
                if(activity is CreateNewTaskActivity){
                    activity.addUpdateTaskListSuccess()
                }//else if(activity is TaskListActivity){
                 //   activity.reloadBoardAfterUndo(board)
               // }

            }.addOnFailureListener {
                    exception->
                if(activity is CreateNewTaskActivity) {
                    activity.progressDialog.dialog.dismiss()
                }
                if(activity is TaskListActivity) {
                activity.progressDialog.dialog.dismiss()
            }
                Log.i(activity.javaClass.simpleName,"error while creating the board")
            }
    }

    //find all  who are assigned a task/board by id and get their details
    fun getAssignedMembersListDetails(activity: Activity,assignedTo:ArrayList<String>){
        mFirestore.collection(Constants.USER) //in collection of name users
            .whereIn(Constants.ID,assignedTo)  //find users where id is present in assignedTo list
            .get()                             //get all users
            .addOnSuccessListener {
                    document->
                Log.e(activity.javaClass.simpleName,document.documents.toString())
                val userList:ArrayList<User> = ArrayList()

                for(i in document.documents){   //convert them into object and store user data in a list
                    val user= i.toObject(User::class.java)!!
                    userList.add(user)
                }
                if(activity is AddDeleteMembersFromBoard){
                    activity.setUpAdapter(userList)
                    activity.mMembers=userList
                }
            }.addOnFailureListener {
                    e->
                if(activity is AddDeleteMembersFromBoard){
                    activity.progressDialog.dialog.dismiss()
                }
                Log.e(activity.javaClass.simpleName,"Error while getting users details",e)

            }
    }

    //get details about the user from email
    fun getMemberDetails(activity: AddDeleteMembersFromBoard,email:String){
        mFirestore.collection(Constants.USER)
            .whereEqualTo(Constants.EMAIL,email) //check through all the users and find if passed email exists in the users
            .get()
            .addOnSuccessListener {
                    document->
                if(document.documents.size>0){
                    val user = document.documents[0].toObject(User::class.java)!! //from the document we got, take the first entry and convert it to class User type
                    activity.memberDetails(user)
                }else{
                    activity.progressDialog.dialog.dismiss()
                    activity.showErrorSnackBar("No such member found")
                }
            }.addOnFailureListener {
                    e->
                activity.progressDialog.dialog.dismiss()
                Log.e(activity.javaClass.simpleName,"Error while getting users details",e)

            }
    }
    //update assigned member to fire store
    fun assignMemberToBoard(activity:AddDeleteMembersFromBoard,board:Board){
        val assignedToHashMap= HashMap<String,Any>()
        assignedToHashMap[Constants.ASSIGNED_TO]=board.assignedTo
        mFirestore.collection(Constants.BOARD)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                Toast.makeText(activity, "updated members detail ", Toast.LENGTH_SHORT).show()

            }.addOnFailureListener {
                activity.progressDialog.dialog.dismiss()
            }
    }
    //function to delete a board
    fun deleteBoard(activity: CenterActivity, board:Board){
        mFirestore.collection(Constants.BOARD)       //create collection named boards or access it if already exist
            .document(board.documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(activity, "Board deleted successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                    exception->
                activity.progressDialog.dialog.dismiss()
                Log.e(activity.javaClass.simpleName,"Error creating board",exception)
            }
    }
}