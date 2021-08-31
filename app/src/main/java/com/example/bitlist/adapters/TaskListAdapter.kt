package com.example.bitlist.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bitlist.R
import com.example.bitlist.models.Board
import com.example.bitlist.models.Task
import kotlinx.android.synthetic.main.board_single_row.view.*
import kotlinx.android.synthetic.main.task_list_single_row.view.*

open class TaskListAdapter(
    private val context: Context,
    private var list: ArrayList<Task>
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var onClickListener:OnClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.task_list_single_row,parent,false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model=list[position]
        if(holder is MyViewHolder){

            holder.itemView.et_task_name.text = model.title
           // holder.itemView.tv_board_created_by.text=model.createdBy
            holder.itemView.et_task_desc.text = model.description

           //sending position and task model to task list activity
            holder.itemView.setOnClickListener {
                if(onClickListener!=null){
                    onClickListener!!.onClick(position,model)
                }
            }
        }
    }

    interface OnClickListener{
        fun onClick(position:Int,model: Task)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener=onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

}