package com.example.quickpick

import android.R.attr
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.quickpick.Model.AnimationModel
import com.example.quickpick.Model.DriverModel
import com.example.quickpick.Model.QuickpickdataModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker


object Commmon {

    val driverSubscribe: MutableMap<String, AnimationModel> =HashMap<String,AnimationModel>()
    val markerlist:MutableMap<String, Marker> =HashMap<String, Marker>()
    val driverfound: MutableSet<DriverModel> =HashSet<DriverModel>()
    val TOKEN_REFERENCE: String="Token"
    var currentuser: QuickpickdataModel?=null
     var DRIVER_INFO_REFERENCE:String="Qucikowner";
    val DRIVER_LOCATION_REFERENCE:String="DriverLocation"

    fun shownotification(context: Context, id: Int, title: String?, body: String?, intent: Intent?){
        var pendingIntentt:PendingIntent?=null
        if (intent!=null){
            pendingIntentt= PendingIntent.getActivity(
                context,
                id,
                intent!!,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val NOTIFIACTION_CHANNEL_ID="Quick_PICK"
            val notificaManager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            {
                val notificationChannel=NotificationChannel(
                    NOTIFIACTION_CHANNEL_ID, "QuickPick",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationChannel.description="Quick Pick"
                notificationChannel.enableLights(true)
                notificationChannel.lightColor=Color.RED
                notificationChannel.vibrationPattern= longArrayOf(0, 1000, 500, 1000)
                notificationChannel.enableVibration(true)
                notificaManager.createNotificationChannel(notificationChannel)
            }

            val builder=NotificationCompat.Builder(context, NOTIFIACTION_CHANNEL_ID)
            builder.setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.ic_baseline_directions_car_24)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.ic_baseline_directions_car_24
                    )
                )

            if (pendingIntentt!=null){
                builder.setContentIntent(pendingIntentt)
                val notification=builder.build()
                notificaManager.notify(id, notification)



            }



        }


    }

    fun buildname(username: String): String? {

        return java.lang.StringBuilder(username).toString()



    }

    val NOTI_BODY="body"
    val NOTI_TITLE="title"

 fun decodepoly(encoded: String): ArrayList<LatLng?> {
     val poly= ArrayList<LatLng?>()
     var index = 0
     val len: Int = encoded.length
     var lat = 0
     var lng = 0
     while (index < len) {
         var b: Int
         var shift = 0
         var result = 0
         do {
             b = encoded[index++].toInt() - 63
             result = result or (b and 0x1f shl shift)
             shift += 5
         } while (b >= 0x20)
         val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
         lat += dlat
         shift = 0
         result = 0
         do {
             b = encoded[index++].toInt()- 63
             result = result or (b and 0x1f shl shift)
             shift += 5
         } while (b >= 0x20)
         val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
         lng += dlng
         val p = LatLng(
             lat.toDouble() / 1E5,
             lng.toDouble() / 1E5
         )
         poly.add(p)
     }
     return poly
 }


    fun getBearing(begin: LatLng, end: LatLng):Float{

        val lat: Double = Math.abs(begin.latitude - end.latitude)
        val lng: Double = Math.abs(begin.longitude - end.longitude)

        if (begin.latitude < end.latitude && begin.longitude < end.longitude) return Math.toDegrees(
            Math.atan(lng / lat)
        )
            .toFloat() else if (begin.latitude >= end.latitude && begin.longitude < end.longitude) return (90 - Math.toDegrees(
            Math.atan(lng / lat)
        ) + 90).toFloat() else if (begin.latitude >=end.latitude && begin.longitude >= end.longitude) return (Math.toDegrees(
            Math.atan(lng / lat)
        ) + 180).toFloat() else if (begin.latitude < end.latitude && begin.longitude >= end.longitude) return (90 - Math.toDegrees(
            Math.atan(lng / lat)
        ) + 270).toFloat()
        return (-1).toFloat()
    }



}
