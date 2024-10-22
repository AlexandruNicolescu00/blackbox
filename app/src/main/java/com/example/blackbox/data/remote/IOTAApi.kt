package com.example.blackbox.data.remote

import com.example.blackbox.data.remote.dto.BlockId
import com.example.blackbox.data.remote.dto.FullBlockWithTaggedDataPayload
import com.example.blackbox.data.remote.dto.NodeInfo
import com.example.blackbox.data.remote.dto.Routes
import com.example.blackbox.data.remote.dto.TaggedDataBlock
import com.example.blackbox.data.remote.dto.Tips
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface IOTAApi {

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("/api/core/v2/blocks")
    suspend fun sendData(@Body body: FullBlockWithTaggedDataPayload): BlockId

    @Headers("Accept: application/json")
    @GET("/api/core/v2/blocks/{blockId}")
    suspend fun getData(@Path("blockId") blockId: String): TaggedDataBlock

    @Headers("Accept: application/json")
    @GET("/api/core/v2/tips")
    suspend fun getTips(): Tips

    @Headers("Accept: application/json")
    @GET("/api/core/v2/info")
    suspend fun getInfo(): NodeInfo

    @Headers("Accept: application/json")
    @GET("/api/routes")
    suspend fun getApiRoutes(): Routes
}