package com.example.quickpick.Services

import com.example.quickpick.Commmon
import com.example.quickpick.Model.DriverRequestReceived
import com.example.quickpick.Utils.UserUtils
import com.google.android.gms.common.internal.service.Common
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.greenrobot.eventbus.EventBus
import kotlin.random.Random

class MyFirebaseMessagingSertives : FirebaseMessagingService() {


    override fun onNewToken(token: String) {
        super.onNewToken(token)

        if (FirebaseAuth.getInstance().currentUser != null) {
            UserUtils.updateToken(this, token)
        }


    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        if (data != null) {
            if (data[Commmon.NOTI_TITLE].equals(Commmon.REQUEST_DRIVER_TITLE)) {
EventBus.getDefault()
    .postSticky(
        DriverRequestReceived(data[Commmon.RIDER_KEY]!!,
    data[Commmon.PICK_UP_LOCATION]!!
        )
    )

            } else

                Commmon.shownotification(
                    this, Random.nextInt(), data[Commmon.NOTI_TITLE],
                    data[Commmon.NOTI_BODY], null
                )


        }


    }


}