package com.example.quickpick.Model

import android.os.Handler
import com.google.android.gms.maps.model.LatLng

class AnimationModel(var isRun:Boolean, var geoqueyModel: GeoqueryModel?)
{
    var polylinelist: ArrayList<LatLng?>? = null
    var handler: Handler
    var index = 0
    var next: Int = 0
    var v: Float = 0.0f
    var lat: Double = 0.0
    var lng: Double = 0.0
    var start: LatLng? = null
    var end: LatLng? = null




    init {
        handler=Handler()
    }




}