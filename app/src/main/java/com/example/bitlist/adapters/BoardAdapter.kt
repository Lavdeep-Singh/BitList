package com.example.bitlist.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bitlist.R
import com.example.bitlist.models.Board
import kotlinx.android.synthetic.main.activity_create_board.view.*
import kotlinx.android.synthetic.main.board_single_row.view.*
import kotlinx.android.synthetic.main.board_single_row.view.iv_board_image

open class BoardAdapter(
    private val context: Context,
    private var list: ArrayList<Board>
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var onClickListener:OnClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.board_single_row,parent,false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model=list[position]
        if(holder is MyViewHolder){
            Glide.with(context).load(model.image).centerCrop()
                .placeholder(R.drawable.logotwo).into(holder.itemView.iv_board_image)
            holder.itemView.tv_board_name.text=model.name
            holder.itemView.tv_board_created_by.text=model.createdBy
            holder.itemView.tv_end_date.text=model.endDate
            holder.itemView.tv_start_date.text=model.startDate
            if(model.priority==0){
                holder.itemView.iv_board_priority.setImageResource(R.drawable.priority_high_circle)
            }else if(model.priority==1){
                holder.itemView.iv_board_priority.setImageResource(R.drawable.priority_medium_circle)
            }else{
                holder.itemView.iv_board_priority.setImageResource(R.drawable.priority_low_circle)
            }

            holder.itemView.setOnClickListener {
                if(onClickListener!=null){
                    onClickListener!!.onClick(position,model)
                }
            }
        }
    }

    interface OnClickListener{
        fun onClick(position:Int,model:Board)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener=onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

}