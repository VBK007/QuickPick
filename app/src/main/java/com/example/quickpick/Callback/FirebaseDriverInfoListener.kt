package com.example.quickpick.Callback

import com.example.quickpick.Model.DriverModel

interface FirebaseDriverInfoListener {

    fun onDriverInfoloadedSucess(driverModel: DriverModel?)

}