package com.example.quickpick.HomeLayoutforDrivers

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.quickpick.Commmon
import com.example.quickpick.EndUserLayouts.HomeENdUser
import com.example.quickpick.Model.DriverRequestReceived
import com.example.quickpick.R
import com.example.quickpick.Remote.IGoogleAPi
import com.example.quickpick.Remote.RetroFitClient
import com.example.quickpick.Utils.UserUtils
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.operators.observable.ObservableAll
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.TimeUnit

class homefragfordrivers : Fragment(), OnMapReadyCallback {
    private var countDownEvent: Disposable? = null
    private lateinit var chip_decline: Chip
    private lateinit var layout_accept: CardView
    private lateinit var circularProgressbar: CircularProgressBar
    private lateinit var txt_estimate_time: TextView
    private lateinit var txt_estimate_distance: TextView
//decline
    private var driverRequestReceived: DriverRequestReceived? = null

    private val compositeDisposable = CompositeDisposable()
    private lateinit var iGoogleApi: IGoogleAPi
    private var rootlayout: FrameLayout? = null
    private var blackPolyline: Polyline? = null
    private var greyPolyLine: Polyline? = null
    private var polygonOptions: PolylineOptions? = null

    private var balckPolyLineOptions: PolylineOptions? = null
    private var polylinelist: ArrayList<LatLng?>? = null

    private lateinit var mMap: GoogleMap
    private lateinit var homeviewmodel: homefordriandowner
    private lateinit var mapFragment: SupportMapFragment

    private var locatiomRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private lateinit var onlineRef: DatabaseReference
    private var currentUserRef: DatabaseReference? = null
    private lateinit var driverlocationRef: DatabaseReference
    private lateinit var geoFire: GeoFire
    lateinit var txt_rating:TextView
    lateinit var layout_start_uber:CardView
    lateinit var txt_type_type_uber:TextView
    lateinit var img_round:ImageView


    private val onlineValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists() && currentUserRef != null) {
                currentUserRef!!.onDisconnect().removeValue()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Snackbar.make(mapFragment.requireView(), error.message, Snackbar.LENGTH_SHORT).show()
        }


    }



    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
        geoFire.removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
        onlineRef.removeEventListener(onlineValueListener)
        compositeDisposable.clear()
        if (EventBus.getDefault().hasSubscriberForEvent(homefordriandowner::class.java))
            EventBus.getDefault().removeStickyEvent(homeviewmodel::class.java)
        EventBus.getDefault().unregister(this)
        super.onDestroy()

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onDriverRequestReceived(event: DriverRequestReceived) {
        driverRequestReceived = event
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient!!.lastLocation.addOnFailureListener { e ->
            Snackbar.make(requireView(), e.message!!, Snackbar.LENGTH_LONG).show()

        }.addOnSuccessListener { location ->
            compositeDisposable.add(
                iGoogleApi.getDirections(
                    "driving",
                    "less_driving",
                    StringBuilder().append(location.latitude)
                        .append(",")
                        .append(location.longitude)
                        .toString(),
                    event.pickuplocation,
                    getString(R.string.google_api_key)
                )!!
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { returnresult ->

                        Log.d("API_RETURN", returnresult)

                        try {
                            val jsonobject = JSONObject(returnresult)
                            val jsonarray = jsonobject.getJSONArray("routes")

                            for (i in 0 until jsonarray.length()) {
                                val route = jsonarray.getJSONObject(i)
                                val poly = route.getJSONObject("overview_polyline")
                                val polyline = poly.getString("points")
                                polylinelist = Commmon.decodepoly(polyline)

                            }


                            polygonOptions = PolylineOptions()
                            polygonOptions!!.color(Color.GRAY)
                            polygonOptions!!.width(12f)
                            polygonOptions!!.startCap(SquareCap())
                            polygonOptions!!.jointType(JointType.ROUND)
                            polygonOptions!!.addAll(polylinelist)
                            greyPolyLine = mMap.addPolyline(polygonOptions)


                            balckPolyLineOptions = PolylineOptions()
                            balckPolyLineOptions!!.color(Color.GRAY)
                            balckPolyLineOptions!!.width(12f)
                            balckPolyLineOptions!!.startCap(SquareCap())
                            balckPolyLineOptions!!.jointType(JointType.ROUND)
                            balckPolyLineOptions!!.addAll(polylinelist)
                            blackPolyline = mMap.addPolyline(balckPolyLineOptions)


                            val valueAnimator = ValueAnimator.ofInt(0, 100)
                            valueAnimator.duration = 1100
                            valueAnimator.repeatCount = ValueAnimator.INFINITE
                            valueAnimator.interpolator = LinearInterpolator()
                            valueAnimator.addUpdateListener { value ->

                                val points = greyPolyLine!!.points
                                val percentvaluer = value.animatedValue.toString().toInt()
                                val size = points.size
                                val newpoints = (size * (percentvaluer / 100.0f).toInt())
                                val p = points.subList(0, newpoints)
                                blackPolyline!!.points = (p)

                            }

                            valueAnimator.start()
                            var orgin = LatLng(location.latitude, location.longitude)
                            val destination = LatLng(
                                event.pickuplocation!!.split(",")[0].toDouble(),
                                event.pickuplocation!!.split(",")[1].toDouble()
                            )
                            val latLngBound = LatLngBounds.Builder()
                                .include(orgin)
                                .include(destination)
                                .build()
                            val objects = jsonarray.getJSONObject(0)
                            val legs = objects.getJSONArray("legs")
                            val legsobject = legs.getJSONObject(0)
                            val time = legsobject.getJSONObject("duration")
                            val duration = time.getString("text")

                            val distanceEstimate = legsobject.getString("start_address")
                            val distance = legsobject.getString("end_address")

                            txt_estimate_time.setText(duration)
                            txt_estimate_distance.setText(distance)


                            mMap.addMarker(
                                MarkerOptions().position(destination)
                                    .icon(BitmapDescriptorFactory.defaultMarker())
                                    .title("PickUp Location")
                            )

//Display layout

                            countDownEvent = Observable.interval(100, TimeUnit.MILLISECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnNext { x ->
                                    circularProgressbar.progress += 1f

                                }.takeUntil { along -> along == "100".toLong() }
                                .doOnComplete {
                                   createtripplan(event,duration,distance)

                                }.subscribe()

                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBound, 160))
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.cameraPosition!!.zoom - 1))

                            chip_decline.visibility = View.VISIBLE
                            layout_accept.visibility = View.VISIBLE



                        } catch (e: Exception) {
                            // Snackbar.make(applicationContext, e.message!!, Snackbar.LENGTH_LONG).show()


                        }
                    })


        }


    }

    private fun createtripplan(event: DriverRequestReceived, duration: String, distance: String) {
        setlayoutprocess(true)

    }

    private fun setlayoutprocess(process: Boolean) {
        var color  =-1
        if (process){
            color =ContextCompat.getColor(requireContext(),R.color.mycolor1)
            circularProgressbar.indeterminateMode = true
            txt_rating.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_baseline_stars_24,0)


        }
        else{
            color =ContextCompat.getColor(requireContext(),R.color.white)
            circularProgressbar.indeterminateMode = true
            circularProgressbar.progress=0F
            txt_rating.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_baseline_star_24,0)

        }









    }

    override fun onResume() {
        super.onResume()
        registerOnnlineSystem()

    }

    private fun registerOnnlineSystem() {
        onlineRef.addValueEventListener(onlineValueListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.activity_maps, container, false)
        initview(root)
        init()

        mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return root

    }

    private fun initview(root: View?) {
        chip_decline = root!!.findViewById(R.id.chip_decline) as Chip
        layout_accept = root!!.findViewById(R.id.layout_accept) as CardView
        circularProgressbar = root!!.findViewById(R.id.circularProgress) as CircularProgressBar
        txt_estimate_distance = root!!.findViewById(R.id.txt_estimate_distance) as TextView
        txt_estimate_time = root!!.findViewById(R.id.txt_estimate_time) as TextView
        rootlayout = root!!.findViewById(R.id.root)
        txt_rating = root!!.findViewById(R.id.txt_rating)

        chip_decline.setOnClickListener {
            if (driverRequestReceived != null) {
                if (countDownEvent != null)
                    countDownEvent!!.dispose()
                chip_decline.visibility = View.GONE
                layout_accept.visibility = View.GONE
                mMap.clear()
                circularProgressbar.progress = 0f
                UserUtils.sendDeclineRequest(rootlayout!!, activity!!, driverRequestReceived!!.key!!)
                driverRequestReceived = null

            }

        }


    }


    private fun init() {
        iGoogleApi = RetroFitClient.instance!!
            .create(IGoogleAPi::class.java)

        onlineRef = FirebaseDatabase.getInstance().reference.child("info/connected")
        builtLocationRequest()
        builtLocationCallback()
        updateLocation()


    }

    private fun updateLocation() {

        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)


            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            fusedLocationProviderClient!!.requestLocationUpdates(
                locatiomRequest,
                locationCallback,
                Looper.myLooper()
            )
        }
    }

    private fun builtLocationCallback() {
        if (locationCallback == null) {
            locationCallback = object : LocationCallback() {

                override fun onLocationResult(locationresult: LocationResult?) {
                    super.onLocationResult(locationresult)
                    val newpos = LatLng(
                        locationresult!!.lastLocation!!.latitude,
                        locationresult.lastLocation.longitude
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newpos, 18f))

                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val addresslList: List<Address>?
                    try {
                        addresslList = geocoder.getFromLocation(
                            locationresult.lastLocation.latitude,
                            locationresult.lastLocation.longitude, 1
                        )
                        val cityname = addresslList[0].locality

                        driverlocationRef = FirebaseDatabase.getInstance()
                            .getReference(Commmon.DRIVER_LOCATION_REFERENCE)
                            .child(cityname)
                        currentUserRef = driverlocationRef
                            .child(FirebaseAuth.getInstance().currentUser!!.uid)


                        geoFire = GeoFire(driverlocationRef)


                        geoFire.setLocation(
                            FirebaseAuth.getInstance().currentUser!!.uid,
                            GeoLocation(
                                locationresult.lastLocation.latitude,
                                locationresult.lastLocation.longitude
                            )
                        ) { key: String?, error: DatabaseError? ->

                            if (error != null) {
                                Snackbar.make(
                                    mapFragment.requireView(),
                                    error.message,
                                    Snackbar.LENGTH_SHORT
                                ).show()

                            } else {

                            }


                        }

                        registerOnnlineSystem()

                    } catch (e: IOException) {
                        Snackbar.make(requireView(), e.message!!, Snackbar.LENGTH_LONG).show()
                    }


                }


            }
        }
    }

    private fun builtLocationRequest() {

        if (locatiomRequest == null) {
            locatiomRequest = LocationRequest()
            locatiomRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            locatiomRequest!!.setFastestInterval(15000)
            locatiomRequest!!.interval = 10000
            locatiomRequest!!.setSmallestDisplacement(50f)
        }


    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        Dexter.withContext(requireContext())
            .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        return
                    }
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                    mMap.setOnMyLocationClickListener {
                        fusedLocationProviderClient!!.lastLocation
                            .addOnFailureListener { e ->
                                Toast.makeText(context!!, e.message, Toast.LENGTH_SHORT).show()

                            }
                            .addOnSuccessListener { location ->
                                val userlating = LatLng(location.latitude, location.longitude)
                                mMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        userlating,
                                        18f
                                    )
                                )
                            }
                        true

                    }

                    val view = mapFragment.view!!
                        .findViewById<View>("1".toInt())!!
                        .parent as View

                    val location = view.findViewById<View>("2".toInt())
                    val parent = location.layoutParams as RelativeLayout.LayoutParams
                    parent.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
                    parent.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                    parent.bottomMargin = 50


//Location
                    builtLocationRequest()
                    builtLocationCallback()
                    updateLocation()


                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(
                        context!!,
                        "Permission " + p0?.permissionName + "was denied",
                        Toast.LENGTH_SHORT
                    ).show()

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    p1!!.continuePermissionRequest()

                }

            }).check()



        try {
            val sucess = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.uber_maps_style
                )
            )
            if (!sucess)
                Log.e("VBK_ERROR", "Style parsing error")

        } catch (e: Resources.NotFoundException) {
            e.message?.let { Log.e("EDMT_ERROR", it) }
        }


        Snackbar.make(
            mapFragment.requireView(),
            "You're online!",
            Snackbar.LENGTH_SHORT
        ).show()


    }


}