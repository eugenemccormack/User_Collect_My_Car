package com.example.user_collect_my_car.Remote

import com.example.user_collect_my_car.Model.FCMResponse
import com.example.user_collect_my_car.Model.FCMSendData
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMService {

    @Headers(
            "Content-Type:application/json",
            "Authorization:key=AAAAqG__Ve4:APA91bEr6ZRDxDcMVzpTJHzR5dmsTqxK5UESnaIwC7k_-whGzvrDvvct3rYkSctVCMoZBP6OKYAk2lV2pa0tY2SvrNzd1XsMXhoUo1j0stfXcYAhbeZrW-PDzwc2pCxIpj0tNfDXtCvd"

    )

    @POST("fcm/send")
    fun sendNotification(@Body body: FCMSendData?):Observable<FCMResponse?>?
}