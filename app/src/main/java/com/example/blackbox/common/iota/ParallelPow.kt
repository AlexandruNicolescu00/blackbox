package com.example.blackbox.common.iota

import android.util.Log
import com.appmattus.crypto.Algorithm
import com.example.blackbox.data.remote.dto.FullBlockWithTaggedDataPayload
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class ParallelPow {

    @Volatile
    private var nonceCounter = 0uL

     suspend fun performParallelPow(block: FullBlockWithTaggedDataPayload): ULong = coroutineScope {
        val serializedMessage = serializeMessage(block)
        val powDigest = blake2b(serializedMessage)
        val powDigestTrits = b1t6Encode(powDigest)
        val curlFactory = { JCurl() }
        val maxCoroutines = 6
        val semaphore = Semaphore(maxCoroutines)

        val resultChannel = CompletableDeferred<ULong>()

        repeat(maxCoroutines) {
            launch {
                semaphore.withPermit {
                    val curl = curlFactory()
                    while (isActive) {
                        val nonce = getNextNonce()
                        val nonceBites = nonce.toLittleEndianByteArray(8)
                        val nonceTrits = b1t6Encode(nonceBites)
                        val inputTrits = powDigestTrits + nonceTrits + byteArrayOf(0, 0, 0)

                        curl.absorb(inputTrits)
                        val powHash = ByteArray(HASH_LENGTH)
                        curl.squeeze(powHash)
                        val trailingZeros = countTrailingZeros(powHash)

                        val message = serializedMessage + nonceBites
                        val score = Math.pow(3.0, trailingZeros.toDouble()) / message.size

                        if (score >= MIN_POW_SCORE) {
                            resultChannel.complete(nonce)
                            break
                        }
                        curl.reset()
                    }
                }
            }
        }

        try {
            resultChannel.await()
        } finally {
            coroutineContext.cancelChildren()
        }
    }

    @Synchronized
    private fun getNextNonce(): ULong {
        val current = nonceCounter
        nonceCounter++
        return current
    }


    fun countTrailingZeros(trits: Trits): Int {
        var zeros = 0
        var index = trits.size - 1
        while (index >= 0 && trits[index] == 0.toByte()) {
            zeros = zeros + 1
            index = index - 1
        }
        return zeros
    }

    fun blake2b(data: ByteArray): ByteArray {
        val digest = Algorithm.Blake2b_256.createDigest()
        digest.update(data)
        val result: ByteArray = digest.digest()
        return result
    }

    fun b1t6Encode(byteArray: ByteArray): Trits {
        val b1t6 = B1t6()
        val trits: Trits = ByteArray(b1t6.encodeLen(byteArray.size))
        b1t6.encode(trits, byteArray)
        return trits
    }

    fun b1t6EncodeToTrytes(trits: Trits): Trytes {
        val b1t6 = B1t6()
        return b1t6.encodeToTrytes(trits)
    }
}