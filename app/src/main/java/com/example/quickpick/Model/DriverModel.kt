package com.example.quickpick.Model

import com.firebase.geofire.GeoLocation

class DriverModel(var key: String?, geolocation: GeoLocation?) {


    var geolocation: GeoLocation? = null
    var isDecline:Boolean=false
    var driverInfoModel:QuickpickdataModel?=null

    init {
        this.geolocation=geolocation!!
    }



}