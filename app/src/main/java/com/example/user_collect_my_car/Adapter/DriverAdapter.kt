package com.example.user_collect_my_car.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.user_collect_my_car.Model.DriverGeoModel
import com.example.user_collect_my_car.Model.DriverInfoModel
import com.example.user_collect_my_car.Model.UserModel
import com.example.user_collect_my_car.R
import kotlin.collections.ArrayList

class DriverAdapter (internal var context: Context): RecyclerView.Adapter<DriverAdapter.MyViewHolder>() {

    internal var driverList: MutableList<UserModel>

    val lastItemId: String?
        get() = driverList[driverList.size - 1].phone

    fun addAll(newDrivers: ArrayList<UserModel>){

        val init = driverList.size
        driverList.addAll(newDrivers)
        notifyItemRangeChanged(init, newDrivers.size)

    }

    fun removeLastItem(){

        driverList.removeAt(driverList.size - 1)

    }

    init {

        this.driverList = ArrayList()

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(context).inflate(R.layout.activity_recyclerview_layout, parent, false)

        return MyViewHolder(itemView)



    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.driver_name.text = driverList[position].name
        holder.driver_email.text = driverList[position].email



    }

    override fun getItemCount(): Int {

        return driverList.size



    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){



        internal var driver_name: TextView = itemView.findViewById<TextView>(R.id.driver_name)

        internal var driver_email: TextView = itemView.findViewById<TextView>(R.id.driver_email)


    }
}