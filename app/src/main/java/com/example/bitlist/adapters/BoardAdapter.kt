package com.example.bitlist.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bitlist.Constants
import com.example.bitlist.R
import com.example.bitlist.models.Board
import com.example.bitlist.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_create_board.view.*
import kotlinx.android.synthetic.main.board_single_row.view.*
import kotlinx.android.synthetic.main.board_single_row.view.iv_board_image

open class BoardAdapter(
    private val context: Context,
    private var list: ArrayList<Board>

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onClickListener: OnClickListener? = null
    private val mFirestore = FirebaseFirestore.getInstance() //instance of fire store

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.board_single_row, parent, false)
        )

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            Glide.with(context).load(model.image).centerCrop()
                .placeholder(R.drawable.logotwo).into(holder.itemView.iv_board_image)
            holder.itemView.tv_board_name.text = model.name
            holder.itemView.tv_board_created_by.text = model.createdBy
            holder.itemView.tv_end_date.text = model.endDate
            holder.itemView.tv_start_date.text = model.startDate

            //setting up recycler view to show member images on board itself
            holder.itemView.rv_board_members_images.layoutManager = GridLayoutManager(context, 6)
            val assignedTo = model.assignedTo
            //requesting firebase to get details of members of the board by passing member's id present on the board
            mFirestore.collection(Constants.USER) //in collection of name users
                .whereIn(
                    Constants.ID,
                    assignedTo
                )  //find users where id is present in assignedTo list
                .get()                             //get all users
                .addOnSuccessListener { document ->
                    var userList: ArrayList<User> = ArrayList()
                    for (i in document.documents) {   //convert them into object and store user data in a list
                        val user = i.toObject(User::class.java)!!
                        userList.add(user)
                    }
                    //setting up adapter
                    val adapter = BoardMembersPhotosAdapter(context, userList)
                    holder.itemView.rv_board_members_images.adapter = adapter

                }.addOnFailureListener { e ->
                    Toast.makeText(context, "error getting members photos", Toast.LENGTH_SHORT)
                        .show()

                }


            if (model.priority == 0) {
                holder.itemView.iv_board_priority.setImageResource(R.drawable.priority_high_circle)
            } else if (model.priority == 1) {
                holder.itemView.iv_board_priority.setImageResource(R.drawable.priority_medium_circle)
            } else {
                holder.itemView.iv_board_priority.setImageResource(R.drawable.priority_low_circle)
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}