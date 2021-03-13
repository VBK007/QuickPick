package com.example.quickpick.HomeLayoutforDrivers

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quickpick.EndUserLayouts.HomeENdUser
import com.example.quickpick.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class homefragfordrivers:Fragment(),OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var homeviewmodel: HomeENdUser
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var locatiomRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

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

                    mMap.isMyLocationEnabled=true
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

                    val view=mapFragment.view!!
                        .findViewById<View>("1".toInt())!!
                        .parent as View

                    val location=view.findViewById<View>("2".toInt())
                    val parent=location.layoutParams as RelativeLayout.LayoutParams
                    parent.addRule(RelativeLayout.ALIGN_TOP,0)
                    parent.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                    parent.bottomMargin=50


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


    @SuppressLint("MissingPermission")
    private fun init() {

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


            }


        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
//        fusedLocationProviderClient.requestLocationUpdates(
//            locatiomRequest,
//            locationCallback,
//            Looper.myLooper()
//        )

        fusedLocationProviderClient.requestLocationUpdates(locatiomRequest,locationCallback, Looper.myLooper())



    }
}