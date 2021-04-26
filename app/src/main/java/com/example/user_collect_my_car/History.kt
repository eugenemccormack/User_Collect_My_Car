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
    var db: DatabaseReference? = null

    //Variables

    private lateinit var database: FirebaseDatabase
    private lateinit var userInfoRef: DatabaseReference

    val ITEM_COUNT = 21
    var total_item = 0
    var last_visable_item = 0

    //lateinit var adapter: DriverAdapter

    var isLoading = false
    var isMaxData = false

    var last_node: String? = ""
    var last_key: String? = ""

    companion object{

        val MESSGAE = "Message"
    }

    private lateinit var posts: MutableList<TripPlanModel>
    lateinit var testList: List<TripPlanModel>
    lateinit var arrayTest: ArrayList<TripPlanModel>

    lateinit var adapter: HistoryAdapter


    lateinit var toggle: ActionBarDrawerToggle

    private lateinit var image_avatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)//activity_navigation_drawer_user)

        posts = mutableListOf()

        adapter = HistoryAdapter(this, posts, this)

        recycler_view.adapter = adapter

        recycler_view.layoutManager = LinearLayoutManager(this)

        mAuth = FirebaseAuth.getInstance()

        mUser = mAuth!!.currentUser
        val userID = mUser!!.uid


        database = FirebaseDatabase.getInstance()
        //val userInfoRef = database.getReference(Common.USER_INFO_REFERENCE).child(userID).child("Collections")//.child("Test")//.child("MYzpNmIdZjgREGhR9_k")//.child("MYzomuuS0csr4LGlhYu")//.child(userID).child("Collections").child("4464204406145883896")//.orderByKey()//child("2185699360067625422")//.child(userID).child("Collections").child("4464204406145883896")

        val userInfoRef = database.getReference(Common.TRIP)

        userInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                posts.clear()

                for (ds in snapshot.children) {

                    val id = ds.key

                    //val text = ds.child("user:").getValue(TripPlanModel::class.java)
//                    val time = ds.child("driver").getValue(UserModel::class.java)
                    Log.d("Test ", "Key " + id )//+ " Test " + time)
                    //Log.d("Test ", "User " + text )

                            val userInfoRef3 = database.getReference(Common.TRIP).child(id!!).child("user")//orderByChild("user").equalTo(userID)

                            userInfoRef3.addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onDataChange(snapshot2: DataSnapshot) {

                                    //for (ds2 in snapshot.children) {

                                        //val id2 = snapshot2.getValue(TripPlanModel::class.java)
                                        //val text = ds.child("user:").getValue(TripPlanModel::class.java)

                                    Log.d("Test ", "Find 2.0  " + snapshot2)

                                        if(snapshot2.value?.equals(userID) == true) {

                                            Log.d("Test ", "FOUND  " + snapshot2) //Displays This: DataSnapshot { key = user, value = QdCe2CjMTvbWCGHvAoHXGQehGL33 }

                                            Log.d("Test ", "FOUND KEY  " + snapshot2.key)



                                        }
                                       // Log.d("Test ", "Find 2.0  " + id2)

                                  //  }


                                    //Log.d("Test ", "Find 2.0  " + userInfoRef3.toString() )

                                   // Log.d("Test ", "FINDING $snapshot2")

                                    //val text = ds.child("user:").getValue(TripPlanModel::class.java)
                                    //val id2 = snapshot2.key

                                   // Log.d("Test ", "Key2 " + id2 )

                                   //val postList2 = snapshot2.getValue(TripPlanModel::class.java)

                                    //val username = snapshot2.getValue(TripPlanModel::class.java)

                                    //if(userID == username.toString()){

                                       // Log.d("Test", "Username " + postList2)

                                    //}

                                }

                                override fun onCancelled(error: DatabaseError) {


                                }
                            })
                                    val userInfoRef4 = database.getReference(Common.TRIP).child(id!!)//.child("user")//orderByChild("user").equalTo(userID)

                                    userInfoRef4.addListenerForSingleValueEvent(object : ValueEventListener{
                                        override fun onDataChange(snapshot3: DataSnapshot) {

                                            //for (ds2 in snapshot.children) {

                                            val search = snapshot3.child("user").value

                                            val search2 = snapshot3.key

                                            val search3 = snapshot3.child("Braking").child("MYlq3pbJyRDl_cY0EY1").child("sensor").value

                                            Log.d("Test ", "SEARCH " + search)

                                            Log.d("Test ", "BRAKING " + search3)



                                            val searchPhoto = snapshot3.child("collectionPhotos")

                                            val searchPhotoKey = snapshot3.child("collectionPhotos").key

                                            val searchPhotoChildren = snapshot3.child("collectionPhotos").children

                                            for (ds in snapshot.children) {


                                            }



                                            Log.d("Test ", "PHOTO SEARCH " + searchPhoto)

                                            Log.d("Test ", "PHOTO SEARCH KEY " + searchPhotoKey)

                                            Log.d("Test ", "PHOTO SEARCH CHILDREN " + searchPhotoChildren)

                                                if (search != null) {

                                                    if (search == userID){

                                                        Log.d("Test ", "SUCCESSFUL " + search)
                                                        Log.d("Test ", "SUCCESSFUL KEY " + search2)

                                                        val testMap = HashMap<String, TripPlanModel>()


                                                        val postList = snapshot3.getValue(TripPlanModel::class.java) ?: return

                                                        testMap[snapshot3.key!!] = postList

                                                        Log.d("Test", "TESTMAP $testMap")

                                                        Log.d("Test", "SnapShot $postList") //This Works to get User / destinationString


                                                        if (postList != null) {

                                                            posts.add(postList)


                                                        }

                                                        adapter.notifyDataSetChanged()

                                                    }
                                                }
                                                //for (ds2 in snapshot.children) {

                                                //val id2 = snapshot2.getValue(TripPlanModel::class.java)
                                                //val text = ds.child("user:").getValue(TripPlanModel::class.java)

                                                Log.d("Test ", "FIND 2.0000  " + snapshot3)

                                                if(snapshot3.value?.equals(userID) == true) {

                                                    Log.d("Test ", "FOUND  2 " + snapshot3) //Displays This: DataSnapshot { key = user, value = QdCe2CjMTvbWCGHvAoHXGQehGL33 }

                                                    Log.d("Test ", "FOUND KEY  2 " + snapshot3.key)

                                                    Log.d("Test ", "FOUND KEY  2 " + snapshot3.value)



                                                }

                                            //}

                                        }

                                        override fun onCancelled(error: DatabaseError) {


                                        }
                                    })



                   /* //val userInfoRef2 = database.getReference(Common.USER_INFO_REFERENCE).child(userID).child("Collections").child(id!!)//.child("driverInfoModel")

                    val userInfoRef2 = database.getReference(Common.TRIP).child(id!!)//.orderByChild("user").equalTo(userID)//.child("driverInfoModel")

                    //val username = ds.child("user").getValue(TripPlanModel::class.java)
                    //Log.d("Test ", "Find  " + userInfoRef2 )

                    //Log.d("Test", "Username " + username)

                    userInfoRef2.addListenerForSingleValueEvent(object : ValueEventListener{

                        override fun onDataChange(snapshot: DataSnapshot) {


                            // val username = snapshot.child("user").getValue(TripPlanModel::class.java)

                            val postList = snapshot.getValue(TripPlanModel::class.java)

                            //Log.d("Test", "SnapShot $postList") //This Works to get User / destinationString


                            if (postList != null) {

                                posts.add(postList)

                            }

                            adapter.notifyDataSetChanged()
                            //}
                        }


                        override fun onCancelled(error: DatabaseError) {

                        }


                    })*/




                    //     adapter.notifyDataSetChanged()









                }


            }

            override fun onCancelled(error: DatabaseError) {

            }




        })




/*

        getLastKey()

        val layoutManger = LinearLayoutManager(this)
       recycler_view.layoutManager = layoutManger

        val dividerItemDecoration = DividerItemDecoration(recycler_view.context, layoutManger.orientation)
        recycler_view.addItemDecoration(dividerItemDecoration)

        adapter = DriverAdapter(this)
        recycler_view.adapter = adapter*/

/*        getDrivers()

        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                total_item = layoutManger.itemCount

                last_visable_item = layoutManger.findLastVisibleItemPosition()

                if (!isLoading && total_item <= last_visable_item + ITEM_COUNT) {

                    getDrivers()

                    isLoading = true

                }
            }


        })

      *//*  toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.closed)

        drawerLayout.addDrawerListener(toggle)

        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {

            when(it.itemId){

                R.id.mItem1 -> {

                    intent = Intent(this, MapsActivity::class.java)

                    startActivity(intent)

                    Toast.makeText(applicationContext, "Maps", Toast.LENGTH_SHORT).show()
                }

                R.id.mItem2 -> {

                    intent = Intent(this, NavigationDrawerUser::class.java)

                    startActivity(intent)

                    Toast.makeText(applicationContext, "Navigation Activity", Toast.LENGTH_SHORT).show()
                }

                R.id.mItem3 -> {

                    val builder = AlertDialog.Builder(this)

                    builder.setTitle("Logout")
                        .setMessage("Do you really want to Logout?")
                        .setNegativeButton("Cancel", {dialogInterface, _ -> dialogInterface.dismiss()})

                        .setPositiveButton("Logout") { dialogInterface, _ ->

                            FirebaseAuth.getInstance().signOut()

                            val intent = Intent(this, LoginUser::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish()

                        }.setCancelable(false)

                    val dialog = builder.create()
                    dialog.setOnShowListener {

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.red))
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.blueInk))

                    }

                    dialog.show()
                }

            }

            true

        }

        val headerView = navView.getHeaderView(0)
        val navName = headerView.findViewById<android.view.View>(R.id.nav_name) as TextView
        val navPhone = headerView.findViewById<android.view.View>(R.id.nav_phone) as TextView
        image_avatar = headerView.findViewById<android.view.View>(R.id.nav_imageView) as ImageView

        navName.setText(Common.buildNavMessage())
        navPhone.setText(Common.currentUser!!.phone)

        if(Common.currentUser != null && Common.currentUser!!.image != null){

            Glide.with(this).load(Common.currentUser!!.image).into(image_avatar)

        }*//*


    }

    private fun getDrivers() {

        mAuth = FirebaseAuth.getInstance()

        mUser = mAuth!!.currentUser
        val userID = mUser!!.uid

        val fireDBUser = FirebaseDatabase.getInstance().getReference("UserInfo").child(userID)//.child("Note") //.child("title");;


        database = FirebaseDatabase.getInstance()
        userInfoRef = database.getReference(Common.USER_INFO_REFERENCE)

        if (!isMaxData) {

            val query: Query

            Log.d("Nav1", FirebaseDatabase.getInstance().getReference("UserInfo").child(userID).child("name").toString())

            if (TextUtils.isEmpty(last_node)) {

                query =  FirebaseDatabase.getInstance().getReference("UserInfo").child(userID)
                    .orderByKey()
                    .limitToFirst(ITEM_COUNT)

                Log.d("Nav2", query.toString())

            } else {

                query =  FirebaseDatabase.getInstance().getReference("UserInfo").child(userID)
                    .orderByKey()
                    .startAt(last_node)
                    .limitToFirst(ITEM_COUNT)

            }

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.hasChildren()) {

                        snapshot.getValue(UserModel::class.java)

                        val driverList = ArrayList<UserModel>()

                        Log.d("Nav3.2", "SnapShot " + snapshot.getValue(UserModel::class.java))

                        for (snapshot: DataSnapshot in snapshot.children) {

                            Log.d("Nav3.3", "SnapShot " + snapshot.getValue(UserModel::class.java))

                            val post = snapshot.getValue(UserModel::class.java)

                            driverList.add(post!!)

                        }

                        last_node = driverList[driverList.size - 1].name

                        Log.d("Nav4", last_node.toString())

                        if (!last_node.equals(last_key))

                            driverList.removeAt(driverList.size - 1)
                        else

                            last_node = "end"

                        adapter.addAll(driverList)

                        isLoading = false

                    } else {

                        isLoading = false
                        isMaxData = true

                    }


                }

                override fun onCancelled(error: DatabaseError) {


                }


            })

        }

    }

    private fun getLastKey() {

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/UserInfo/$uid")

        mAuth = FirebaseAuth.getInstance()

        mUser = mAuth!!.currentUser
        val userID = mUser!!.uid

        database = FirebaseDatabase.getInstance()
         userInfoRef = database.getReference(Common.USER_INFO_REFERENCE)

        Log.d("Nav5", "Entered getLastKey()")

        val get_last_key: Query =  FirebaseDatabase.getInstance().getReference("UserInfo").child(userID)
            .orderByKey()
            .limitToLast(1)

        Log.d("Nav6", FirebaseDatabase.getInstance().getReference("UserInfo").child(userID).toString())

        Log.d("Nav7", FirebaseDatabase.getInstance().getReference("UserInfo").child(userID).toString()
        )

        Log.d("Nav8", "onDataChange1")

        get_last_key.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                Log.d("Nav9", "onDataChange2")


                for (driverSnapShot: DataSnapshot in p0.children)

                    last_key = driverSnapShot.key

                Log.d("Nav10", last_key.toString())

            }

            override fun onCancelled(error: DatabaseError) {


            }


        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        *//*if (toggle.onOptionsItemSelected(item)){

            return true

        }

        else *//*
        if(id == R.id.refresh) {

            isMaxData = false
            last_node = adapter.lastItemId
            adapter.removeLastItem()
            adapter.notifyDataSetChanged()
            getLastKey()
            getDrivers()

        }

            return true


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.recyclerview_menu, menu)

        return true
    }*/

    }

    override fun onResume() {
        super.onResume()

        //posts.clear()

        adapter.notifyDataSetChanged()
    }

    override fun onItemClick(position: Int) {

        Toast.makeText(this, "Item $position clicked", Toast.LENGTH_SHORT).show()
        val clickedItem = posts[position]

        //posts.indexOf(clickedItem.user)

        var collectionNumber = posts.elementAt(position).collectionNumber



        //Log.d("ViewCollection", "LIST POSITION " + posts.indexOf(clickedItem.user))

        Log.d("ViewCollection", "ELEMENT AT  POSITION " + posts.elementAt(position))

        Log.d("ViewCollection", "ELEMENT AT  POSITION USER " + posts.elementAt(position).user)

        Log.d("ViewCollection", "ELEMENT AT  POSITION USER " + posts.elementAt(position).collectionNumber)


        val intent = Intent(this, UserHistory::class.java)

           intent.putExtra(MESSGAE,collectionNumber)

            startActivity(intent)


    }

    override fun onBackPressed() {

        //posts.clear()

        finish()

    }
}



