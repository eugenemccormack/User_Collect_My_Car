package com.example.user_collect_my_car.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.example.user_collect_my_car.Model.TripPlanModel
import com.example.user_collect_my_car.Model.UserModel
import com.example.user_collect_my_car.R
import kotlinx.android.synthetic.main.activity_recyclerview_layout.view.*
import kotlin.collections.ArrayList

class ImageAdapter (val context: Context, val imagePosts: List<TripPlanModel>, private val listener: OnItemClickListener): RecyclerView.Adapter<ImageAdapter.MyViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageAdapter.MyViewHolder {

        val itemView = LayoutInflater.from(context).inflate(R.layout.activity_view_images, parent, false)

        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: ImageAdapter.MyViewHolder, position: Int) {

        holder.bind(imagePosts[position])

    }

    override fun getItemCount() = imagePosts.size
    //) /*(internal var context: Context): RecyclerView.Adapter<DriverAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        fun bind(tripPlanModel: TripPlanModel) {

            itemView.driver_name.text = tripPlanModel.time
            itemView.driver_email.text = tripPlanModel.user


        }
        init{

            itemView.setOnClickListener(this)

        }


        override fun onClick(v: View?) {

            val position = adapterPosition

            if(position != RecyclerView.NO_POSITION) {

                listener.onItemClick(position)

            }

        }


    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)


    }


}