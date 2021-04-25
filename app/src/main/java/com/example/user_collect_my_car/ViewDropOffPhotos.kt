package com.example.user_collect_my_car

import android.os.Bundle
import android.util.Log
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

/*        mAuth = FirebaseAuth.getInstance()

        mUser = mAuth!!.currentUser
        val userID = mUser!!.uid*/

        var collection_id = intent.getStringExtra(NavigationDrawerUser.MESSGAE)


        database = FirebaseDatabase.getInstance()
        trip = database.getReference(Common.TRIP)
        firebaseAuth = FirebaseAuth.getInstance()

        trip.child(collection_id!!).child("dropOffPhotos").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (ds in snapshot.children) {

                    val id = ds.key

                    Log.d("ViewPhotos", "KEY $id")

/*                    val imageList = snapshot.getValue(TripPlanModel::class.java)

                    Log.d("ViewPhotos", "ImagePosts " + imageList)

                    if (imageList != null) {

                        imagePosts.add(imageList.toString())




                    }

                    adapter.notifyDataSetChanged()*/




                    trip.child(collection_id!!).child("dropOffPhotos").child(id!!).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            //val imageList = snapshot.getValue(TripPlanModel::class.java)

                            Log.d("ViewPhotos", "KEY ID " + snapshot.value)

                            //val imageList = snapshot.getValue(TripPlanModel::class.java)

                            //Log.d("ViewPhotos", "ImagePosts " + imageList)

                            Log.d("ViewPhotos", "SNAPSHOT TOSTRING " + snapshot.value.toString())

                            //   if (imageList != null) {

                            imagePosts.add(snapshot.value.toString())




                            //   }

                            adapter.notifyDataSetChanged()






                        }

                        override fun onCancelled(error: DatabaseError) {

                        }


                    })

                }

                Log.d("ViewPhotos", "SnapShot " + snapshot.key)

                // val imageList = snapshot.getValue(TripPlanModel::class.java)

                //Log.d("ViewPhotos", "ImageList " + imageList)

                /* if (imageList != null) {

                     imagePosts.add(imageList)

                     Log.d("ViewPhotos", "ImagePosts " + imagePosts)


                 }

                 adapter.notifyDataSetChanged()*/
            }

            override fun onCancelled(error: DatabaseError) {


            }


        })


    }

    override fun onItemClick(position: Int) {

    }
}