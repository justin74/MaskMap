package com.justin.huang.maskmap.api

import com.justin.huang.maskmap.data.MaskPoints
import retrofit2.Call
import retrofit2.http.GET

interface MaskApiService {
    @GET("points.json")
    suspend fun getMaskPoints(): MaskPoints
}