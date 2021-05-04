package com.example.user_collect_my_car

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.user_collect_my_car.Adapter.HistoryAdapter
import com.example.user_collect_my_car.Common.Common
import com.example.user_collect_my_car.Model.TripPlanModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_recyclerview.*
import java.util.ArrayList

class History : AppCompatActivity(), HistoryAdapter.OnItemClickListener {

    private var mAuth: FirebaseAuth? = null
    private var mUser: FirebaseUser? = null

    private lateinit var database: FirebaseDatabase

    companion object{

        val MESSGAE = "Message"
    }

    private lateinit var posts: MutableList<TripPlanModel>


    lateinit var adapter: HistoryAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)

        posts = mutableListOf()

        adapter = HistoryAdapter(this, posts, this)

        recycler_view.adapter = adapter

        recycler_view.layoutManager = LinearLayoutManager(this)

        mAuth = FirebaseAuth.getInstance()

        mUser = mAuth!!.currentUser
        val userID = mUser!!.uid

        database = FirebaseDatabase.getInstance()

        val userInfoRef = database.getReference(Common.TRIP)

        userInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                posts.clear()

                for (ds in snapshot.children) {

                    val id = ds.key

                                    val userInfoRef4 = database.getReference(Common.TRIP).child(id!!)

                                    userInfoRef4.addListenerForSingleValueEvent(object : ValueEventListener{
                                        override fun onDataChange(snapshot3: DataSnapshot) {

                                            val search = snapshot3.child("user").value

                                                if (search != null) {

                                                    if (search == userID){

                                                        val postList = snapshot3.getValue(TripPlanModel::class.java)

                                                        if (postList != null) {

                                                            posts.add(postList)
                                                        }

                                                        adapter.notifyDataSetChanged()

                                                    }
                                                }
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

    override fun onResume() {
        super.onResume()

        adapter.notifyDataSetChanged()
    }

    override fun onItemClick(position: Int) {

        var collectionNumber = posts.elementAt(position).collectionNumber

        val intent = Intent(this, UserHistory::class.java)

           intent.putExtra(MESSGAE,collectionNumber)

            startActivity(intent)
    }

    override fun onBackPressed() {

        finish()

    }
}



