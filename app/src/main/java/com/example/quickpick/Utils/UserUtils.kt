package com.example.quickpick.Utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.example.quickpick.Commmon
import com.example.quickpick.Model.DriverModel
import com.example.quickpick.Model.FCMSendData
import com.example.quickpick.Model.TokenModel
import com.example.quickpick.R
import com.example.quickpick.Remote.IFCM
import com.example.quickpick.Remote.IFCMService
import com.example.quickpick.Remote.RetroFitClient
import com.example.quickpick.Services.MyFirebaseMessagingSertives
import com.google.android.gms.common.internal.service.Common
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.StringBuilder

object UserUtils {
    fun updateToken(context: Context, token: String) {
        val tokenModel = TokenModel()
        tokenModel.token = token

        FirebaseDatabase.getInstance()
            .getReference(Commmon.TOKEN_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .setValue(token)
            .addOnFailureListener { e ->
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }

            .addOnSuccessListener {


            }

    }

    fun sendRequestToDriver(
        context: Context,
        aminLayout: RelativeLayout?,
        foundriver: DriverModel?,
        target: LatLng
    ) {
        val compositeDisposable = CompositeDisposable()
        val ifcmservice = IFCMService.instance!!.create(IFCM::class.java)

        //ghet token
        FirebaseDatabase.getInstance().getReference(Commmon.TOKEN_REFERENCE)
            .child(foundriver!!.key!!)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
if (snapshot.exists()){
    val  tokenModel=snapshot.getValue(TokenModel::class.java)
    val notificationData:MutableMap<String,String> = HashMap()
    notificationData.put(Commmon.NOTI_TITLE,Commmon.REQUEST_DRIVER_TITLE)
    notificationData.put(Commmon.NOTI_BODY,"This Messge Represent You got a driver")
notificationData.put(Commmon.RIDER_KEY,FirebaseAuth.getInstance().currentUser!!.uid)

    notificationData.put(Commmon.PICK_UP_LOCATION,StringBuilder()
    .append(target.latitude)
    .append(",")
    .append(target.longitude)
    .toString()
)

    val fsmSendData=FCMSendData(tokenModel!!.token,notificationData)
    compositeDisposable.add(ifcmservice.sendNotification(fsmSendData)!!
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread()

        ).subscribe({fcmResponse->
            if (fcmResponse!!.sucess==0){
                compositeDisposable.clear()
                Snackbar.make(aminLayout!!,context.getString(R.string.send_request_driver_found),Snackbar.LENGTH_LONG).show()
            }

        },{t:Throwable?->


compositeDisposable.clear()
            Snackbar.make(aminLayout!!,t!!.message!!,Snackbar.LENGTH_LONG).show()

    }))

}

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }

    fun sendDeclineRequest(rootlayout: View, activity: Activity, key: String) {
        val compositeDisposable = CompositeDisposable()
        val ifcmservice = IFCMService.instance!!.create(IFCM::class.java)

        FirebaseDatabase.getInstance().getReference(Commmon.TOKEN_REFERENCE)
            .child(key)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val  tokenModel=snapshot.getValue(TokenModel::class.java)
                        val notificationData:MutableMap<String,String> = HashMap()
                        notificationData.put(Commmon.NOTI_TITLE,Commmon.REQUEST_DRIVER_DECLINE)
                        notificationData.put(Commmon.NOTI_BODY,"This Messge represent for decline action for Driver")
                        notificationData.put(Commmon.DRIVER_KEY,FirebaseAuth.getInstance().currentUser!!.uid)

//                        notificationData.put(Commmon.PICK_UP_LOCATION,StringBuilder()
//                            .append(target.latitude)
//                            .append(",")
//                            .append(target.longitude)
//                            .toString()
//                        )

                        val fsmSendData=FCMSendData(tokenModel!!.token,notificationData)

                        compositeDisposable.add(ifcmservice.sendNotification(fsmSendData)!!
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread()

                            ).subscribe({fcmResponse->
                                if (fcmResponse!!.sucess==0){
                                    compositeDisposable.clear()
                                    Snackbar.make(rootlayout,activity.getString(R.string.decline_failed),Snackbar.LENGTH_LONG).show()
                                }
                                else{
                                    Snackbar.make(rootlayout,activity.getString(R.string.decline_Sucess),Snackbar.LENGTH_LONG).show()

                                }

                            },{t:Throwable?->


                                compositeDisposable.clear()
                                Snackbar.make(rootlayout,t!!.message!!,Snackbar.LENGTH_LONG).show()

                            }))

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })



    }


}