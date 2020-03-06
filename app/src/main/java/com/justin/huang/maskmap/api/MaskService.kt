package com.justin.huang.maskmap.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface MaskService {
    @GET("points.json")
    fun getMaskData(): Call<ResponseBody>
}