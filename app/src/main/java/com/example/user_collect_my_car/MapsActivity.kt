package com.example.user_collect_my_car

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.user_collect_my_car.Callback.FirebaseDriverInfoListener
import com.example.user_collect_my_car.Callback.FirebaseFailedListener
import com.example.user_collect_my_car.Common.Common
import com.example.user_collect_my_car.Model.*
import com.example.user_collect_my_car.Model.EventBus.SelectedPlaceEvent
import com.example.user_collect_my_car.Remote.IGoogleAPI
import com.example.user_collect_my_car.Remote.RetroFitClient
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import io.reactivex.Observable
import io.reactivex.Observable.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_navigation_drawer_user.*
import kotlinx.android.synthetic.main.layout_bottom_sheet.*
import kotlinx.android.synthetic.main.layout_driver_info.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.IOException
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, FirebaseDriverInfoListener, GoogleMap.OnMarkerClickListener {

    private lateinit var database : FirebaseDatabase
    private lateinit var driverInfoRef : DatabaseReference
    private lateinit var firebaseAuth : FirebaseAuth

    private var isNextLaunch: Boolean = false
    lateinit var toggle: ActionBarDrawerToggle

    private lateinit var image_avatar : ImageView

    private lateinit var mMap: GoogleMap

    private lateinit var slidingUpPanelLayout: SlidingUpPanelLayout
    private lateinit var welcomeMessage: TextView
    private lateinit var autocompleteSupportFragment: AutocompleteSupportFragment

    //Location

    private var locationRequest: LocationRequest ?= null
    private var locationCallback: LocationCallback ?= null
    private var fusedLocationProviderClient: FusedLocationProviderClient ?= null

    //Load Drivers

    var distance = 1.0
    val LIMIT_RANGE = 10.0
    var previousLocation: Location? = null
    var currentLocation:Location? = null

    var new = true

    //Listener

    lateinit var iFirebaseDriverInfoListener: FirebaseDriverInfoListener
    lateinit var iFirebaseFailedListener: FirebaseFailedListener

    //Find by City Name

    var cityName = ""

    var driverKeySelect = ""

    var driverKey = ""

    private lateinit var driver_profileImage2: ImageView
    private lateinit var driver_Name2: TextView
    private lateinit var driver_Email2: TextView
    private lateinit var driver_Phone2: TextView

    companion object{

        val MESSGAE = "Message"
    }

    val compositeDisposable = CompositeDisposable()
    lateinit var iGoogleAPI: IGoogleAPI



    override fun onStop() {

        compositeDisposable.clear()

        super.onStop()
    }

    override fun onResume() {

        super.onResume()

        if(isNextLaunch)

            loadAvailableDrivers()

        else

            isNextLaunch = true

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        toggle = ActionBarDrawerToggle(this, drawerLayoutMaps, R.string.open, R.string.closed)

        drawerLayoutMaps.addDrawerListener(toggle)

        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navViewMaps.setNavigationItemSelectedListener {

            when(it.itemId){

                R.id.mItem1 -> {

                    intent = Intent(this, MapsActivity::class.java)

                    //   finish()

                    //  overridePendingTransition(0, 0)

                    startActivity(intent)

                    // overridePendingTransition(0, 0)

                    Toast.makeText(applicationContext, "Maps", Toast.LENGTH_SHORT).show()

                    /*finish();
                    overridePendingTransition(0, 0);
                    startActivity(intent);
                    overridePendingTransition(0, 0);*/
                }

                R.id.mItem2 -> {

                    intent = Intent(this, History::class.java)

                    startActivity(intent)

                    Toast.makeText(applicationContext, "History", Toast.LENGTH_SHORT)
                            .show()
                }

                R.id.mItem3 -> {

                    val builder = AlertDialog.Builder(this)

                    builder.setTitle("Logout")
                            .setMessage("Do you really want to Logout?")
                            .setNegativeButton(
                                    "Cancel",
                                    { dialogInterface, _ -> dialogInterface.dismiss() })

                            .setPositiveButton("Logout") { dialogInterface, _ ->

                                FirebaseAuth.getInstance().signOut()

                                val intent = Intent(this, LoginUser::class.java)
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                finish()

                            }.setCancelable(false)

                    val dialog = builder.create()
                    dialog.setOnShowListener {

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                                resources.getColor(
                                        R.color.red
                                )
                        )
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                                resources.getColor(
                                        R.color.blueInk
                                )
                        )

                    }

                    dialog.show()
                }

            }

            true

        }

        val headerView = navViewMaps.getHeaderView(0)
        val navName = headerView.findViewById<android.view.View>(R.id.nav_name) as TextView
        val navPhone = headerView.findViewById<android.view.View>(R.id.nav_phone) as TextView
        image_avatar = headerView.findViewById<android.view.View>(R.id.nav_imageView) as ImageView

        navName.setText("Welcome")//Common.buildNavMessage())
        navPhone.setText(Common.currentUser!!.name)

        if(Common.currentUser != null && Common.currentUser!!.image != null){

            Glide.with(this).load(Common.currentUser!!.image).into(image_avatar)

        }


        init()

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onDestroy() {

        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)

        super.onDestroy()
    }



    private fun init(){

        Places.initialize(this, getString(R.string.google_api_key))

        //placesClient = Places.createClient(this)

        autocompleteSupportFragment = supportFragmentManager.findFragmentById(R.id.auto_complete) as AutocompleteSupportFragment

        //autocompleteSupportFragment.view?.setBackgroundResource(R.drawable.button_border)
        autocompleteSupportFragment.view?.setBackgroundColor(Color.WHITE)


        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.NAME))

        autocompleteSupportFragment.setOnPlaceSelectedListener(object:PlaceSelectionListener{

            override fun onPlaceSelected(p0: Place) {

                if (ActivityCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this@MapsActivity, getString(R.string.permission_require), Toast.LENGTH_SHORT).show()

                    return
                }
                fusedLocationProviderClient!!.lastLocation.addOnSuccessListener { location->

                    val origin = LatLng(location.latitude, location.longitude)

                    val destination = LatLng(p0.latLng!!.latitude, p0.latLng!!.longitude)

                    startActivity(Intent(this@MapsActivity, RequestDriverActivity::class.java))

                    EventBus.getDefault().postSticky(SelectedPlaceEvent(origin, destination, p0!!.address!!))

                }

            }

            override fun onError(p0: Status) {

                Toast.makeText(this@MapsActivity, "Please Select A Drop Off Location", Toast.LENGTH_SHORT).show()

            }


        })

        iGoogleAPI = RetroFitClient.instance!!.create(IGoogleAPI::class.java)

        iFirebaseDriverInfoListener = this



        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {

            Toast.makeText(this@MapsActivity, getString(R.string.permission_require), Toast.LENGTH_SHORT).show()

            return
        }

        buildLocationRequest()

        buildLocationCallback()

        updateLocation()

        loadAvailableDrivers();

    }

    private fun updateLocation() {

        if(fusedLocationProviderClient == null){

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
            ) {

                Toast.makeText(this@MapsActivity, getString(R.string.permission_require), Toast.LENGTH_SHORT).show()

                return
            }
            fusedLocationProviderClient!!.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()
            )

        }

    }

    private fun buildLocationCallback() {

        if(locationCallback == null){

            locationCallback = object: LocationCallback(){

                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)

                    val newPos = LatLng(
                            locationResult!!.lastLocation.latitude,
                            locationResult!!.lastLocation.longitude
                    )
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPos, 18f))

                    //If User has Changed Location, Calculate and Load Driver Again

                    if(new)
                    {

                        previousLocation = locationResult.lastLocation
                        currentLocation = locationResult.lastLocation

                        setRestrictPlacesInCountry(locationResult!!.lastLocation)

                        new = false

                    }

                    else{


                        previousLocation = currentLocation
                        currentLocation = locationResult.lastLocation

                    }

                    if(previousLocation!!.distanceTo(currentLocation)/1000 <= LIMIT_RANGE)

                        Log.d("MapsActivityTest", "LimitRange")

                    loadAvailableDrivers();

                }
            }

        }

    }

    private fun buildLocationRequest() {

        if(locationRequest == null){

            locationRequest = LocationRequest()
            locationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            locationRequest!!.setFastestInterval(3000)
            locationRequest!!.setSmallestDisplacement(10f)
            locationRequest!!.interval = 5000

        }

    }

    private fun setRestrictPlacesInCountry(location: Location?) {

        try {

            val geoCoder = Geocoder(this@MapsActivity, Locale.getDefault())

            val addressList = geoCoder.getFromLocation(
                    location!!.latitude,
                    location!!.longitude,
                    1)

            if(addressList.size > 0){

                autocompleteSupportFragment.setCountry(addressList[0].countryCode)

            }

        }catch (e:IOException){

            e.printStackTrace()

        }
    }

    private fun loadAvailableDrivers() {

        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {


            Toast.makeText(
                    this@MapsActivity,
                    getString(R.string.permission_require),
                    Toast.LENGTH_SHORT
            ).show()

            return
        }
        fusedLocationProviderClient!!.lastLocation
                .addOnFailureListener { e ->

                    Toast.makeText(this@MapsActivity, e.message!!, Toast.LENGTH_SHORT).show()

                }
                .addOnSuccessListener { location ->

                    //Load All Driver in City

                    val geoCoder = Geocoder(this@MapsActivity, Locale.getDefault())

                    val addressList : List<Address>?

                    try {

                        addressList = geoCoder.getFromLocation(
                                location.latitude,
                                location.longitude,
                                1
                        )

                        if(addressList.size > 0)

                            cityName = addressList[0].countryName

                        //Query


                        val driver_Location_Ref = FirebaseDatabase.getInstance().getReference(
                                Common.DRIVERS_LOCATION_REFERENCE
                        ).child(cityName)

                        val gf = GeoFire(driver_Location_Ref)

                        val geoQuery = gf.queryAtLocation(
                                GeoLocation(
                                        location.latitude,
                                        location.longitude
                                ), distance
                        )

                        geoQuery.removeAllListeners()

                        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {


                            override fun onKeyEntered(key: String?, location: GeoLocation?) {

                                if (!Common.driversFound.containsKey(key)) {

                                    Common.driversFound[key!!] = DriverGeoModel(key, location)
                                }
                            }

                            override fun onKeyExited(key: String?) {

                            }

                            override fun onKeyMoved(key: String?, location: GeoLocation?) {

                            }

                            override fun onGeoQueryReady() {

                                if (distance <= LIMIT_RANGE) {

                                    distance++
                                    loadAvailableDrivers()

                                } else {

                                    distance = 0.0
                                    addDriverMarker()

                                }
                            }

                            override fun onGeoQueryError(error: DatabaseError?) {

                                Toast.makeText(
                                        this@MapsActivity,
                                        error!!.message,
                                        Toast.LENGTH_SHORT
                                ).show()

                            }
                        })

                        driver_Location_Ref.addChildEventListener(object : ChildEventListener {


                            override fun onChildAdded(
                                    p0: DataSnapshot,
                                    previousChildName: String?
                            ) {

                                //Has a New Driver

                                val geoQueryModel = p0.getValue(GeoQueryModel::class.java)

                                val geoLocation = GeoLocation(
                                        geoQueryModel!!.l!![0],
                                        geoQueryModel!!.l!![1]
                                )

                                val driverGeoModel = DriverGeoModel(p0.key, geoLocation)

                                val newDriverLocation = Location("")

                                newDriverLocation.latitude = geoLocation.latitude
                                newDriverLocation.longitude = geoLocation.longitude

                                val newDistance =
                                        location.distanceTo(newDriverLocation) / 1000 // in km

                                if (newDistance <= LIMIT_RANGE) {

                                    findDriverByKey(driverGeoModel)

                                }

                            }

                            override fun onChildChanged(
                                    p0: DataSnapshot,
                                    previousChildName: String?
                            ) {

                            }

                            override fun onChildRemoved(p0: DataSnapshot) {

                            }

                            override fun onChildMoved(
                                    p0: DataSnapshot,
                                    previousChildName: String?
                            ) {

                            }

                            override fun onCancelled(p0: DatabaseError) {

                                Toast.makeText(this@MapsActivity, p0.message, Toast.LENGTH_SHORT)
                                        .show()

                            }


                        })

                    }

                    catch (e: IOException){

                        Toast.makeText(
                                this@MapsActivity,
                                getString(R.string.permission_require),
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }
    }

    private fun addDriverMarker() {

        if(Common.driversFound.size > 0){

            Observable.fromIterable(Common.driversFound.keys)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { key: String? ->

                                findDriverByKey(Common.driversFound[key!!])

                                Log.d("MapsActivityTest", "addDriverMarker")


                            },
                            { t: Throwable? ->
                                Toast.makeText(
                                        this@MapsActivity,
                                        t!!.message,
                                        Toast.LENGTH_SHORT
                                ).show()

                                Toast.makeText(
                                        this@MapsActivity,
                                        "Drivers Found",
                                        Toast.LENGTH_SHORT
                                ).show()

                            }


                    )


        }
        else{

            /*Toast.makeText(
                    this@MapsActivity,
                    getString(R.string.drivers_not_found),
                    Toast.LENGTH_SHORT
            ).show()*/

            Log.d("MapsActivityTest", "Driver Not Found")

        }



    }

    private fun findDriverByKey(driverGeoModel: DriverGeoModel?) {

        FirebaseDatabase.getInstance().getReference(Common.DRIVER_INFO_REFERENCE).child(
                driverGeoModel!!.key!!
        )
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(p0: DataSnapshot) {

                        if (p0.hasChildren()) {

                            driverGeoModel.driverInfoModel = (p0.getValue(DriverInfoModel::class.java))

                            Common.driversFound[driverGeoModel.key!!]!!.driverInfoModel = (p0.getValue(
                                    DriverInfoModel::class.java
                            ))

                            Log.d("MapsActivityTest", "findDriverByKey")

                            iFirebaseDriverInfoListener.onDriverInfoLoadSuccess(driverGeoModel)

                        } else {

                            iFirebaseFailedListener.onFirebaseFailed(getString(R.string.key_not_found) + driverGeoModel.key)

                        }

                    }

                    override fun onCancelled(p0: DatabaseError) {

                        iFirebaseFailedListener.onFirebaseFailed(p0.message)

                    }


                })



    }

    class CustomInfoWindowForGoogleMap(context: Context) : GoogleMap.InfoWindowAdapter {

        var mContext = context
        var mWindow = (context as Activity).layoutInflater.inflate(R.layout.activity_info_window, null)

        private fun rendowWindowText(marker: Marker, view: View){

            val infoTitle = view.findViewById<TextView>(R.id.title)
            // val infoSnippet = view.findViewById<TextView>(R.id.snippet)

            infoTitle.text = marker.title
            // infoSnippet.text = marker.snippet

/*            val intent = Intent(this, ViewDriverInfo::class.java)

            intent.putExtra(MESSGAE,marker.title)

            startActivity(intent)*/



        }

        override fun getInfoContents(marker: Marker): View {
            rendowWindowText(marker, mWindow)

            return mWindow
        }

        override fun getInfoWindow(marker: Marker): View? {
            rendowWindowText(marker, mWindow)

            return mWindow
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap!!

        //Request Permission

        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {

                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                        //Enable Button First

                        if (ActivityCompat.checkSelfPermission(
                                        this@MapsActivity,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                        this@MapsActivity,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED
                        ) {

                            return
                        }

                        mMap.isMyLocationEnabled = true
                        mMap.uiSettings.isMyLocationButtonEnabled = true
                        mMap.setOnMyLocationClickListener {

                            //Toast.makeText(this@MapsActivity, "CLICKED BUTTON", Toast.LENGTH_LONG)
                            //  .show()

                            fusedLocationProviderClient!!.lastLocation
                                    .addOnFailureListener { e ->

                                        Toast.makeText(
                                                this@MapsActivity,
                                                e.message + "TEST ACCEPTED",
                                                Toast.LENGTH_LONG
                                        ).show()

                                    }.addOnSuccessListener { location ->

                                        val userLatLng = LatLng(location.latitude, location.longitude)
                                        mMap.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                        userLatLng,
                                                        18f
                                                )
                                        )

                                    }
                            true

                        }

                        //Update Location

                        buildLocationRequest()

                        buildLocationCallback()

                        updateLocation()

                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                        Toast.makeText(
                                this@MapsActivity,
                                "Permission " + p0!!.permissionName + " was Denied",
                                Toast.LENGTH_LONG
                        ).show()

                    }

                    override fun onPermissionRationaleShouldBeShown(
                            p0: com.karumi.dexter.listener.PermissionRequest?,
                            p1: PermissionToken?
                    ) {

                    }

                })
                .check()

        //Enable Zoom

        mMap.uiSettings.isZoomControlsEnabled = true

    }

    override fun onMarkerClick(marker: Marker): Boolean {

        driverKey = marker.snippet.toString()

        Log.d("ViewDriverInfo", driverKey)

        getDriverFromFirebase()

        return false
    }

    private fun getDriverFromFirebase(){

        Log.d("ViewDriverInfo", "getUserFromFirebase")

        database = FirebaseDatabase.getInstance()
        driverInfoRef = database.getReference(Common.DRIVER_INFO_REFERENCE)
        firebaseAuth = FirebaseAuth.getInstance()

        Log.d("ViewDriverInfo", driverInfoRef.toString())

        if (driverKey != null) {

            Log.d("ViewDriverInfo", "driverKey != null")

            driverInfoRef.child(driverKey)
                    .addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(p0: DataSnapshot) {

                            val model = p0.getValue(DriverInfoModel::class.java)

                            loadDriverInfo(model)
                        }

                        override fun onCancelled(p0: DatabaseError) {

                            Toast.makeText(this@MapsActivity, p0.message, Toast.LENGTH_SHORT).show()

                        }
                    })
        }
    }

    private fun loadDriverInfo(model: DriverInfoModel?) {



        Common.selectedDriver = model

        val bottomSheetDialog = BottomSheetDialog(this@MapsActivity, R.style.BottomSheetDialogTheme)

        val bottomSheetView = LayoutInflater.from(applicationContext).inflate(
                R.layout.layout_bottom_sheet,
                findViewById(R.id.bottom_sheet) as LinearLayout?)

         driver_profileImage2 = bottomSheetView.findViewById<View>(R.id.driver_profileImage) as ImageView

        driver_Name2 = bottomSheetView.findViewById<View>(R.id.driver_Name2) as TextView
        driver_Email2 = bottomSheetView.findViewById<View>(R.id.driver_Email2) as TextView
        driver_Phone2 = bottomSheetView.findViewById<View>(R.id.driver_Phone2) as TextView

        driver_Name2.text = Common.selectedDriver!!.name
        driver_Email2.text = Common.selectedDriver!!.email
        driver_Phone2.text = Common.selectedDriver!!.phone


      if(Common.selectedDriver != null && Common.selectedDriver!!.image != null){

           Glide.with(this).load(Common.selectedDriver!!.image).into(driver_profileImage2)

       }

        bottomSheetView.findViewById<View>(R.id.selectDriver).setOnClickListener {

            Common.selectedDriverKey = driverKey

            Log.d("ViewDriverInfo",  driverKey)

            Log.d("ViewDriverInfo",  Common.selectedDriverKey.toString())

            bottomSheetDialog.dismiss()

            select_drop_off.visibility = View.VISIBLE

        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    //Driver Goes Offline
    override fun onDriverInfoLoadSuccess(driverGeoModel: DriverGeoModel?) {

        //If Already has a Marker with this Key, it Doesn't Set it Again

        if(!Common.markerList.containsKey(driverGeoModel!!.key)){


            driverKeySelect = driverGeoModel!!.key!!

            Common.markerList.put(
                    driverGeoModel!!.key!!,
                    mMap.addMarker(
                            MarkerOptions()
                                    .position(
                                            LatLng(
                                                    driverGeoModel!!.geoLocation!!.latitude,
                                                    driverGeoModel!!.geoLocation!!.longitude
                                            )
                                    )
                                    .title(
                                            Common.buildName(
                                                    driverGeoModel.driverInfoModel!!.name
                                            )
                                    )
                                    .snippet(driverGeoModel!!.key!!)
                                    .icon(bitmapDescriptorFromVector(R.drawable.ic_red_car))
                                    .flat(true)

                    )
            )

            mMap.setInfoWindowAdapter(CustomInfoWindowForGoogleMap(this))
            mMap.setOnMarkerClickListener(this@MapsActivity)

        }

        if(!TextUtils.isEmpty(cityName)){

            Log.d("MapsActivityTest", "!TextUtils.isEmpty(cityName")

            val driverLocation = FirebaseDatabase.getInstance()
                    .getReference(Common.DRIVERS_LOCATION_REFERENCE)
                    .child(cityName)
                    .child(driverGeoModel!!.key!!)

            driverLocation.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(p0: DataSnapshot) {

                    if (!p0.hasChildren()) {

                        if (Common.markerList[driverGeoModel!!.key!!] != null) {

                            //  Log.d("MapsActivityTest", "if(Common.markerList[driverGeoModel!!.key!!] != null)")

                            //Remove the Marker from the Map

                            val marker = Common.markerList.get(driverGeoModel!!.key!!)
                            //Remove Marker
                            marker!!.remove()
                            //Log.d("MapsActivityTest", "RemoveMarker")

                            //Remove the Marker Info
                            Common.markerList.remove(driverGeoModel!!.key!!)
                            //Log.d("MapsActivityTest", "RemoveMarkerInfo")


                            //Remove Driver Info
                            Common.driversSubscribe.remove(driverGeoModel.key!!)
                            //Log.d("MapsActivityTest", "RemoveDriverInfo")

                            //Fix Error When A Driver Declines Request, they Can Accept again if they Stop and Open App Again

                            if(Common.driversFound != null && Common.driversFound[driverGeoModel.key!!] != null)

                                Common.driversFound.remove(driverGeoModel!!.key!!)

                            driverLocation.removeEventListener(this)

                        }

                    }else {



                        if (Common.markerList[driverGeoModel!!.key!!] != null) {

                            val geoQueryModel = p0!!.getValue(GeoQueryModel::class.java)



                            val animationModel = AnimationModel(false, geoQueryModel!!)

                            if (Common.driversSubscribe.get(driverGeoModel.key!!) != null) {

                                val marker = Common.markerList.get(driverGeoModel!!.key!!)

                                val oldPosition =
                                        Common.driversSubscribe.get(driverGeoModel.key!!)

                                val from = StringBuilder()
                                        .append(oldPosition!!.geoQueryModel!!.l?.get(0))
                                        .append(",")
                                        .append(oldPosition!!.geoQueryModel!!.l?.get(1))
                                        .toString()

                                val to = StringBuilder()
                                        .append(animationModel.geoQueryModel!!.l?.get(0))
                                        .append(",")
                                        .append(animationModel.geoQueryModel!!.l?.get(1))
                                        .toString()

                                moveMarkerAnimation(
                                        driverGeoModel.key!!,
                                        animationModel,
                                        marker,
                                        from,
                                        to
                                )

                            } else {

                                //First Location Init
                                Log.d("MapsActivityTest", "else statement ")
                                Common.driversSubscribe.put(driverGeoModel.key!!,animationModel)
                            }
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                    Toast.makeText(this@MapsActivity, p0.message, Toast.LENGTH_SHORT).show()

                }


            })


        }

    }

    private fun moveMarkerAnimation(
            key: String,
            newData: AnimationModel,
            marker: Marker?,
            from: String,
            to: String

    ) {

        if(!newData.isRun){

            //Request API

            Log.d("MapsActivityTest", "Request API")

            compositeDisposable.add(iGoogleAPI.getDirections(
                    "driving", "less_driving", from, to,getString(R.string.google_api_key))
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

                                //polylineList = Common.decodePoly(polyline)
                                newData.polylineList = Common.decodePoly(polyline)
                            }

                            //Moving Object

                            newData.handler =  Handler(Looper.getMainLooper())

                            newData.index = -1

                            newData.next = 1

                            val runnable = object : Runnable {
                                override fun run() {

                                    if (newData.polylineList != null && newData.polylineList!!.size > 1) {

                                        if (newData.index < newData.polylineList!!.size - 2) {

                                            newData.index++

                                            newData.next = newData.index + 1

                                            newData.start = newData.polylineList!![newData.index]!!

                                            newData.end = newData.polylineList!![newData.next]!!

                                        }

                                        val valueAnimator = ValueAnimator.ofInt(0, 1)
                                        valueAnimator.duration = 3000
                                        valueAnimator.interpolator = LinearInterpolator()
                                        valueAnimator.addUpdateListener { value ->

                                            newData.v = value.animatedFraction

                                            newData.lat = newData.v * newData.end!!.latitude + (1 - newData.v) * newData.start!!.latitude

                                            newData.lng = newData.v * newData.end!!.longitude + (1 - newData.v) * newData.start!!.longitude

                                            val newPos = LatLng(newData.lat, newData.lng)
                                            marker!!.position = newPos
                                            marker!!.setAnchor(0.5f, 0.5f)
                                            marker!!.rotation = Common.getBearing(newData.start!!, newPos)

                                        }

                                        valueAnimator.start()

                                        if (newData.index < newData.polylineList!!.size - 2) {

                                            newData. handler!!.postDelayed(this, 1500) //Previously 2500

                                        } else if (newData.index < newData.polylineList!!.size - 1) {

                                            newData.isRun = false

                                            //Update
                                            Common.driversSubscribe.put(key, newData)

                                        }

                                    }

                                }


                            }

                            newData.handler!!.postDelayed(runnable, 1500) //1500

                            /* Handler(Looper.getMainLooper()).postDelayed({
                               // Your Code
                           }, 3000)*/

                        } catch (e: java.lang.Exception) {

                            Toast.makeText(this@MapsActivity, e.message, Toast.LENGTH_SHORT).show()

                        }

                    })


        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){

            return true

        }

        return super.onOptionsItemSelected(item)
    }

    private fun Context.bitmapDescriptorFromVector(vectorResId:Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        vectorDrawable.draw(Canvas(bitmap))
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}