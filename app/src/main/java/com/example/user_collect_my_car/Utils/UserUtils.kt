package com.example.user_collect_my_car.Utils

import android.content.Context
import android.util.Log
import android.widget.RelativeLayout
import android.widget.Toast
import com.example.user_collect_my_car.Common.Common
import com.example.user_collect_my_car.Model.DriverGeoModel
import com.example.user_collect_my_car.Model.EventBus.SelectedPlaceEvent
import com.example.user_collect_my_car.Model.FCMSendData
import com.example.user_collect_my_car.Model.TokenModel
import com.example.user_collect_my_car.R
import com.example.user_collect_my_car.Remote.IFCMService
import com.example.user_collect_my_car.Remote.RetroFitFCMClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

object UserUtils {

    fun updateToken(context: Context, token: String) {

        val tokenModel = TokenModel()

        tokenModel.token = token;

        FirebaseDatabase.getInstance().getReference(Common.TOKEN_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .setValue(tokenModel)
            .addOnFailureListener { e->

                Toast.makeText(context, e.message, Toast.LENGTH_LONG ).show()

            }

            .addOnSuccessListener {  }

    }

    fun sendRequestToDriver(context: Context, mainLayout: RelativeLayout?, foundDriver: DriverGeoModel?, selectedPlaceEvent: SelectedPlaceEvent) {

        val compositeDisposable = CompositeDisposable()

        val ifcmService = RetroFitFCMClient.instance!!.create(IFCMService::class.java)

        //Get Token

        FirebaseDatabase.getInstance()
            .getReference(Common.TOKEN_REFERENCE)
            .child(foundDriver!!.key!!)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()){

                        val tokenModel = snapshot.getValue(TokenModel::class.java)

                        val notificationData : MutableMap<String, String> = HashMap()

                        notificationData.put(Common.NOTI_TITLE, Common.REQUEST_DRIVER_TITLE)
                        notificationData.put(Common.NOTI_BODY, "This Message is for the Requested Driver Action")
                        notificationData.put(Common.RIDER_KEY, FirebaseAuth.getInstance().currentUser!!.uid)
                        notificationData.put(Common.PICKUP_LOCATION_STRING, selectedPlaceEvent.originAddress)
                        notificationData.put(Common.PICKUP_LOCATION, StringBuilder()
                            .append(selectedPlaceEvent.origin.latitude)
                            .append(",")
                            .append(selectedPlaceEvent.origin.longitude)
                            .toString())

                        notificationData.put(Common.DESTINATION_LOCATION_STRING, selectedPlaceEvent.destinationAddress)

                        notificationData.put(Common.DESTINATION_LOCATION, StringBuilder()
                                .append(selectedPlaceEvent.destination.latitude)
                                .append(",")
                                .append(selectedPlaceEvent.destination.longitude)
                                .toString())

                        Log.d("UserUtils", "SnapShot Exists" + selectedPlaceEvent.destination.latitude)


                        notificationData[Common.USER_DISTANCE_TEXT] = selectedPlaceEvent.distanceText!!
                        notificationData[Common.USER_DISTANCE_VALUE] = selectedPlaceEvent.distanceValue.toString()
                        notificationData[Common.USER_DURATION_TEXT] = selectedPlaceEvent.durationText!!
                        notificationData[Common.USER_DURATION_VALUE] = selectedPlaceEvent.durationValue.toString()
                        notificationData[Common.USER_TOTAL] = selectedPlaceEvent.totalFee.toString()

                        val fcmSendData = FCMSendData(tokenModel!!.token, notificationData)

                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)!!
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ fcmResponse ->

                                if(fcmResponse!!.success ==0){

                                    compositeDisposable.clear()

                                    Toast.makeText(context, context.getString(R.string.send_request_driver_failed), Toast.LENGTH_LONG ).show()

                                }


                            },{t: Throwable? ->

                                compositeDisposable.clear()

                                Toast.makeText(context, t!!.message, Toast.LENGTH_LONG ).show()



                            }))

                    }

                    else{

                        Toast.makeText(context, context.getString(R.string.token_not_found), Toast.LENGTH_LONG ).show()

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                  //  Toast.makeText(mainLayout!!, error.message, Toast.LENGTH_SHORT).show()
                    Toast.makeText(context, error.message, Toast.LENGTH_LONG ).show()

                }


            })

    }
}