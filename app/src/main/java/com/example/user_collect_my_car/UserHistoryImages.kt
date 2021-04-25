package com.example.user_collect_my_car

import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.user_collect_my_car.Adapter.DriverAdapter
import com.example.user_collect_my_car.Adapter.ImageAdapter
import com.example.user_collect_my_car.Model.TripPlanModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_recyclerview.*

class UserHistoryImages: AppCompatActivity(), ImageAdapter.OnItemClickListener {

    private var mAuth: FirebaseAuth? = null
    private var mUser: FirebaseUser? = null
    var db: DatabaseReference? = null

    private lateinit var imagePosts: MutableList<TripPlanModel>

    lateinit var adapter: ImageAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_recyclerview)


        imagePosts = mutableListOf()

        adapter = ImageAdapter(this, imagePosts, this)

        recycler_view.adapter = adapter

        recycler_view.layoutManager = LinearLayoutManager(this)

        mAuth = FirebaseAuth.getInstance()

        mUser = mAuth!!.currentUser
        val userID = mUser!!.uid
    }

    override fun onItemClick(position: Int) {

    }
}
