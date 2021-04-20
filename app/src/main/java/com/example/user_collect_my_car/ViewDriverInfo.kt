package com.example.user_collect_my_car

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.user_collect_my_car.Common.Common
import com.example.user_collect_my_car.Common.Common.DRIVER_INFO_REFERENCE
import com.example.user_collect_my_car.Model.DriverInfoModel
import com.example.user_collect_my_car.Model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_view_driver_info.*

class ViewDriverInfo : AppCompatActivity() {

    private lateinit var database : FirebaseDatabase
    private lateinit var driverInfoRef : DatabaseReference
    private lateinit var firebaseAuth : FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_driver_info)

        var driverKey = intent.getStringExtra(MapsActivity.MESSGAE)

        //driverKey = driver_Key.text.toString()

        driver_Key.text = driverKey

        getDriverFromFirebase()

         select_driver.setOnClickListener {


             Common.selectedDriverKey = intent.getStringExtra(MapsActivity.MESSGAE)

             Log.d("ViewDriverInfo",  Common.selectedDriverKey.toString())

            // Log.d("ViewDriverInfo",  Common.selectedDriver.name)

             finish()


        }



    }

    private fun getDriverFromFirebase(){

        Log.d("ViewDriverInfo", "getUserFromFirebase")

        var driverKey = intent.getStringExtra(MapsActivity.MESSGAE)

        database = FirebaseDatabase.getInstance()
        driverInfoRef = database.getReference(DRIVER_INFO_REFERENCE)
        firebaseAuth = FirebaseAuth.getInstance()

        Log.d("ViewDriverInfo", driverInfoRef.toString())

        if (driverKey != null) {

            Log.d("ViewDriverInfo", "driverKey != null")

            driverInfoRef.child(driverKey)
                    .addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(p0: DataSnapshot) {

                            Log.d("ViewDriverInfo", driverInfoRef.child(driverKey).toString())

                            Log.d("ViewDriverInfo", "onDataChange")

                          //  if(p0.exists()){

                                Log.d("ViewDriverInfo", "p0.exists")

                                val model = p0.getValue(DriverInfoModel::class.java)

                            loadDriverInfo(model)

                                //Common.selectedDriver = model




                                Log.d("ViewDriverInfo", model.toString())

                              /*  val driverTestString = ""

                                driverTest.text = model.toString()*/

                           // }

                        }

                        override fun onCancelled(p0: DatabaseError) {

                            Toast.makeText(this@ViewDriverInfo, p0.message, Toast.LENGTH_SHORT).show()

                        }


                    })
        }



    }

   private fun loadDriverInfo(model: DriverInfoModel?) {

       val driver_profileImage = findViewById<android.view.View>(R.id.driver_profileImage) as ImageView

        Common.selectedDriver = model

       driver_Name.text = Common.selectedDriver!!.name
       driver_Email.text = Common.selectedDriver!!.email
       driver_Phone.text = Common.selectedDriver!!.phone

       if(Common.selectedDriver != null && Common.selectedDriver!!.image != null){

           Glide.with(this).load(Common.selectedDriver!!.image).into(driver_profileImage)

       }








    }

    override fun onBackPressed() {

        finish()

    }
}