package com.example.quickpick.Model

import com.firebase.geofire.GeoLocation

class DriverModel {


    var key: String? = null
    var geolocation: GeoLocation? = null
    var isDecline:Boolean=false
    var driverInfoModel:QuickpickdataModel?=null

    constructor(key:String?,geolocation:GeoLocation?){
        this.key=key
        this.geolocation=geolocation!!
    }



}