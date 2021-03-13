package com.example.quickpick.EndUserLayouts.Fragment

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Observable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.quickpick.Callback.FirebaseDriverInfoListener
import com.example.quickpick.Callback.FirebaseFailedListener
import com.example.quickpick.Commmon
import com.example.quickpick.EndUserLayouts.HomeENdUser
import com.example.quickpick.Model.DriverModel
import com.example.quickpick.Model.GeoqueryModel
import com.example.quickpick.Model.QuickpickdataModel
import com.example.quickpick.R
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.ArrayList

class homefragenduser : Fragment(), OnMapReadyCallback, FirebaseDriverInfoListener {
    private lateinit var mMap: GoogleMap
    private lateinit var homeviewmodel: HomeENdUser
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var locatiomRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var distance = 1.0
    val LIMIT_RANGE = 10.0
    var previousLocation: Location? = null
    var currentLocation: Location? = null
    var firstime = true

    lateinit var isFirebaseDriverInfoListener: FirebaseDriverInfoListener
    lateinit var isFirebaseFailedListyener: FirebaseFailedListener

    var cityname = ""
    override fun onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onDestroy()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragmenthome, container, false)
        init()

        mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return root

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        Dexter.withContext(requireContext())
            .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                @SuppressLint("MissingPermission")
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                    mMap.setOnMyLocationClickListener {
                        fusedLocationProviderClient.lastLocation
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
        isFirebaseDriverInfoListener = this

        locatiomRequest = LocationRequest()
        locatiomRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locatiomRequest.setFastestInterval(3000)
        locatiomRequest.interval = 5000
        locatiomRequest.setSmallestDisplacement(10f)

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
                    firstime = false
                } else {
                    previousLocation = currentLocation
                    currentLocation = locationresult.lastLocation

                }

                if (previousLocation!!.distanceTo(currentLocation) / 1000 <= LIMIT_RANGE)

                    loadAvailabelDrivers()


            }


        }

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

        fusedLocationProviderClient.requestLocationUpdates(
            locatiomRequest,
            locationCallback,
            Looper.myLooper()
        )
        loadAvailabelDrivers()

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
        fusedLocationProviderClient.lastLocation
            .addOnFailureListener { e ->
                Snackbar.make(requireView(), e.message!!, Snackbar.LENGTH_SHORT).show()

            }

            .addOnSuccessListener { location ->
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                var addreslist :List<Address> =ArrayList()
                try {
                    addreslist = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    cityname = addreslist[0].locality
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

                            Commmon.driverfound.add(DriverModel(key!!, location!!))
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


                } catch (e: IOException) {


                    Snackbar.make(
                        requireView(),
                        getString(R.string.permission_equire),
                        Snackbar.LENGTH_SHORT
                    ).show()


                }


            }

    }


    private fun finduserbykey(drivergeomodel: DriverModel) {
        FirebaseDatabase.getInstance().getReference(Commmon.DRIVER_INFO_REFERENCE)
            .child(drivergeomodel!!.key!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        drivergeomodel.driverInfoModel =
                            (snapshot.getValue(QuickpickdataModel::class.java))
                        isFirebaseDriverInfoListener.onDriverInfoloadedSucess(drivergeomodel)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    isFirebaseFailedListyener.onFirebaseFaiiled(error.message)
                }

            })

    }

    private fun addDriverMaker() {
        if (Commmon.driverfound.size > 0) {
            io.reactivex.Observable.fromIterable(Commmon.driverfound)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { drivermodel: DriverModel ->
                        finduserbykey(drivermodel)

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
                driverModel!!.key!!,

                mMap.addMarker(
                    MarkerOptions()
                        .position(
                            LatLng(
                                driverModel!!.geolocation!!.latitude,
                                driverModel!!.geolocation!!.longitude
                            )
                        )
                        .flat(true)
                        .title(Commmon.buildname(driverModel.driverInfoModel!!.username))
                        .snippet(driverModel.driverInfoModel!!.phonenumber)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))


                )

            )


        if (!TextUtils.isEmpty(cityname)) {
            val driverlocation=FirebaseDatabase.getInstance().getReference(Commmon.DRIVER_LOCATION_REFERENCE)
                .child(cityname)
                .child(driverModel!!.key!!)
            driverlocation.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                   if (snapshot.hasChildren()){
                       if (Commmon.markerlist.get(driverModel!!.key!!)!=null){
                           val marker=Commmon.markerlist.get(driverModel!!.key!!)
                           marker!!.remove()
                           Commmon.markerlist.remove(driverModel!!.key)
                           driverlocation.removeEventListener(this)
                       }
                   }
                }

                override fun onCancelled(error: DatabaseError) {
                    Snackbar.make(requireView(),error.message,Snackbar.LENGTH_SHORT).show()
                }

            })
        }


    }


}