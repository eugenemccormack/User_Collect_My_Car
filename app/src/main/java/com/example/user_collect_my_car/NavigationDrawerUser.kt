package com.example.user_collect_my_car

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.user_collect_my_car.Adapter.DriverAdapter
import com.example.user_collect_my_car.Common.Common
import com.example.user_collect_my_car.Model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_recyclerview.*

class NavigationDrawerUser : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var mUser: FirebaseUser? = null
    var db: DatabaseReference? = null

    //Variables

    private lateinit var database : FirebaseDatabase
    private lateinit var userInfoRef : DatabaseReference

    val ITEM_COUNT = 21
    var total_item = 0
    var last_visable_item = 0

    lateinit var adapter: DriverAdapter

    var isLoading = false
    var isMaxData = false

    var last_node: String ?= ""
    var last_key: String ?= ""




    lateinit var toggle: ActionBarDrawerToggle

    private lateinit var image_avatar : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)//activity_navigation_drawer_user)


        getLastKey()

        val layoutManger = LinearLayoutManager(this)
       recycler_view.layoutManager = layoutManger

        val dividerItemDecoration = DividerItemDecoration(recycler_view.context, layoutManger.orientation)
        recycler_view.addItemDecoration(dividerItemDecoration)

        adapter = DriverAdapter(this)
        recycler_view.adapter = adapter

        getDrivers()

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

      /*  toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.closed)

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

        }*/


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

            Log.d("Nav1", FirebaseDatabase.getInstance().getReference("UserInfo").child(userID).toString())

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

                        Log.d("Nav3", "hasChildren()")

                        val driverList = ArrayList<UserModel>()

                        for (snapshot: DataSnapshot in snapshot.children)


                            driverList.add(snapshot.getValue(UserModel::class.java)!!)

                        last_node = driverList[driverList.size - 1].phone

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

        /*if (toggle.onOptionsItemSelected(item)){

            return true

        }

        else */
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
    }
}

