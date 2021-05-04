package com.example.user_collect_my_car

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.user_collect_my_car.Common.Common
import com.example.user_collect_my_car.Model.TripPlanModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_user_history.*
import java.util.*

class UserHistory: AppCompatActivity() {

    private lateinit var database : FirebaseDatabase
    private lateinit var trip : DatabaseReference
    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_history)

        getCollectionFromFirebase()

    }

    private fun getCollectionFromFirebase() {

        var collection_id = intent.getStringExtra(History.MESSGAE)

        database = FirebaseDatabase.getInstance()
        trip = database.getReference(Common.TRIP)
        firebaseAuth = FirebaseAuth.getInstance()

        if (collection_id != null) {

            trip.child(collection_id)
                    .addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(p0: DataSnapshot) {


                            val model = p0.getValue(TripPlanModel::class.java)

                            loadCollectionInfo(model)

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
        driver_distance_history.text = Common.collectionInfo!!.distanceText
        driver_duration_history.text = Common.collectionInfo!!.durationText

        var complete: Boolean

        complete = Common.collectionInfo!!.done



        if (complete){

            driver_complete_history.text = "Complete"

        }
        else{

            driver_complete_history.text = "In Progress"


        }

        driver_braking_count_history.text = Common.collectionInfo!!.brakingCount.toString()  + " times"
        driver_old_rating_history.text = Common.collectionInfo!!.driverInfoModel!!.rating.toString()
        driver_current_rating_history.text = Common.collectionInfo!!.newDriverRating.toString()


        val total: Double = Common.collectionInfo!!.estimatedPrice

        val round = Math.round(total * 100.0) / 100.0

        driver_estimated_total_history.text = "€"+round.toString()

        driver_price_history.text = "€"+Common.collectionInfo!!.totalPrice!!.toString()

        driver_name_history.text = Common.collectionInfo!!.driverInfoModel!!.name
        driver_email_history.text = Common.collectionInfo!!.driverInfoModel!!.email
        driver_phone_history.text = Common.collectionInfo!!.driverInfoModel!!.phone

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
}