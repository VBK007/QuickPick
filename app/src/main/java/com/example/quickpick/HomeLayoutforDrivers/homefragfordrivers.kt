package com.example.quickpick.HomeLayoutforDrivers

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.quickpick.Commmon
import com.example.quickpick.EndUserLayouts.HomeENdUser
import com.example.quickpick.R
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.IOException
import java.util.*

class homefragfordrivers : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var homeviewmodel: HomeENdUser
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var locatiomRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var onlineRef: DatabaseReference
    private var currentUserRef: DatabaseReference? = null
    private lateinit var driverlocationRef: DatabaseReference
    private lateinit var geoFire: GeoFire

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

    override fun onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        geoFire.removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
        onlineRef.removeEventListener(onlineValueListener)
        super.onDestroy()

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
        val root = inflater.inflate(R.layout.fragmenthome, container, false)
        init()

        mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return root

    }


    private fun init() {

        onlineRef = FirebaseDatabase.getInstance().reference.child("info/connected")


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
                            Snackbar.make(
                                mapFragment.requireView(),
                                "You're online!",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }


                    }

                    registerOnnlineSystem()

                } catch (e: IOException) {
                    Snackbar.make(requireView(), e.message!!, Snackbar.LENGTH_LONG).show()
                }


            }


        }

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
        fusedLocationProviderClient.requestLocationUpdates(
            locatiomRequest,
            locationCallback,
            Looper.myLooper()
        )


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
                    parent.bottomMargin = 50


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


    }




}