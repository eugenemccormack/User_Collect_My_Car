package com.example.user_collect_my_car.Callback

import com.example.user_collect_my_car.Model.DriverGeoModel

interface FirebaseDriverInfoListener {

    fun onDriverInfoLoadSuccess(driverGeoModel: DriverGeoModel?)


}