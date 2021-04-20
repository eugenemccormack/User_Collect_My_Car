package com.example.user_collect_my_car.Services

import com.example.user_collect_my_car.Common.Common
import com.example.user_collect_my_car.Model.EventBus.DeclineRequestAndRemoveTripFromDriver
import com.example.user_collect_my_car.Model.EventBus.DeclineRequestFromDriver
import com.example.user_collect_my_car.Model.EventBus.DriverAcceptTripEvent
import com.example.user_collect_my_car.Model.EventBus.DriverCompleteJourneyEvent
import com.example.user_collect_my_car.Utils.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.random.Random

class MyFirebaseMessagingService: FirebaseMessagingService() {


    override fun onNewToken(token: String) {
        super.onNewToken(token)

        if(FirebaseAuth.getInstance().currentUser != null){

            UserUtils.updateToken(this,token)

        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data

        if(data != null){

            if(data[Common.NOTI_TITLE] != null) {

                if(data[Common.NOTI_TITLE].equals(Common.REQUEST_DRIVER_DECLINE)){

                    EventBus.getDefault().postSticky(DeclineRequestFromDriver())

                }
                else if(data[Common.NOTI_TITLE].equals(Common.REQUEST_DRIVER_ACCEPT)){

                    EventBus.getDefault().postSticky(DriverAcceptTripEvent(data[Common.TRIP_KEY]!!))

                }
                else if(data[Common.NOTI_TITLE].equals(Common.REQUEST_DRIVER_DECLINE_AND_REMOVE_TRIP)){

                    EventBus.getDefault().postSticky(DeclineRequestAndRemoveTripFromDriver())

                }

                else if(data[Common.NOTI_TITLE].equals(Common.USER_REQUEST_COMPLETE_TRIP)){

                    val tripKey = data[Common.TRIP_KEY]

                    EventBus.getDefault().postSticky(DriverCompleteJourneyEvent(tripKey!!))

                }
                else {

                    Common.showNotification(this, Random.nextInt(),
                            data[Common.NOTI_TITLE],
                            data[Common.NOTI_BODY],
                            null)

                }

            }

        }
    }

}