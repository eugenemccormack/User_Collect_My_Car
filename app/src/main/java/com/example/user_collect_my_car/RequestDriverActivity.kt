package com.example.user_collect_my_car

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.user_collect_my_car.Common.Common
import com.example.user_collect_my_car.Model.DriverGeoModel
import com.example.user_collect_my_car.Model.EventBus.*
import com.example.user_collect_my_car.Model.TripPlanModel
import com.example.user_collect_my_car.Remote.IGoogleAPI
import com.example.user_collect_my_car.Remote.RetroFitClient
import com.example.user_collect_my_car.Utils.UserUtils

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.ui.IconGenerator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_request_driver.*
import kotlinx.android.synthetic.main.activity_user_history.*
import kotlinx.android.synthetic.main.layout_confirm_driver.*
import kotlinx.android.synthetic.main.layout_confirm_pickup.*
import kotlinx.android.synthetic.main.layout_driver_info.*
import kotlinx.android.synthetic.main.origin_info_windows.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import kotlin.random.Random

class RequestDriverActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var mMap: GoogleMap


    private var selectedPlaceEvent: SelectedPlaceEvent?= null

    //Routes

    private val compositeDisposable = CompositeDisposable()

    private lateinit var iGoogleAPI: IGoogleAPI

    private lateinit var txt_origin: TextView

    private var blackPolyline: Polyline ?= null
    private var greyPolyline: Polyline ?= null
    private var polylineOptions: PolylineOptions ?= null
    private var blackPolylineOptions: PolylineOptions ?= null
    private var polylineList: ArrayList<LatLng?>?= null
    private var originMarker: Marker ?= null
    private var destinationMarker: Marker ?= null

    private var lastDriverCall: DriverGeoModel ?= null

    private var driversOldPosition: String = ""
    private var handler: Handler?= null
    private var v = 0f
    private var lat = 0.0
    private var lng = 0.0
    private var index = 0
    private var next = 0
    private var start: LatLng ?= null
    private var end: LatLng ?= null
    private var driverPhoneCall: String = ""





    override fun onStart() {

        super.onStart()

        if(!EventBus.getDefault().isRegistered(this))

            EventBus.getDefault().register(this)


    }

    override fun onStop() {

        compositeDisposable.clear()

        if(!EventBus.getDefault().hasSubscriberForEvent(SelectedPlaceEvent::class.java))

            EventBus.getDefault().removeStickyEvent(SelectedPlaceEvent::class.java)

        if(!EventBus.getDefault().hasSubscriberForEvent(DeclineRequestFromDriver::class.java))

            EventBus.getDefault().removeStickyEvent(DeclineRequestFromDriver::class.java)

        if(!EventBus.getDefault().hasSubscriberForEvent(DriverAcceptTripEvent::class.java))

            EventBus.getDefault().removeStickyEvent(DriverAcceptTripEvent::class.java)

        if(!EventBus.getDefault().hasSubscriberForEvent(DeclineRequestAndRemoveTripFromDriver::class.java))

            EventBus.getDefault().removeStickyEvent(DeclineRequestAndRemoveTripFromDriver::class.java)

        if(!EventBus.getDefault().hasSubscriberForEvent(DriverCompleteJourneyEvent::class.java))

            EventBus.getDefault().removeStickyEvent(DriverCompleteJourneyEvent::class.java)

        EventBus.getDefault().unregister(this)

        super.onStop()
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onDriverCompleteJourneyEvent(event: DriverCompleteJourneyEvent){

        Common.showNotification(this, Random.nextInt(),
                "Thank You!",
                "Your Drop-off " + event.tripId + " has been Complete",
                null)

        finish()

    }


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onDriverAcceptTripEvent(event: DriverAcceptTripEvent){

        FirebaseDatabase.getInstance().getReference(Common.TRIP)
                .child(event.tripId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if(snapshot.exists()){

                            val tripPlanModel = snapshot.getValue(TripPlanModel::class.java)

                            mMap.clear()

                            /*val cameraPos = CameraPosition.Builder().target(mMap.cameraPosition.target)
                                    .tilt(0f).zoom(mMap.cameraPosition.zoom).build()
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos))*/

                            //Get Routes

                            val driverLocation = StringBuilder()
                                    .append(tripPlanModel!!.currentLat)
                                    .append(",")
                                    .append(tripPlanModel!!.currentLng)
                                    .toString()

                            compositeDisposable.add(
                                    iGoogleAPI.getDirections("driving", "less_driving",
                                            tripPlanModel!!.origin, driverLocation, getString(R.string.google_api_key))!!
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe{ returnResult ->

                                                var blackPolylineOptions: PolylineOptions ?= null
                                                var polylineList:List<LatLng?> ?= null
                                                var blackPolyline : Polyline ?= null

                                                try {

                                                    val jsonObject = JSONObject(returnResult)

                                                    val jsonArray = jsonObject.getJSONArray("routes")

                                                    for (i in 0 until jsonArray.length()) {

                                                        val route = jsonArray.getJSONObject(i)

                                                        val poly = route.getJSONObject("overview_polyline")

                                                        val polyline = poly.getString("points")

                                                        polylineList = Common.decodePoly(polyline)

                                                    }

                                                    blackPolylineOptions = PolylineOptions()
                                                    blackPolylineOptions!!.color(R.color.blue) //Color.WHITE
                                                    blackPolylineOptions!!.width(12f)
                                                    blackPolylineOptions!!.startCap(SquareCap())
                                                    blackPolylineOptions!!.jointType(JointType.ROUND)
                                                    blackPolylineOptions!!.addAll(polylineList)
                                                    blackPolyline = mMap.addPolyline(blackPolylineOptions)

                                                    //Add Car Icon for Origin

                                                    val objects = jsonArray.getJSONObject(0)

                                                    val legs = objects.getJSONArray("legs")

                                                    val legsObject = legs.getJSONObject(0)

                                                    val time = legsObject.getJSONObject("duration")

                                                    val duration = time.getString("text")

                                                    val origin =  LatLng(

                                                            tripPlanModel!!.origin!!.split(",").get(0).toDouble(),
                                                            tripPlanModel!!.origin!!.split(",").get(1).toDouble())

                                                    val destination = LatLng(tripPlanModel.currentLat, tripPlanModel.currentLng)

                                                    val latLngBound = LatLngBounds.Builder().include(origin)
                                                            .include(destination)
                                                            .build()

                                                    addPickupMarkerWithDuration(duration, origin)

                                                    addDriverMarker(destination)

                                                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBound, 160))
                                                    mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.cameraPosition!!.zoom-1))

                                                    initDriverMoving(event.tripId, tripPlanModel)

                                                    //Load Driver Avatar

                                                    Glide.with(this@RequestDriverActivity)
                                                            .load(tripPlanModel!!.driverInfoModel!!.image)
                                                            .into(driver_image)

                                                    txt_driver_name.setText(tripPlanModel!!.driverInfoModel!!.name)
                                                    txt_car_number.setText(tripPlanModel!!.driverInfoModel!!.licenceNumber)

                                                    driverPhoneCall = tripPlanModel!!.driverInfoModel!!.phone.toString()

                                                    confirm_driver_layout.visibility = View.GONE
                                                    confirm_pickup_layout.visibility = View.GONE

                                                    driver_info_layout.visibility = View.VISIBLE




                                                } catch (e: java.lang.Exception) {

                                                    Toast.makeText(this@RequestDriverActivity, e.message, Toast.LENGTH_SHORT).show()

                                                }



                                            }
                            )

                        }

                        else{

                            Toast.makeText(this@RequestDriverActivity, getString(R.string.trip_not_found) + " " + event.tripId, Toast.LENGTH_SHORT).show()

                        }

                    }

                    override fun onCancelled(error: DatabaseError) {

                        Toast.makeText(this@RequestDriverActivity, error.message, Toast.LENGTH_SHORT).show()

                    }


                })

    }



    private fun initDriverMoving(tripId: String, tripPlanModel: TripPlanModel) {

        driversOldPosition = StringBuilder().append(tripPlanModel.currentLat)
                .append(",").append(tripPlanModel.currentLng).toString()

        FirebaseDatabase.getInstance().getReference(Common.TRIP)
                .child(tripId)
                .addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val newData = snapshot.getValue(TripPlanModel::class.java)

                        if(newData != null) {

                            val driverNewPosition = StringBuilder().append(newData!!.currentLat)
                                    .append(",")
                                    .append(newData!!.currentLng).toString()

                            if (!driversOldPosition.equals(driverNewPosition))

                                moveMarkerAnimation(destinationMarker!!, driversOldPosition, driverNewPosition)
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {

                        Toast.makeText(this@RequestDriverActivity, error.message, Toast.LENGTH_SHORT).show()

                    }


                })

    }

    private fun moveMarkerAnimation(marker: Marker, from: String, to: String) {

        compositeDisposable.add(
                iGoogleAPI.getDirections("driving", "less_driving",
                        from, to , getString(R.string.google_api_key))!!
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe{ returnResult ->

                            try {

                                val jsonObject = JSONObject(returnResult)

                                val jsonArray = jsonObject.getJSONArray("routes")

                                for (i in 0 until jsonArray.length()) {

                                    val route = jsonArray.getJSONObject(i)

                                    val poly = route.getJSONObject("overview_polyline")

                                    val polyline = poly.getString("points")

                                    polylineList = Common.decodePoly(polyline)

                                }

                                blackPolylineOptions!!.color(R.color.blue) //Color.WHITE
                                blackPolylineOptions!!.width(12f)
                                blackPolylineOptions!!.startCap(SquareCap())
                                blackPolylineOptions!!.jointType(JointType.ROUND)
                                blackPolylineOptions!!.addAll(polylineList)
                                blackPolyline = mMap.addPolyline(blackPolylineOptions)

                                //Add Car Icon for Origin

                                val objects = jsonArray.getJSONObject(0)

                                val legs = objects.getJSONArray("legs")

                                val legsObject = legs.getJSONObject(0)

                                val time = legsObject.getJSONObject("duration")

                                val duration = time.getString("text")

                                val bitmap = Common.createIconWithDuration(this@RequestDriverActivity, duration)

                                originMarker!!.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))

                                //Moving Driver

                                val runnable = object:Runnable{

                                    override fun run(){

                                        if(index < polylineList!!.size - 2){

                                            index++
                                            next = index + 1
                                            start = polylineList!![index]
                                            end = polylineList!![next]

                                        }

                                        val valueAnimator = ValueAnimator.ofInt(0,1)
                                        valueAnimator.duration = 1500
                                        valueAnimator.interpolator = LinearInterpolator()
                                        valueAnimator.addUpdateListener { valueAnimatorNew ->

                                            v = valueAnimatorNew.animatedFraction
                                            lat = v*end!!.latitude + (1-v)*start!!.latitude
                                            lng = v*end!!.longitude + (1-v)*end!!.longitude

                                            val newPos = LatLng(lat, lng)
                                            marker.position = newPos
                                            marker.setAnchor(0.5f, 0.5f)
                                            marker.rotation = Common.getBearing(start!!, newPos)
                                            mMap.moveCamera(CameraUpdateFactory.newLatLng(newPos))

                                        }

                                        valueAnimator.start()

                                        if(index < polylineList!!.size - 2)

                                            handler!!.postDelayed(this, 1500)



                                    }

                                }

                                handler = Handler(Looper.getMainLooper())//Handler()
                                index = -1
                                next = 1
                                handler!!.postDelayed(runnable, 1500)
                                driversOldPosition = to // Set New Driver Position


                            } catch (e: java.lang.Exception) {

                                Toast.makeText(this@RequestDriverActivity, e.message, Toast.LENGTH_SHORT).show()

                            }

                        }
        )

    }

    private fun addDriverMarker(destination: LatLng) {

        destinationMarker = mMap.addMarker(MarkerOptions().position(destination).flat(true)
                .icon(bitmapDescriptorFromVector(R.drawable.ic_red_car)))

    }

    private fun Context.bitmapDescriptorFromVector(vectorResId:Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        vectorDrawable.draw(Canvas(bitmap))
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun addPickupMarkerWithDuration(duration: String, origin: LatLng) {

        val icon = Common.createIconWithDuration(this@RequestDriverActivity, duration)!!

        originMarker = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(origin))

    }


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onDeclineReceived(event: DeclineRequestFromDriver){

        if(lastDriverCall != null){

            Common.driversFound.get(lastDriverCall!!.key)!!.isDecline = true

            findDriver(selectedPlaceEvent!!)

        }

    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onDeclineAndRemoveTripReceived(event: DeclineRequestAndRemoveTripFromDriver){

        if(lastDriverCall != null){

            if(Common.driversFound.get(lastDriverCall!!.key) != null)

                Common.driversFound.get(lastDriverCall!!.key)!!.isDecline = true

            //Finish the Activity
            finish()

        }

    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onSelectPlaceEvent(event:SelectedPlaceEvent){

        selectedPlaceEvent = event

    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_driver)

        init()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun init() {

        iGoogleAPI = RetroFitClient.instance!!.create(IGoogleAPI::class.java)

        //Event

        confirm_driver_button.setOnClickListener {

            confirm_pickup_layout.visibility = View.VISIBLE

            confirm_driver_layout.visibility = View.GONE

            setDataPickUp()

        }

        confirm_pickup_button.setOnClickListener {

            if(mMap == null)

                return@setOnClickListener

            if(selectedPlaceEvent == null)

                return@setOnClickListener

            //Clear Map

            mMap.clear()

            //Tilt

            val cameraPosition = CameraPosition.Builder().target(selectedPlaceEvent!!.origin)
                    .tilt(45f)
                    .zoom(16f)
                    .build()

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            addMarkerWithPulse()

        }

        call_driver.setOnClickListener {

          /*  //val smsManager = SmsManager.getDefault() as SmsManager
            smsManager.sendTextMessage("driverPhoneCall", null, "sms message", null, null)*/


 /*           val uri = Uri.parse(driverPhoneCall)
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            intent.putExtra("sms_body", "Here goes your message...")
            startActivity(it)*/
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:" + driverPhoneCall)
            startActivity(dialIntent)

        }

/*        if (ActivityCompat.checkSelfPermission(this@RequestDriverActivity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@RequestDriverActivity, Manifest.permission.SEND_RESPOND_VIA_MESSAGE) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this@RequestDriverActivity, getString(R.string.permission_require), Toast.LENGTH_SHORT).show()

            return
        }*/



    }

    private fun addMarkerWithPulse(){

        confirm_pickup_layout.visibility = View.GONE

        originMarker = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker())
                .position(selectedPlaceEvent!!.origin))

        //addPulsatingEffect(selectedPlaceEvent!!) // Stop Duplicate Trip Database Entry



    }

    private fun setDataPickUp() {

        txt_pickup_location.text = if (txt_origin != null)  txt_origin.text  else "None"

        mMap.clear()

        addPickUpMarker()

    }

    private fun addPickUpMarker() {

        val view = layoutInflater.inflate(R.layout.pickup_info_windows, null)

        val generator = IconGenerator(this)

        generator.setContentView(view)

        generator.setBackground(ColorDrawable(Color.TRANSPARENT))

        val icon = generator.makeIcon()

        originMarker = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectedPlaceEvent!!.origin))

        addPulsatingEffect(selectedPlaceEvent!!)

    }

    private fun addPulsatingEffect(selectedPlaceEvent: SelectedPlaceEvent) {

        startMapCameraSpinningAnimation(selectedPlaceEvent)

    }

    private fun startMapCameraSpinningAnimation(selectedPlaceEvent: SelectedPlaceEvent) {

        findDriver(selectedPlaceEvent)

    }

    private fun findDriver(selectedPlaceEvent: SelectedPlaceEvent) {

        //var foundDriver: DriverGeoModel ?= null//String? = null

        if(Common.selectedDriverKey != null) {

            //if(Common.driversFound.size > 0) {

            var foundDriver: DriverGeoModel ?= null

            var chosenDriver = Common.selectedDriverKey

            //foundDriver = Common.selectedDriverKey

            Toast.makeText(this@RequestDriverActivity, Common.selectedDriverKey.toString(), Toast.LENGTH_SHORT).show()

            Toast.makeText(this@RequestDriverActivity, Common.selectedDriver!!.name.toString(), Toast.LENGTH_SHORT).show()

            for(key in Common.driversFound.keys){

                var foundDriverKey = Common.driversFound[key]!!.key

                Log.d("RequestDriverActivity", "Inside For Loop")
                Log.d("RequestDriverActivity", "$chosenDriver")
                Log.d("RequestDriverActivity", "$foundDriver")

                if(chosenDriver!! == foundDriverKey){

                    if(!Common.driversFound[key]!!.isDecline) {

                        foundDriver = Common.driversFound[key]

                        Log.d("RequestDriverActivity", "foundDriverKey" + foundDriver.toString())

                        break
                    }

                    else{

                        Log.d("RequestDriverActivity", "Continue")
                        continue //If Already Decline Before, Skip and Continue
                    }

                }
            }





            if (foundDriver != null) {

                Log.d("RequestDriverActivity", "sendRequestToDriver")

                UserUtils.sendRequestToDriver(this@RequestDriverActivity,
                        main_layout,
                        foundDriver,
                        selectedPlaceEvent!!)



                lastDriverCall = foundDriver

            } else {

                Toast.makeText(this@RequestDriverActivity, getString(R.string.no_driver_accept), Toast.LENGTH_SHORT).show()

                lastDriverCall = null

                finish()

            }
        }
        else{

            //Toast.makeText(this@RequestDriverActivity, getString(R.string.drivers_not_found), Toast.LENGTH_SHORT).show()
            Toast.makeText(this@RequestDriverActivity, getString(R.string.no_driver_selected), Toast.LENGTH_SHORT).show()

            lastDriverCall = null

            finish()
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap



        drawPath(selectedPlaceEvent!!)



    }

    private fun drawPath(selectedPlaceEvent: SelectedPlaceEvent) {

        //Request API
        //getString(R.string.google_api_key)

        Log.d("RequestDriverActivity", "Request API")

        compositeDisposable.add(iGoogleAPI.getDirections(
                "driving", "less_driving", selectedPlaceEvent.originString, selectedPlaceEvent.destinationString,getString(R.string.google_api_key))
        !!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { returnResult ->

                    Log.d("API_RETURN", returnResult)

                    try {

                        val jsonObject = JSONObject(returnResult)

                        val jsonArray = jsonObject.getJSONArray("routes")

                        for (i in 0 until jsonArray.length()) {

                            val route = jsonArray.getJSONObject(i)

                            val poly = route.getJSONObject("overview_polyline")

                            val polyline = poly.getString("points")

                            polylineList = Common.decodePoly(polyline)

                        }

                        polylineOptions = PolylineOptions()
                        polylineOptions!!.color(R.color.blue)
                        polylineOptions!!.width(12f)
                        polylineOptions!!.startCap(SquareCap())
                        polylineOptions!!.jointType(JointType.ROUND)
                        polylineOptions!!.addAll(polylineList)
                        greyPolyline = mMap.addPolyline(polylineOptions)

                        blackPolylineOptions = PolylineOptions()
                        blackPolylineOptions!!.color(R.color.blue)
                        blackPolylineOptions!!.width(12f)
                        blackPolylineOptions!!.startCap(SquareCap())
                        blackPolylineOptions!!.jointType(JointType.ROUND)
                        blackPolylineOptions!!.addAll(polylineList)
                        blackPolyline = mMap.addPolyline(blackPolylineOptions)

                        //Animator

                        val valueAnimator = ValueAnimator.ofInt(0,100)

                        valueAnimator.duration = 1100
                        valueAnimator.repeatCount = ValueAnimator.INFINITE
                        valueAnimator.interpolator = LinearInterpolator()
                        valueAnimator.addUpdateListener { value ->

                            val points = greyPolyline!!.points

                            val percentValue = value.animatedValue.toString().toInt()

                            val size = points.size

                            val newPoints = (size * ((percentValue/100.0f)).toInt())

                            val p = points.subList(0, newPoints)

                            blackPolyline!!.points = (p)

                        }

                        valueAnimator.start()

                        val latLngBound = LatLngBounds.Builder().include(selectedPlaceEvent.origin)
                                .include(selectedPlaceEvent.destination)
                                .build()

                        //Add Car Icon for Origin

                        val objects = jsonArray.getJSONObject(0)

                        val legs = objects.getJSONArray("legs")

                        val legsObject = legs.getJSONObject(0)

                        val time = legsObject.getJSONObject("duration")

                        val duration = time.getString("text")
                        val durationValue = time.getInt("value")

                        val distance = legsObject.getJSONObject("distance")
                        val distanceText = distance.getString("text")
                        val distanceValue = distance.getInt("value")

                        val startAddress = legsObject.getString("start_address")

                        val endAddress = legsObject.getString("end_address")

                        val startLocation = legsObject.getJSONObject("start_location")

                        val endLocation = legsObject.getJSONObject("end_location")

                        distance_confirm_driver.text = (distanceText)


                        selectedPlaceEvent.originAddress = startAddress
                        selectedPlaceEvent.origin = LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng"))
                        selectedPlaceEvent.destinationAddress = endAddress
                        selectedPlaceEvent.destination = LatLng(endLocation.getDouble("lat"), endLocation.getDouble("lng"))
                        selectedPlaceEvent.durationValue = durationValue
                        selectedPlaceEvent.distanceValue = distanceValue
                        selectedPlaceEvent.durationText = duration
                        selectedPlaceEvent.distanceText = distanceText

                        val totalPrice = Common.calculateTotalPrice(distanceValue)

                        selectedPlaceEvent.totalFee = (totalPrice)

                        val total: Double = totalPrice

                        val round = Math.round(total * 100.0) / 100.0

                        amount_confirm_driver.text = "€"+round.toString()

                        //amount_confirm_driver.text = StringBuilder("€").append(totalPrice)



                        addOriginMarker(duration, startAddress)

                        addDestinationMarker(endAddress)

                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBound, 160))
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.cameraPosition!!.zoom-1))


                    } catch (e: java.lang.Exception) {

                        Toast.makeText(this@RequestDriverActivity, e.message, Toast.LENGTH_SHORT).show()

                    }

                })



    }

    private fun addDestinationMarker(endAddress: String) {

        val view = layoutInflater.inflate(R.layout.destination_info_windows, null)

        val txt_destination = view.findViewById<View>(R.id.txt_destination) as TextView

        txt_destination.text = Common.formatAddress(endAddress)

        val generator = IconGenerator(this)

        generator.setContentView(view)

        generator.setBackground(ColorDrawable(Color.TRANSPARENT))

        val icon = generator.makeIcon()

        destinationMarker = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectedPlaceEvent!!.destination))

    }

    private fun addOriginMarker(duration: String, startAddress: String) {

        val view = layoutInflater.inflate(R.layout.origin_info_windows, null)

        val txt_time = view.findViewById<View>(R.id.txt_time) as TextView

        txt_origin = view.findViewById<View>(R.id.txt_origin) as TextView

        txt_time.text = Common.formatDuration(duration)
        txt_origin.text = Common.formatAddress(startAddress)

        val generator = IconGenerator(this)

        generator.setContentView(view)

        generator.setBackground(ColorDrawable(Color.TRANSPARENT))

        val icon = generator.makeIcon()

        originMarker = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectedPlaceEvent!!.origin))

    }
}