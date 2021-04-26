package com.example.user_collect_my_car

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.user_collect_my_car.Common.Common
import com.example.user_collect_my_car.Model.TripPlanModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_user_history.*
import kotlinx.android.synthetic.main.activity_view_driver_info.*
import java.io.IOException
import java.util.*

class UserHistory: AppCompatActivity() {

    companion object{

        val MESSGAE = "Message"
    }


    private lateinit var database : FirebaseDatabase
    private lateinit var trip : DatabaseReference
    private lateinit var firebaseAuth : FirebaseAuth





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_history)




        //collection_id = driver_Key.text.toString()

        //collection_id.text = driverKey

        getCollectionFromFirebase()

    }

    private fun getCollectionFromFirebase() {

        Log.d("ViewCollection", "getUserFromFirebase")

        var collection_id = intent.getStringExtra(History.MESSGAE)

        database = FirebaseDatabase.getInstance()
        trip = database.getReference(Common.TRIP)
        firebaseAuth = FirebaseAuth.getInstance()

        Log.d("ViewCollection", trip.toString())

        if (collection_id != null) {

            Log.d("ViewCollection", "driverKey != null")

            trip.child(collection_id)
                    .addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(p0: DataSnapshot) {

                            Log.d("ViewCollection", trip.child(collection_id).toString())

                            Log.d("ViewCollection", "onDataChange")

                            //  if(p0.exists()){

                            Log.d("ViewCollection", "p0.exists")

                            val model = p0.getValue(TripPlanModel::class.java)

                            loadCollectionInfo(model)

                            //Common.selectedDriver = model




                            Log.d("ViewCollection", model.toString())

                            /*  val driverTestString = ""

                              driverTest.text = model.toString()*/

                            // }

                        }

                        override fun onCancelled(p0: DatabaseError) {

                            Toast.makeText(this@UserHistory, p0.message, Toast.LENGTH_SHORT).show()

                        }


                    })
        }

    }

    private fun loadCollectionInfo(model: TripPlanModel?) {



        val driver_profileImage = findViewById<android.view.View>(R.id.driver_profileImage_history) as ImageView

        Common.collectionInfo = model

        collectionID_history.text =  Common.collectionInfo!!.collectionNumber

        driver_date_history.text =  Common.collectionInfo!!.time
        from_location_history.text =  Common.collectionInfo!!.originString
        to_location_history.text =  Common.collectionInfo!!.destinationString

        var complete: Boolean

        complete = Common.collectionInfo!!.done



        if (complete){

            driver_complete_history.text = "Complete"

            Log.d("UserHistory", "isDone True : " +  Common.collectionInfo!!.done.toString())
        }
        else{

            driver_complete_history.text = "In Progress"

            Log.d("UserHistory", "isDone False : " +  Common.collectionInfo!!.done.toString())

        }



        driver_price_history.text =  Common.collectionInfo!!.distanceDestination

        driver_name_history.text = Common.collectionInfo!!.driverInfoModel!!.name
        driver_email_history.text = Common.collectionInfo!!.driverInfoModel!!.email
        driver_phone_history.text = Common.collectionInfo!!.driverInfoModel!!.phone

/*        val geoCoder = Geocoder(this@UserHistory, Locale.getDefault())

        var collectionLocation = Common.collectionInfo!!.originString

        Log.d("UserHistory", "Address $collectionLocation")

        var latlong =  Common.collectionInfo!!.originString!!.split(',');
        var latitude = (latlong[0]).toDouble()
        var longitude = (latlong[1]).toDouble()

        var cityName = ""

        val addressList : List<Address>?// = ArrayList()

        try {

            addressList = geoCoder.getFromLocation(latitude,
                    longitude, 1)

            Log.d("UserHistory", "ADDRESS NEW $addressList")

            if (addressList.isNotEmpty())

                cityName = addressList[0].getAddressLine(0)//countryName//adminArea//locality//subAdminArea//subLocality

            Log.d("UserHistory", "Address $cityName")

            from_location_history.text = cityName

        }
        catch (e: IOException){

            Log.d("UserHistory", "IOException $e")

        }*/




        if(Common.collectionInfo != null && Common.collectionInfo!!.driverInfoModel!!.image != null){

            Glide.with(this).load(Common.collectionInfo!!.driverInfoModel!!.image).into(driver_profileImage)

        }

        collection_photos.setOnClickListener {

            var collection_id2 = intent.getStringExtra(History.MESSGAE)

            val intent = Intent(this, ViewCollectionPhotos::class.java)

            intent.putExtra(History.MESSGAE, collection_id2)

            startActivity(intent)

        }

        drop_off_photos.setOnClickListener {

            var collection_id2 = intent.getStringExtra(History.MESSGAE)

            val intent = Intent(this, ViewDropOffPhotos::class.java)

            intent.putExtra(History.MESSGAE, collection_id2)

            startActivity(intent)

        }








    }

/*    override fun onBackPressed() {

        finish()

    }*/
}