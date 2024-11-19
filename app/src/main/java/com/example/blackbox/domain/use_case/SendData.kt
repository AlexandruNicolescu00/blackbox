package com.example.blackbox.domain.use_case

import android.util.Log
import com.example.blackbox.common.Resource
import com.example.blackbox.common.iota.Pow
import com.example.blackbox.data.remote.dto.FullBlockWithTaggedDataPayload
import com.example.blackbox.data.remote.dto.Payload
import com.example.blackbox.domain.repository.IOTARepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SendData @Inject constructor(
    private val repository: IOTARepository
) {
    operator fun invoke(data: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            var block = FullBlockWithTaggedDataPayload(
                parents = repository.getTips().tips,
                protocolVersion = 2,
                payload = Payload(
                    type = 5,
                    tag = "0x68656c6c6f",//BLOCK_TAG.toHexWithPrefix(),
                    data = "0x54686973206973206120746573742064617461"//data.toHexWithPrefix()
                ),
                nonce = ""
            )

            val nonce = Pow().performPow(block)
            Log.d("Nonce", "Nonce: $nonce")
            Log.d("Nonce", "Parents: ${block.parents}")
            block = block.copy(nonce = nonce.toString())

            val blockId = repository.sendData(block).blockId
            emit(Resource.Success(blockId))
        } catch (e: HttpException) {
            emit(Resource.Error(message = "${e.code()}: ${e.message}"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach any node. Check your internet connection"))
        }
    }
}

fun String.toHexWithPrefix(): String {
    val hexString = this.encodeToByteArray().joinToString(separator = "") {
        String.format("%02x", it)
    }
    return "0x$hexString"
}
