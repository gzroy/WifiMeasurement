package com.example.wifimeasurement

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitAPI {
    @POST("senddata")
    fun postData(@Body data: WifiMeasureReportData?): Call<WifiMeasureReportData?>?
}