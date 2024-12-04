package com.example.blackbox.domain.use_case

import android.util.Log
import com.example.blackbox.common.BLOCK_TAG
import com.example.blackbox.common.iota.Pow
import com.example.blackbox.data.remote.dto.FullBlockWithTaggedDataPayload
import com.example.blackbox.data.remote.dto.Payload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class EvaluationEventsNonce {
    operator fun invoke() {
        val sample = listOf(
            "FOREGROUND SERVICE START by com.google.android.dialer at 12:55:49",
            "FOREGROUND SERVICE STOP by com.google.android.dialer at 12:55:52",
            "APP CHANGED by com.google.android.apps.maps at 11:35:16",
            "NOTIFICATION ARRIVED by com.google.android.dialer at 11:35:23",
            "APP CHANGED by com.google.android.projection.gearhead at 11:26:31",
            "NOTIFICATION ARRIVED by com.whatsapp at 11:55:23"
        )

        runBlocking {
            val results = (0..100).map { numberOfEvents ->
                async(Dispatchers.Default) {
                    val events = buildString {
                        repeat(numberOfEvents) {
                            val random = Random.nextInt(sample.size)
                            append(sample[random]).append("\n")
                        }
                    }

                    val block = FullBlockWithTaggedDataPayload(
                        protocolVersion = 2,
                        parents = listOf(
                            "0x0a0594654f215c2c5597f878a7360654eee2b03ae2210f1d5689c0978a63a802",
                            "0x7a9722c1a33bba345500e20f32dd5d31f7b8d7327da096681af769d5169af480",
                            "0x861bb0fa268e9b1617da1414627c7b3fb20bf857a6544447fc02d42798ce922c",
                            "0xed65b0dac47b7f015d08eb342f7bd49b89e7a3d523c67130a7baf5c0f0c052b9"
                        ),
                        payload = Payload(
                            type = 5,
                            tag = BLOCK_TAG,
                            data = events
                        ),
                        nonce = ""
                    )

                    val nonce = Pow().performPow(block)
                    Log.d("testEvaluation", "$numberOfEvents - $nonce len: ${events.length}")
                    "$numberOfEvents - $nonce len: ${events.length}"
                }
            }

            val output = results.awaitAll().joinToString("\n")
            Log.i("EvaluationEventsNonce", output)
        }
    }
}