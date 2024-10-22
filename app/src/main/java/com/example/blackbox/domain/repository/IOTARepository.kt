package com.example.blackbox.domain.repository

import com.example.blackbox.data.remote.dto.BlockId
import com.example.blackbox.data.remote.dto.FullBlockWithTaggedDataPayload
import com.example.blackbox.data.remote.dto.NodeInfo
import com.example.blackbox.data.remote.dto.Routes
import com.example.blackbox.data.remote.dto.TaggedDataBlock
import com.example.blackbox.data.remote.dto.Tips

interface IOTARepository {
    suspend fun sendData(data: FullBlockWithTaggedDataPayload): BlockId
    suspend fun getData(blockId: String): TaggedDataBlock
    suspend fun getTips(): Tips
    suspend fun getInfo(): NodeInfo
    suspend fun getRoutes(): Routes
}