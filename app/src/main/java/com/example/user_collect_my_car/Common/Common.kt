package com.example.user_collect_my_car.Common

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.user_collect_my_car.Model.*
import com.example.user_collect_my_car.R
import com.example.user_collect_my_car.RequestDriverActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ui.IconGenerator


object Common {

    val BASE_PRICE: Double = 2.00
    val USER_TOTAL: String = "TotalUser"
    val USER_DURATION_VALUE: String = "DurationUserValue"
    val USER_DURATION_TEXT: String = "DurationUser"
    val USER_DISTANCE_VALUE: String = "DistanceUserValue"
    val USER_DISTANCE_TEXT: String = "DistanceUser"
    val USER_REQUEST_COMPLETE_TRIP: String = "RequestCompleteTripToUser"
    val REQUEST_DRIVER_DECLINE_AND_REMOVE_TRIP: String = "DeclineAndRemoveTrip"
    val DESTINATION_LOCATION: String = "DestinationLocation"
    val DESTINATION_LOCATION_STRING: String = "DestinationLocationString"
    val PICKUP_LOCATION_STRING: String = "PickupLocationString"
    val REQUEST_DRIVER_DECLINE: String ?= "Decline"
    val RIDER_KEY: String = "RiderKey"
    val PICKUP_LOCATION: String = "PickupLocation"
    val REQUEST_DRIVER_TITLE: String = "RequestDriver"
    val NOTI_BODY: String = "body"
    val NOTI_TITLE: String = "title"

/*    fun buildNavMessage(): String {

        return StringBuilder("Welcome ")
                .append(currentUser!!.name)
                .toString()

    }*/


    val TOKEN_REFERENCE: String = "Token"
    val driversSubscribe: MutableMap<String, AnimationModel> = HashMap<String, AnimationModel>()


    val TRIP: String = "Trips"

    val TRIP_KEY: String = "TripKey"

    val REQUEST_DRIVER_ACCEPT: String = "Accept"

    val markerList: MutableMap<String, Marker> = HashMap<String, Marker>()

    val DRIVER_INFO_REFERENCE: String = "DriverInfo"

    val driversFound: MutableMap<String, DriverGeoModel> = HashMap<String, DriverGeoModel>()

    val DRIVERS_LOCATION_REFERENCE: String = "DriversLocation" //Same a Driver App

    var currentUser: UserModel?=null

    var collectionInfo: TripPlanModel?=null

   var selectedDriver: DriverInfoModel?=null

    var selectedDriverKey: String ?= null

    val USER_INFO_REFERENCE: String = "UserInfo"

    fun showNotification(context: Context, id: Int, title: String?, body: String?, intent: Intent?) {

        var pendingIntent : PendingIntent? = null

        if(intent != null)

            pendingIntent = PendingIntent.getActivity(context, id, intent!!, PendingIntent.FLAG_UPDATE_CURRENT)

            val NOTIFICATION_CHANNEL_ID = "collectmycar-c2834"

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

                val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Collect My Car", NotificationManager.IMPORTANCE_HIGH)

                notificationChannel.description = "Collect My Car"
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.RED
                notificationChannel.vibrationPattern = longArrayOf(0,1000,500,1000)
                notificationChannel.enableVibration(true)

                notificationManager.createNotificationChannel(notificationChannel)

            }

            val builder = NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID)
            builder.setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(false)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSmallIcon(R.drawable.ic_baseline_directions_car_24)
                    .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_baseline_directions_car_24))

            if(pendingIntent != null)

                builder.setContentIntent(pendingIntent!!)

                val notification = builder.build()

                notificationManager.notify(id, notification)





    }



    fun buildName(name: String?): String? { //, phone: String?): String? {

        return StringBuilder(name!!).toString()//.append("").append(phone).toString()

    }

    //Decode Poly
    fun decodePoly(encoded: String): ArrayList<LatLng?> {
        val poly = ArrayList<LatLng?>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(
                    lat.toDouble() / 1E5,
                    lng.toDouble() / 1E5)
            poly.add(p)
        }
        return poly
    }

    //Get Bearings

    fun getBearing(begin: LatLng, end: LatLng): Float {

        val lat = Math.abs(begin.latitude - end.latitude)

        val lng = Math.abs(begin.longitude - end.longitude)

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)

            return Math.toDegrees(Math.atan(lng / lat)).toFloat()

        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)

            return (90 - Math.toDegrees(Math.atan(lng / lat)) + 90).toFloat()

        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)

            return (Math.toDegrees(Math.atan(lng / lat)) + 180).toFloat()

        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)

            return (90 - Math.toDegrees(Math.atan(lng / lat)) + 270).toFloat()


        return (-1).toFloat()
    }

    fun formatDuration(duration: String): CharSequence? {

        if(duration.contains("mins"))

            return duration.substring(0, duration.length-1) //Removes the Letter "s"

        else

            return duration



    }

    fun formatAddress(startAddress: String): CharSequence? {

        val firstIndexComma = startAddress.indexOf(",")

        return startAddress.substring(0, firstIndexComma)



    }

    fun createIconWithDuration(context: Context, duration: String): Bitmap? {

        val view = LayoutInflater.from(context).inflate(R.layout.pickup_info_with_duration_window, null)

        val txt_time = view.findViewById<View>(R.id.txt_duration) as TextView
        txt_time.setText(getNumberFromText(duration!!))

        val generator = IconGenerator(context)
        generator.setContentView(view)
        generator.setBackground(ColorDrawable(Color.TRANSPARENT))

        return generator.makeIcon()

    }

    private fun getNumberFromText(s: String): String {

        return s.substring(0, s.indexOf(" "))

    }

    fun calculateTotalPrice(meters: Int): Double {

        return if(meters <= 1000)

            BASE_PRICE

        else

            (BASE_PRICE/1000)*meters

    }


}