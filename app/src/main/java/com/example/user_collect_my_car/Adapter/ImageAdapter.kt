package com.example.user_collect_my_car.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.example.user_collect_my_car.R
import kotlinx.android.synthetic.main.activity_recyclerview_layout.view.*
import kotlinx.android.synthetic.main.activity_view_images.view.*



class ImageAdapter (val context: Context, val imagePosts: List<String>, private val listener: OnItemClickListener): RecyclerView.Adapter<ImageAdapter.MyViewHolder>()  {
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
        fun bind(s: String) {//tripPlanModel: TripPlanModel) {

            Log.d("ImageAdapter", "String Null $s")

/*            if(s.isBlank()){

                itemView.image_title.text = "Empty "

            }*/




            //itemView.image_title.text = "Photo " + photoNumber//tripPlanModel.collectionPhotos.toString()//tripPlanModel.collectionsPhotos!!.photos

          // Log.d("ImageAdapter", "IMAGE 1 " + tripPlanModel.collectionPhotos)//!!.photos)

           Glide.with(context).load(s).into(itemView.image)

           // photoNumber++

            //Log.d("ImageAdapter", "IMAGE" + tripPlanModel.collectionPhotos)//!!.photos)

            // itemView.driver_email.text = tripPlanModel.user


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