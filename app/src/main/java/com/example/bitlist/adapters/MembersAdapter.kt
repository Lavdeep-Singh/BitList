package com.example.bitlist.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bitlist.R
import com.example.bitlist.models.Task
import com.example.bitlist.models.User
import kotlinx.android.synthetic.main.add_delete_member_single_row.view.*
import kotlinx.android.synthetic.main.task_list_single_row.view.*

open class MembersAdapter(
    private val context: Context,
    private var list: ArrayList<User>
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var onClickListener:OnClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.add_delete_member_single_row,parent,false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model=list[position]
        if(holder is MyViewHolder){

            holder.itemView.tv_member_name.text=model.name
            holder.itemView.tv_member_email.text=model.email
            Glide                   //use glide to populate header image
                .with(context)
                .load(model.image)   //load this image
                .centerCrop()
                .placeholder(R.drawable.logotwo)  //default image
                .into(holder.itemView.iv_member_image);          //populate this image view  her

            //sending position and task model to task list activity
            holder.itemView.setOnClickListener {
                if(onClickListener!=null){
                   onClickListener!!.onClick(position,model)
                }
            }
        }
    }

    interface OnClickListener{
        fun onClick(position:Int,model: User)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener=onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

}