package com.example.quickpick.EventBus

import com.google.android.gms.maps.model.LatLng
import java.lang.StringBuilder

class SelelectedPlaceEvent (var orgin:LatLng,var destination:LatLng){
    val orginString:String
    get()=StringBuilder()
        .append(orgin.latitude)
        .append(",")
        .append(orgin.longitude)
        .toString()
    val destinationString:String
    get()=StringBuilder()
        .append(destination.latitude)
        .append(",")
        .append(destination.longitude)
        .toString()



}