 package com.example.quickpick.EndUserLayouts.Fragment

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.example.quickpick.Callback.FirebaseDriverInfoListener
import com.example.quickpick.Callback.FirebaseFailedListener
import com.example.quickpick.Commmon
import com.example.quickpick.EndUserLayouts.HomeENdUser
import com.example.quickpick.EventBus.SelelectedPlaceEvent
import com.example.quickpick.Model.AnimationModel
import com.example.quickpick.Model.DriverModel
import com.example.quickpick.Model.GeoqueryModel
import com.example.quickpick.Model.QuickpickdataModel
import com.example.quickpick.R
import com.example.quickpick.Remote.IGoogleAPi
import com.example.quickpick.Remote.RetroFitClient
import com.example.quickpick.RequestDriveractivity
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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class homefragenduser : Fragment(), OnMapReadyCallback, FirebaseDriverInfoListener {
    private lateinit var mMap: GoogleMap
    private lateinit var homeviewmodel: HomeENdUser
    private lateinit var mapFragment: SupportMapFragment
    private  var locatiomRequest: LocationRequest?=null
    private  var locationCallback: LocationCallback?=null
    private  var fusedLocationProviderClient: FusedLocationProviderClient?=null
    private lateinit var slidingPaneLayout: SlidingUpPanelLayout
    private lateinit var text_welcome: TextView
    private lateinit var autocompleteSupportFragment: AutocompleteSupportFragment

    var distance = 1.0
    val LIMIT_RANGE = 10.0
    var previousLocation: Location? = null
    var currentLocation: Location? = null
    var firstime = true


    lateinit var isFirebaseDriverInfoListener: FirebaseDriverInfoListener
    lateinit var isFirebaseFailedListyener: FirebaseFailedListener

    var cityname = ""
    val compositeDisposable = CompositeDisposable()
    lateinit var iGoogleApi: IGoogleAPi


    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    override fun onDestroy() {
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
        super.onDestroy()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragmenthome, container, false)

        mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        init()
        initviews(root)
        return root

    }

    private fun initviews(root: View?) {
        slidingPaneLayout = root!!.findViewById(R.id.frame) as SlidingUpPanelLayout
        text_welcome = root!!.findViewById(R.id.txt_welcome)
        Commmon.setWelcomeMessage(text_welcome)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        Dexter.withContext(requireContext())
            .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            requireContext(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
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
                    parent.addRule(RelativeLayout.ALIGN_TOP, 0)
                    parent.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                    parent.bottomMargin = 250


                    //updateLocation
                    builtLocationRequest()
                    builLocationCallback()
                    loadLocation()
                    loadAvailabelDrivers()



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


        mMap.uiSettings.isZoomControlsEnabled = true




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


    }


    private fun init() {
        Places.initialize(requireContext(), getString(R.string.google_maps_key))
        autocompleteSupportFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteSupportFragment.setPlaceFields(
            Arrays.asList(
                Place.Field.ID,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.NAME
            )
        )

        autocompleteSupportFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: Place) {
                // Snackbar.make(requireView(),""+p0.latLng!!,Snackbar.LENGTH_LONG).show()

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
                fusedLocationProviderClient!!.lastLocation.addOnSuccessListener { location ->
                    val orgin = LatLng(location.latitude, location.longitude)
                    val destination = LatLng(p0.latLng!!.latitude, p0.latLng!!.longitude)

                    startActivity(Intent(requireContext(), RequestDriveractivity::class.java))

                    EventBus.getDefault().postSticky(SelelectedPlaceEvent(orgin, destination))


                }


            }

            override fun onError(p0: Status) {
                Snackbar.make(requireView(), "" + p0.statusMessage!!, Snackbar.LENGTH_LONG).show()

            }

        })

        iGoogleApi = RetroFitClient.instance!!.create(IGoogleAPi::class.java)

        isFirebaseDriverInfoListener = this

      builtLocationRequest()
        builLocationCallback()
        loadLocation()
        loadAvailabelDrivers()

    }

    private fun loadLocation() {
        if (fusedLocationProviderClient==null){
            fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireContext())

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
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

    private fun builLocationCallback() {
        if (locationCallback==null){
            locationCallback = object : LocationCallback() {

                override fun onLocationResult(locationresult: LocationResult?) {
                    super.onLocationResult(locationresult)
                    val newpos = LatLng(
                        locationresult!!.lastLocation!!.latitude,
                        locationresult.lastLocation.longitude
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newpos, 18f))

                    if (firstime) {
                        previousLocation = locationresult.lastLocation
                        currentLocation = locationresult.lastLocation
                        setRestrictPlaceCountry(locationresult.lastLocation)

                        firstime = false
                    } else {
                        previousLocation = currentLocation
                        currentLocation = locationresult.lastLocation

                    }

                    if (previousLocation!!.distanceTo(currentLocation) / 1000 <= LIMIT_RANGE)

                        loadAvailabelDrivers()


                }


            }
        }
    }

    private fun builtLocationRequest() {
        if (locatiomRequest==null){

            locatiomRequest = LocationRequest()
            locatiomRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            locatiomRequest!!.setFastestInterval(3000)
            locatiomRequest!!.interval = 5000
            locatiomRequest!!.setSmallestDisplacement(10f)


        }
    }

    private fun setRestrictPlaceCountry(location: Location?) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            var addreslist = geocoder.getFromLocation(location!!.latitude, location!!.longitude, 1)
            if (addreslist.size > 0) {
                autocompleteSupportFragment.setCountry(addreslist[0].countryCode)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }


    }

    private fun loadAvailabelDrivers() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Snackbar.make(
                requireView(),
                getString(R.string.permission_equire),
                Snackbar.LENGTH_SHORT
            ).show()

            return
        }
        fusedLocationProviderClient!!.lastLocation
            .addOnFailureListener { e ->
                Snackbar.make(requireView(), e.message!!, Snackbar.LENGTH_SHORT).show()

            }

            .addOnSuccessListener { location ->
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                var addreslist: List<Address> = ArrayList()
                try {
                    addreslist =
                        geocoder.getFromLocation(location!!.latitude, location!!.longitude, 1)
                    if (addreslist.size > 0)
                        cityname = addreslist[0].locality

                    if (!TextUtils.isEmpty(cityname)) {
                        val driver_locationref = FirebaseDatabase.getInstance()
                            .getReference(Commmon.DRIVER_LOCATION_REFERENCE)
                            .child(cityname)
                        val gf = GeoFire(driver_locationref)

                        val groquertry = gf.queryAtLocation(
                            GeoLocation(
                                location.latitude, location.longitude
                            ), distance
                        )
                        groquertry.removeAllListeners()

                        groquertry.addGeoQueryEventListener(object : GeoQueryEventListener {
                            override fun onKeyEntered(key: String?, location: GeoLocation?) {

                                if (Commmon.driverfound.containsKey(key))
                                    Commmon.driverfound[key!!] = DriverModel(key, location)

                            }

                            override fun onKeyExited(key: String?) {

                            }

                            override fun onKeyMoved(key: String?, location: GeoLocation?) {

                            }

                            override fun onGeoQueryReady() {
                                if (distance <= LIMIT_RANGE) {
                                    distance++
                                    loadAvailabelDrivers()
                                } else {
                                    distance = 0.0
                                    addDriverMaker()
                                }
                            }

                            override fun onGeoQueryError(error: DatabaseError?) {
                                Snackbar.make(requireView(), error!!.message, Snackbar.LENGTH_SHORT)
                                    .show()

                            }

                        })

                        driver_locationref.addChildEventListener(object : ChildEventListener {
                            override fun onChildAdded(
                                snapshot: DataSnapshot,
                                previousChildName: String?
                            ) {

                                val geoQuerymodel = snapshot.getValue(GeoqueryModel::class.java)

                                val geoLocation =
                                    GeoLocation(geoQuerymodel!!.l!![0], geoQuerymodel!!.l!![1])

                                val drivergeomodel = DriverModel(snapshot.key, geoLocation)
                                val newDriverlocation = Location("")
                                newDriverlocation.latitude = geoLocation.latitude
                                newDriverlocation.longitude = geoLocation.longitude
                                val newdistance = location.distanceTo(newDriverlocation) / 1000

                                if (newdistance <= LIMIT_RANGE) {
                                    finduserbykey(drivergeomodel)
                                }


                            }

                            override fun onChildChanged(
                                snapshot: DataSnapshot,
                                previousChildName: String?
                            ) {

                            }

                            override fun onChildRemoved(snapshot: DataSnapshot) {

                            }

                            override fun onChildMoved(
                                snapshot: DataSnapshot,
                                previousChildName: String?
                            ) {

                            }

                            override fun onCancelled(error: DatabaseError) {
                                Snackbar.make(requireView(), error.message, Snackbar.LENGTH_SHORT)
                                    .show()

                            }

                        })

                    } else {
                        Snackbar.make(
                            requireView(),
                            getString(R.string.cit_name_not_found),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: IOException) {


                    Snackbar.make(
                        requireView(),
                        getString(R.string.permission_equire),
                        Snackbar.LENGTH_SHORT
                    ).show()


                }


            }

    }


    private fun finduserbykey(drivergeomodel: DriverModel?) {
        FirebaseDatabase.getInstance().getReference(Commmon.DRIVER_INFO_REFERENCE)
            .child(drivergeomodel!!.key!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        drivergeomodel.driverInfoModel =
                            (snapshot.getValue(QuickpickdataModel::class.java))

                        Commmon.driverfound[drivergeomodel.key!!]?.driverInfoModel =
                            (snapshot.getValue(QuickpickdataModel::class.java))
                        isFirebaseDriverInfoListener.onDriverInfoloadedSucess(drivergeomodel)
                    } else {
                        isFirebaseFailedListyener.onFirebaseFaiiled(getString(R.string.key_not_found) + drivergeomodel.key)
                    }


                }

                override fun onCancelled(error: DatabaseError) {
                    isFirebaseFailedListyener.onFirebaseFaiiled(error.message)
                }

            })

    }

    private fun addDriverMaker() {
        if (Commmon.driverfound.size > 0) {
            io.reactivex.Observable.fromIterable(Commmon.driverfound.keys)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { key: String? ->
                        finduserbykey(Commmon.driverfound[key!!])

                    }, { t: Throwable? ->
                        Snackbar.make(requireView(), t!!.message!!, Snackbar.LENGTH_SHORT).show()

                    }
                )
        } else {
            Snackbar.make(requireView(), getString(R.string.driver_notfound), Snackbar.LENGTH_SHORT)
                .show()

        }
    }

    override fun onDriverInfoloadedSucess(driverModel: DriverModel?) {
        if (!Commmon.markerlist.containsKey(driverModel!!.key))
            Commmon.markerlist.put(
                driverModel.key!!,

                mMap.addMarker(
                    MarkerOptions()
                        .position(
                            LatLng(
                                driverModel.geolocation!!.latitude,
                                driverModel.geolocation!!.longitude
                            )
                        )
                        .flat(true)
                        .title(Commmon.buildname(driverModel.driverInfoModel!!.username))
                        .snippet(driverModel.driverInfoModel!!.phonenumber)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cae))


                )

            )


        if (!TextUtils.isEmpty(cityname)) {
            val driverlocation =
                FirebaseDatabase.getInstance().getReference(Commmon.DRIVER_LOCATION_REFERENCE)
                    .child(cityname)
                    .child(driverModel!!.key!!)
            driverlocation.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.hasChildren()) {
                        if (Commmon.markerlist.get(driverModel!!.key!!) != null) {
                            val marker = Commmon.markerlist.get(driverModel!!.key!!)
                            marker!!.remove()
                            Commmon.markerlist.remove(driverModel!!.key)
                            Commmon.driverSubscribe.remove(driverModel.key!!)
                            if (Commmon.driverfound!=null&&Commmon.driverfound[driverModel.key!!]!=null)
                                Commmon.driverfound[driverModel!!.key!!]
                            driverlocation.removeEventListener(this)
                        }
                    } else {
                        if (Commmon.markerlist.get(driverModel!!.key!!) != null) {

                            val geoqueryModel = snapshot!!.getValue(GeoqueryModel::class.java)
                            val animationModel = AnimationModel(false, geoqueryModel)

                            if (Commmon.driverSubscribe.get(driverModel.key!!) != null) {
                                val marker = Commmon.markerlist.get(driverModel!!.key)
                                val oldposition = Commmon.driverSubscribe.get(driverModel.key!!)

                                val from = StringBuilder()
                                    .append(oldposition!!.geoqueyModel!!.l?.get(0))
                                    .append(",")
                                    .append(oldposition.geoqueyModel!!.l?.get(1))
                                    .toString()
                                val to = StringBuilder()
                                    .append(animationModel.geoqueyModel!!.l?.get(0))
                                    .append(",")
                                    .append(animationModel.geoqueyModel!!.l?.get(1))
                                    .toString()

                                movemakerAnuimation(
                                    driverModel.key!!,
                                    animationModel,
                                    marker,
                                    from,
                                    to
                                )


                            } else {
                                Commmon.driverSubscribe.put(driverModel.key!!, animationModel)
                            }

                        }


                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Snackbar.make(requireView(), error.message, Snackbar.LENGTH_SHORT).show()
                }

            })
        }


    }

    private fun movemakerAnuimation(
        key: String,
        newData: AnimationModel,
        marker: Marker?,
        from: String,
        to: String
    ) {

        if (!newData.isRun) {

            compositeDisposable.add(iGoogleApi.getDirections(
                "driving",
                "less_driving",
                from, to,
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
                            // polylinelist = Commmon.decodepoly(polyline)
                            newData.polylinelist = Commmon.decodepoly(polyline)

                        }


                        newData.index = -1
                        newData.next = 1

                        val runnable = object : Runnable {
                            override fun run() {
                                if (newData.polylinelist != null && newData.polylinelist!!.size > 1) {
                                    if (newData.index < newData.polylinelist!!.size - 2) {
                                        newData.index++
                                        newData.next = newData.index + 1
                                        newData.start = newData.polylinelist!![newData.index]!!
                                        newData.end = newData.polylinelist!![newData.next]!!
                                    }

                                    val valueanui = ValueAnimator.ofInt(0, 1)
                                    valueanui.duration = 3000
                                    valueanui.interpolator = LinearInterpolator()
                                    valueanui.addUpdateListener { value ->
                                        newData.v = value.animatedFraction
                                        newData.lat =
                                            newData.v * newData.end!!.latitude + (1 - newData.v) * newData.start!!.latitude
                                        newData.lng =
                                            newData.v * newData.end!!.latitude + (1 - newData.v) * newData.start!!.latitude
                                        val newpos = LatLng(newData.lat, newData.lng)
                                        marker!!.position = newpos
                                        marker!!.rotation =
                                            Commmon.getBearing(newData.start!!, newpos)


                                    }
                                    valueanui.start()
                                    if (newData.index < newData.polylinelist!!.size - 2) {
                                        newData.handler!!.postDelayed(this, 1500)
                                    } else if (newData.index < newData.polylinelist!!.size - 1) {
                                        newData.isRun = false
                                        Commmon.driverSubscribe.put(key, newData)
                                    }
                                }
                            }

                        }

                        newData.handler!!.postDelayed(runnable, 1500)

                    } catch (e: Exception) {
                        Snackbar.make(requireView(), e.message!!, Snackbar.LENGTH_LONG).show()
                    }
                }

            )


        }


    }


}