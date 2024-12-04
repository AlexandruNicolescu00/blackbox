package com.example.blackbox.common.iota

import android.util.Log
import com.appmattus.crypto.Algorithm
import com.example.blackbox.data.remote.dto.FullBlockWithTaggedDataPayload
import kotlin.collections.plus

const val HASH_LENGTH = 243 // Hash length for Curl-P-81
const val MIN_POW_SCORE = 1500 // Minimum PoW score for a valid PoW

class Pow : IotaLocalPow {

    @OptIn(ExperimentalStdlibApi::class)
    override fun performPow(block: FullBlockWithTaggedDataPayload): ULong {
        // Compute the BLAKE2b-256 hash of the serialized message excluding the 8-byte Nonce field
        val serializedMessage = serializeMessage(block)
        val powDigest = blake2b(serializedMessage)
        val startTime = System.currentTimeMillis()
        // and convert the hash into its 192-trit b1t6 encoding.
        val powDigestTrits = b1t6Encode(powDigest)
        val curl = JCurl()
        var nonce = 0uL
        while (true) {
            val nonceBites = nonce.toLittleEndianByteArray(8)
            // Take the 8-byte Nonce in little-endian representation,
            // convert it into its 48-trit b1t6 encoding and append it to the hash trits.
            val nonceTrits = b1t6Encode(nonceBites)
            // Add a padding of three zero trits to create a 243-trit string.
            val inputTrits = powDigestTrits + nonceTrits + byteArrayOf(0, 0, 0)
            // Compute the Curl-P-81 hash.
            curl.absorb(inputTrits)
            val powHash = ByteArray(HASH_LENGTH)
            curl.squeeze(powHash)
            // Count the number of trailing zero trits in the hash.
            val trailingZeros = countTrailingZeros(powHash)
            // Then, the PoW score equals 3#zeros / size(message).
            val message = serializedMessage + nonceBites
            val score = Math.pow(3.0, trailingZeros.toDouble()) / message.size

            if (score >= MIN_POW_SCORE) {
                val duration = System.currentTimeMillis() - startTime
                Log.d("Time", "nonce $nonce in $duration ms")
                Log.d("Nonce", "Score: $score")
                return nonce
            }
            nonce++
            curl.reset()
        }
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