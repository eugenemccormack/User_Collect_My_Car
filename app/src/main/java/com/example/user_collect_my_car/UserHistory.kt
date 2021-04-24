package com.example.user_collect_my_car

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_view_driver_info.*

class UserHistory: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_history)


        var driverKey = intent.getStringExtra(MapsActivity.MESSGAE)

        //driverKey = driver_Key.text.toString()

        driver_Key.text = driverKey

    }
}