package com.example.blackbox.domain.use_case

import android.util.Log
import com.example.blackbox.common.Resource
import com.example.blackbox.data.remote.dto.FullBlockWithTaggedDataPayload
import com.example.blackbox.data.remote.dto.Payload
import com.example.blackbox.domain.repository.IOTARepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class SendData @Inject constructor(
    private val repository: IOTARepository
) {
    operator fun invoke(data: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            val block = FullBlockWithTaggedDataPayload(
                parents = repository.getTips().tips,
                protocolVersion = 2,
                payload = Payload(
                    type = 5,
                    tag = "0x68656c6c6f20776f726c64",//BLOCK_TAG.toHexWithPrefix(),
                    data = "0x5370616d6d696e6720646174612e0a436f756e743a203037323935320a54696d657374616d703a20323032312d30322d31315431303a32333a34392b30313a30300a54697073656c656374696f6e3a203934c2b573"//data.toHexWithPrefix()
                ),
                nonce = "0x2102864"
            )
            val blockId = repository.sendData(block).blockId
            emit(Resource.Success(blockId))
        } catch (e: HttpException) {
            emit(Resource.Error(message = "${e.code()}: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach ani node. Check your internet connection"))
        }
    }
}

fun String.toHexWithPrefix(): String {
    val hexString = this.encodeToByteArray().joinToString(separator = "") {
        String.format("%02x", it)
    }
    return "0x$hexString"
}
