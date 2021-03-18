package com.example.quickpick.Remote

import com.example.quickpick.Model.FCMResponse
import com.example.quickpick.Model.FCMSendData
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.*

interface IFCM {
    //AAAAYkZRl30:APA91bFp2-W1t_7tOfRKPQ4sV6QCAm6v8TdKpXOXgQZFnNClckDj7Q45-wVmzhWFKyCn0VVxrq3pThGZ3b6vvnb6eHp8vmrF7ZX9pdWE4IVPhYtbSkXSpqzCRy4M4xyrYgWOduLxCm1A
    @Headers(
        "content-type:application/json",
        "Authorization:key=AAAAYkZRl30:APA91bFp2-W1t_7tOfRKPQ4sV6QCAm6v8TdKpXOXgQZFnNClckDj7Q45-wVmzhWFKyCn0VVxrq3pThGZ3b6vvnb6eHp8vmrF7ZX9pdWE4IVPhYtbSkXSpqzCRy4M4xyrYgWOduLxCm1A"

    )

    @POST("fcm/send")
    fun sendNotification(@Body body: FCMSendData?):Observable<FCMResponse?>?

}