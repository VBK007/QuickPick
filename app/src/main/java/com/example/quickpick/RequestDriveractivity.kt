package com.example.quickpick

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.quickpick.EventBus.SelelectedPlaceEvent
import com.example.quickpick.Model.DeclineRequestFromDrivers
import com.example.quickpick.Model.DriverModel
import com.example.quickpick.Model.QuickpickdataModel
import com.example.quickpick.Remote.IGoogleAPi
import com.example.quickpick.Remote.RetroFitClient
import com.example.quickpick.Utils.UserUtils
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.ui.IconGenerator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.lang.Exception

class RequestDriveractivity : AppCompatActivity(), OnMapReadyCallback {

    var lastusercirclr: Circle? = null
    val duration = 1000
    var animator: ValueAnimator? = null
    private val DESIRED_NUM_OF_SPINS = 5
    private val DESIRED_SECOUNDS_PER_ONE_FULL_360_SPIN = 40
    private var lastDriverCall: DriverModel? = null
    var lastplusAnimato: Animator? = null
    private lateinit var mMap: GoogleMap
    private var selectedPlaceEvent: SelelectedPlaceEvent? = null
    private lateinit var mapFragment: SupportMapFragment

    private lateinit var btn_confirm_uber: Button
    private lateinit var btn_confirm_pickup: Button

    private lateinit var cardpickup: CardView

    private lateinit var cardviewconfirm: CardView

    private lateinit var txt_orgin: TextView
    private lateinit var txt_address_pickup: TextView
    private var fill_maps: View? = null
    private lateinit var finding_your_ride: CardView


    //Routes
    private val compositeDisposable = CompositeDisposable()
    private lateinit var iGoogleApi: IGoogleAPi

    private var blackPolyline: Polyline? = null
    private var greyPolyLine: Polyline? = null
    private var polygonOptions: PolylineOptions? = null

    private var balckPolyLineOptions: PolylineOptions? = null
    private var polylinelist: ArrayList<LatLng?>? = null

    private var orginmarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var amin_layout: RelativeLayout? = null

    override fun onStart() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        super.onStart()
    }

    override fun onStop() {
        compositeDisposable.clear()
        if (EventBus.getDefault().hasSubscriberForEvent(SelelectedPlaceEvent::class.java))
            EventBus.getDefault().removeStickyEvent(SelelectedPlaceEvent::class.java)

        if (EventBus.getDefault().hasSubscriberForEvent(DeclineRequestFromDrivers::class.java))
            EventBus.getDefault().removeStickyEvent(DeclineRequestFromDrivers::class.java)
        EventBus.getDefault().unregister(this)


        super.onStop()
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onDeclineMessage(event: DeclineRequestFromDrivers) {
        if (lastDriverCall != null) {
            Commmon.driverfound.get(lastDriverCall!!.key)!!.isDecline = true
            findnearbydrivrers(selectedPlaceEvent!!.orgin)

        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onSelectedPlaceEvent(event: SelelectedPlaceEvent) {
        selectedPlaceEvent = event
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_driveractivity)

        inti()
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    private fun inti() {
        iGoogleApi = RetroFitClient.instance!!.create(IGoogleAPi::class.java)

//Event
        fill_maps!!.findViewById<View>(R.id.fill_maps)
        amin_layout = findViewById(R.id.main_layout)

        cardpickup.findViewById<CardView>(R.id.confirm_pickup_layout)
        cardviewconfirm.findViewById<CardView>(R.id.confirm_uber_layout)

        txt_address_pickup.findViewById<View>(R.id.address_pickup)
        btn_confirm_uber.findViewById<View>(R.id.btn_confirm_uber) as Button

        finding_your_ride.findViewById<CardView>(R.id.finding_your_ride_lay)
        btn_confirm_pickup.findViewById<Button>(R.id.btn_confirm_pickup)


        btn_confirm_uber.setOnClickListener {
            cardpickup.visibility = View.VISIBLE
            cardviewconfirm.visibility = View.GONE


            setDataPickUp()
        }

        btn_confirm_pickup!!.setOnClickListener {

            if (mMap == null) return@setOnClickListener
            if (selectedPlaceEvent == null) return@setOnClickListener

            mMap.clear()


            val cameraPos = CameraPosition.Builder()
                .target(selectedPlaceEvent!!.orgin)

                .tilt(45f)
                .zoom(16f)
                .build()
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos))

            addMarkerPlusAnimation()

        }


    }

    private fun addMarkerPlusAnimation() {

        cardpickup.visibility = View.GONE
        fill_maps!!.visibility = View.VISIBLE
        finding_your_ride.visibility = View.VISIBLE

        orginmarker = mMap.addMarker(
            MarkerOptions().icon(
                BitmapDescriptorFactory.defaultMarker()
            ).position(selectedPlaceEvent!!.orgin)
        )

        addPulsatinEffect(selectedPlaceEvent!!.orgin)


    }

    private fun addPulsatinEffect(orgin: LatLng) {
        if (lastplusAnimato != null) lastplusAnimato!!.cancel()
        if (lastusercirclr != null) lastusercirclr!!.center = orgin

        lastplusAnimato =
            Commmon.valueAnimate(duration, object : ValueAnimator.AnimatorUpdateListener {
                override fun onAnimationUpdate(animation: ValueAnimator?) {
                    if (lastusercirclr != null) lastusercirclr!!.radius =
                        animation!!.animatedValue.toString().toDouble() else {
                        lastusercirclr = mMap.addCircle(
                            CircleOptions()
                                .center(orgin)
                                .radius(animation!!.animatedValue.toString().toDouble())
                                .strokeColor(Color.WHITE)
                                .fillColor(
                                    ContextCompat.getColor(
                                        this@RequestDriveractivity,
                                        R.color.mapdarker
                                    )
                                )


                        )


                    }
                }


            })

        startMapCameraSpinningAnimation(mMap.cameraPosition.target)


    }

    private fun startMapCameraSpinningAnimation(target: LatLng?) {
        if (animator != null) animator!!.cancel()
        animator = ValueAnimator.ofFloat(0f, (DESIRED_NUM_OF_SPINS * 360).toFloat())
        animator!!.duration =
            (DESIRED_NUM_OF_SPINS * DESIRED_SECOUNDS_PER_ONE_FULL_360_SPIN * 1000).toLong()
        animator!!.interpolator = LinearInterpolator()
        animator!!.startDelay = (100)
        animator!!.addUpdateListener { valueAnimation ->
            val nearBearingvalue = valueAnimation.animatedValue as Float
            mMap.moveCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(target)
                        .zoom(16f)
                        .tilt(45f)
                        .bearing(nearBearingvalue)
                        .build()
                )
            )

        }
        animator!!.start()

        findnearbydrivrers(target)


    }

    private fun findnearbydrivrers(target: LatLng?) {
        if (Commmon.driverfound.size > 0) {

            var min = 0f
            var foundriver: DriverModel? = null
            val currentLocation = Location("")
            currentLocation.latitude = target!!.latitude
            currentLocation.longitude = target!!.longitude
            for (key in Commmon.driverfound.keys) {
                val driverLocation = Location("")
                driverLocation.latitude = Commmon.driverfound[key]!!.geolocation!!.latitude
                driverLocation.longitude = Commmon.driverfound[key]!!.geolocation!!.longitude
                if (min == 0f) {
                    min = driverLocation.distanceTo(currentLocation)
                    if (!Commmon.driverfound[key]!!.isDecline) {
                        foundriver = Commmon.driverfound[key]
                        break
                    } else {
                        continue
                    }


                } else if (driverLocation.distanceTo(currentLocation) < min) {
                    min = driverLocation.distanceTo(currentLocation)
                    if (!Commmon.driverfound[key]!!.isDecline) {
                        foundriver = Commmon.driverfound[key]
                        break
                    } else {
                        continue
                    }

                }


            }
            if (foundriver != null) {
                UserUtils.sendRequestToDriver(
                    this,
                    amin_layout,
                    foundriver, target
                )
                lastDriverCall = foundriver
            } else {
                Toast.makeText(this, getString(R.string.no_driver_accepted), Toast.LENGTH_LONG)
                    .show()
                lastDriverCall = null
                finish()
            }


        } else {
            Toast.makeText(this, "" + R.string.driver_notfound, Toast.LENGTH_LONG).show()
            lastDriverCall = null
            finish()


        }


    }


    override fun onDestroy() {
        if (animator != null) animator!!.end()
        super.onDestroy()
    }

    private fun setDataPickUp() {
        txt_address_pickup.text = if (txt_orgin != null) txt_orgin!!.text else "None"
        mMap.clear()
        addPickUpMarker()

    }

    private fun addPickUpMarker() {
        val view = layoutInflater.inflate(R.layout.pickup_info_windows, null)
        val generator = IconGenerator(this)
        generator.setContentView(view)
        generator.setBackground(ColorDrawable(Color.TRANSPARENT))
        val icon = generator.makeIcon()
        orginmarker = mMap.addMarker(
            MarkerOptions().icon(
                BitmapDescriptorFactory.fromBitmap(icon)

            ).position(selectedPlaceEvent!!.orgin)
        )

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        //layout


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        drawPath(selectedPlaceEvent!!)
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.setOnMyLocationClickListener { location ->
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedPlaceEvent!!.orgin, 18f))
            mMap.uiSettings.isZoomControlsEnabled = true
            try {
                val sucess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this,
                        R.raw.uber_maps_style
                    )
                )

                if (!sucess) {
                    Snackbar.make(
                        mapFragment.requireView(),
                        "Load Map Style Failed",
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }

            } catch (e: Exception) {
                Snackbar.make(mapFragment.requireView(), e.message!!, Snackbar.LENGTH_LONG).show()
            }

        }


        val locationButton = findViewById<View>("1".toInt())!!.parent!! as View

        val params = locationButton.layoutParams as RelativeLayout.LayoutParams
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        params.bottomMargin = 250


    }

    private fun drawPath(selectedPlaceEvent: SelelectedPlaceEvent) {

        compositeDisposable.add(iGoogleApi.getDirections(
            "driving",
            "less_driving",
            selectedPlaceEvent.orginString, selectedPlaceEvent.destinationString,
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
                    val latLngBound = LatLngBounds.Builder()
                        .include(selectedPlaceEvent.orgin)
                        .include(selectedPlaceEvent.destination)
                        .build()
                    val objects = jsonarray.getJSONObject(0)
                    val legs = objects.getJSONArray("legs")
                    val legsobject = legs.getJSONObject(0)
                    val time = legsobject.getJSONObject("duration")
                    val duration = time.getString("text")

                    val startAdress = legsobject.getString("start_address")
                    val end_Address = legsobject.getString("end_address")
                    addOrginMarker(duration, startAdress)
                    addDestinationMarjker(end_Address)


                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBound, 160))
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.cameraPosition!!.zoom - 1))


                } catch (e: Exception) {
                    // Snackbar.make(applicationContext, e.message!!, Snackbar.LENGTH_LONG).show()


                }
            }

        )

    }

    private fun addDestinationMarjker(endAddress: String) {

        val view = layoutInflater.inflate(R.layout.destination, null)

        val tx = view.findViewById<View>(R.id.txt_destination) as TextView
        tx.text = Commmon.formatMode(endAddress)
        val genertor = IconGenerator(this)
        genertor.setContentView(view)
        genertor.setBackground(ColorDrawable(Color.TRANSPARENT))
        val icon = genertor.makeIcon()
        destinationMarker = mMap.addMarker(
            MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectedPlaceEvent!!.destination)
        )


    }

    private fun addOrginMarker(duration: String, startAdress: String) {
        val view = layoutInflater.inflate(R.layout.orgin_info, null)
        val txt_time = view.findViewById<View>(R.id.txt_time) as TextView

        txt_orgin = view.findViewById<View>(R.id.txt_orgin) as TextView

        txt_time.text = Commmon.formatDuration(duration)
        txt_orgin.text = Commmon.formatMode(startAdress)

        val genertor = IconGenerator(this)
        genertor.setContentView(view)
        genertor.setBackground(ColorDrawable(Color.TRANSPARENT))
        val icon = genertor.makeIcon()
        orginmarker = mMap.addMarker(
            MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectedPlaceEvent!!.orgin)
        )


    }
}