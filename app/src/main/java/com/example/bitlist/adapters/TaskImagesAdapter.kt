package com.example.bitlist.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bitlist.R
import com.example.bitlist.models.Task
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.task_images_single_column_for_rv.view.*
import kotlinx.android.synthetic.main.task_list_single_row.view.*
import java.io.IOException

//pass array list of images
open class TaskImagesAdapter(
    private val context: Context,
    private var list: ArrayList<String>
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var onClickListener:OnClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.task_images_single_column_for_rv,parent,false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model=list[position]
        Log.d("Loading this Image",model)
        if(holder is MyViewHolder){
            if(model.isNotEmpty()){
                try {
                    Glide
                        .with(context)
                        .load(model)
                        .centerCrop()
                        .placeholder(R.drawable.logo)
                        .into(holder.itemView.iv_img_in_task);
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            //sending position and task model to task list activity
            holder.itemView.setOnClickListener {
                if(onClickListener!=null){
                    onClickListener!!.onClick(position,model)
                }
            }
        }
    }

    interface OnClickListener{
        fun onClick(position:Int,model:String)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener=onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

}