package com.example.blackbox.domain.use_case

import android.util.Log
import com.example.blackbox.common.BLOCK_TAG
import com.example.blackbox.common.iota.ParallelPow
import com.example.blackbox.common.iota.Pow
import com.example.blackbox.data.remote.dto.FullBlockWithTaggedDataPayload
import com.example.blackbox.data.remote.dto.Payload
import com.example.blackbox.domain.repository.IOTARepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import retrofit2.HttpException
import java.io.IOException

class EvaluationCongestionDelay (
    private val repository: IOTARepository
){
    suspend operator fun invoke() = coroutineScope {
        val semaphore = Semaphore(5) // Limita a 5 istanze parallele
        val results = (1..10).map { i ->
            async(Dispatchers.IO) {
                semaphore.acquire() // Blocca l'accesso se ci sono già 5 coroutine attive
                try {
                    sendBlockWithRetry(i)
                } finally {
                    semaphore.release() // Rilascia il semaforo una volta completata
                }
            }
        }

        // Aspetta il completamento di tutte le coroutine
        val output = results.awaitAll().joinToString("\n")
        Log.i("EvaluationCongestionDelay", output)
    }


    private suspend fun sendBlockWithRetry(index: Int): String {
        Log.d("testEvaluation", "Sending block $index...")
        var success = false
        val data = generateRandomData()
        val foundNonces = mutableListOf<ULong>()
        while (!success) {
            try {
                var block = FullBlockWithTaggedDataPayload(
                    protocolVersion = 2,
                    parents = repository.getTips().tips,
                    payload = Payload(
                        type = 5,
                        tag = BLOCK_TAG,
                        data = data
                    ),
                    nonce = ""
                )

                val nonce = Pow().performPow(block)

                foundNonces.add(nonce)
                block = block.copy(
                    nonce = nonce.toString(),
                    payload = Payload(
                        type = 5,
                        tag = BLOCK_TAG.toHexWithPrefix(),
                        data = data.toHexWithPrefix()
                    )
                )

                val blockId = repository.sendData(block).blockId
                success = true
                Log.d("testEvaluation", "Block $index - $nonce - $foundNonces - $blockId")
                return "Block $index - $nonce - $foundNonces - $blockId"
            } catch (e: HttpException) {
                println("HTTP Exception for Block $index: ${e.message}. Retrying...")
            } catch (e: IOException) {
                println("Network error for Block $index: ${e.message}. Retrying...")
            }
        }
        return "Never happens"
    }

    private fun generateRandomData(): String {
        val sample = listOf(
            "FOREGROUND SERVICE START by com.google.android.dialer at 12:55:49",
            "FOREGROUND SERVICE STOP by com.google.android.dialer at 12:55:52",
            "APP CHANGED by com.google.android.apps.maps at 11:35:16",
            "NOTIFICATION ARRIVED by com.google.android.dialer at 11:35:23",
            "APP CHANGED by com.google.android.projection.gearhead at 11:26:31",
            "NOTIFICATION ARRIVED by com.whatsapp at 11:55:23",
            "APP CHANGED by com.google.android.googlequicksearchbox at 10:31:53",
            "FOREGROUND SERVICE START by com.whatsapp at 09:55:29",
            "FOREGROUND SERVICE STOP by com.whatsapp at 09:55:42",
        )
        val numberOfEvents = (2..10).random()
        var output = ""

        repeat(numberOfEvents) {
            output += sample.random() + "\n"
        }

        return output
    }
}