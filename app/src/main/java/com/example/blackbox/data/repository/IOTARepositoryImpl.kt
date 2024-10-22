package com.example.blackbox.data.repository

import com.example.blackbox.data.remote.IOTAApi
import com.example.blackbox.data.remote.dto.BlockId
import com.example.blackbox.data.remote.dto.FullBlockWithTaggedDataPayload
import com.example.blackbox.data.remote.dto.NodeInfo
import com.example.blackbox.data.remote.dto.Routes
import com.example.blackbox.data.remote.dto.TaggedDataBlock
import com.example.blackbox.data.remote.dto.Tips
import com.example.blackbox.domain.repository.IOTARepository
import javax.inject.Inject

class IOTARepositoryImpl @Inject constructor(
    private val api: IOTAApi
) : IOTARepository {
    override suspend fun sendData(data: FullBlockWithTaggedDataPayload): BlockId {
        return api.sendData(data)
    }

    override suspend fun getData(blockId: String): TaggedDataBlock {
        return api.getData(blockId)
    }

    override suspend fun getTips(): Tips {
        return api.getTips()
    }

    override suspend fun getInfo(): NodeInfo {
        return api.getInfo()
    }

    override suspend fun getRoutes(): Routes {
        return api.getApiRoutes()
    }
}