package com.example.bitlist.adapters

import android.app.Activity
import android.content.Context
import android.nfc.Tag
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.input.key.Key.Companion.D
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bitlist.BuildConfig.DEBUG
import com.example.bitlist.Constants
import com.example.bitlist.Firestore.FireStoreClass
import com.example.bitlist.R
import com.example.bitlist.activity.AddDeleteMembersFromBoard
import com.example.bitlist.activity.CenterActivity
import com.example.bitlist.models.Board
import com.example.bitlist.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.board_single_row.view.*
import kotlinx.android.synthetic.main.single_member_photo.view.*

open class BoardMembersPhotosAdapter(
    private val context: Context,
    private var list: ArrayList<User>
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.single_member_photo,parent,false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model=list[position]
        if(holder is MyViewHolder){
            Glide.with(context).load(model.image).centerCrop()
                .placeholder(R.drawable.account).into(holder.itemView.single_member_photo_hd)

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

}