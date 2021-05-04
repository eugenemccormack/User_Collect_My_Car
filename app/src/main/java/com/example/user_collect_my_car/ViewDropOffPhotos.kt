package com.example.user_collect_my_car

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.user_collect_my_car.Adapter.ImageAdapter
import com.example.user_collect_my_car.Common.Common
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_image_recyclerview.*

class ViewDropOffPhotos: AppCompatActivity(), ImageAdapter.OnItemClickListener {

    private lateinit var database : FirebaseDatabase
    private lateinit var trip : DatabaseReference
    private lateinit var firebaseAuth : FirebaseAuth

    private lateinit var imagePosts: MutableList<String>

    lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_recyclerview)

        imagePosts = mutableListOf()

        adapter = ImageAdapter(this, imagePosts, this)

        imagesRecyclerView.adapter = adapter

        imagesRecyclerView.layoutManager = LinearLayoutManager(this)

        var collection_id = intent.getStringExtra(History.MESSGAE)


        database = FirebaseDatabase.getInstance()
        trip = database.getReference(Common.TRIP)
        firebaseAuth = FirebaseAuth.getInstance()

        trip.child(collection_id!!).child("dropOffPhotos").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (ds in snapshot.children) {

                    val id = ds.key

                    trip.child(collection_id!!).child("dropOffPhotos").child(id!!).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            imagePosts.add(snapshot.value.toString())

                            adapter.notifyDataSetChanged()

                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onItemClick(position: Int) {

    }
}