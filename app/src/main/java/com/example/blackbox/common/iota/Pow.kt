package com.example.blackbox.common.iota

import android.util.Log
import com.appmattus.crypto.Algorithm
import com.example.blackbox.data.remote.dto.FullBlockWithTaggedDataPayload
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val HASH_LENGTH = 243 // Lunghezza dell'hash per Curl-P-81

class Pow : IotaLocalPow {

    override fun performPow(block: FullBlockWithTaggedDataPayload): ULong {
        // Compute the BLAKE2b-256 hash of the serialized message excluding the 8-byte Nonce field
        val serializedMessage = Json.encodeToString(block).toByteArray(Charsets.UTF_8)
        val powDigest = blake2b(serializedMessage)
        // and convert the hash into its 192-trit b1t6 encoding.
        val powDigestTrits = b1t6Encode(powDigest)
        testPow()
        var nonce = 0uL
        val curl = JCurl()

        while (true) {
            val nonceBites = nonceToBytes(nonce)
            // Take the 8-byte Nonce in little-endian representation, convert it into its 48-trit b1t6 encoding and append it to the hash trits.
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

            if (score >= 1500) {
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

    fun nonceToBytes(nonce: ULong): ByteArray {
        return ByteArray(8) { i ->
            (nonce shr (i * 8)).toByte()
        }
    }

    fun blake2b(data: ByteArray): ByteArray {
        val digest = Algorithm.Blake2b_256.createDigest()
        digest.update(data)
        val result: ByteArray = digest.digest()
        return result
    }

    // Implementazione della codifica b1t6
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

    @OptIn(ExperimentalStdlibApi::class)
    fun testPow() {
        val message = "48656c6c6f2c20576f726c64215ee6aaaaaaaaaaaa".hexToByteArray()
        val messageWithoutNonce = "48656c6c6f2c20576f726c6421".hexToByteArray()
        val powDigest = blake2b(messageWithoutNonce)
        Log.d("Test", "Pow digest: ${powDigest.toHexString()}")

        val nonce = 12297829382473049694uL
        val nonceBytes = nonceToBytes(nonce)
        Log.d("Test", "Nonce bytes: ${nonceBytes.toHexString()} ${nonceBytes.size}")
        val nonceTrits = b1t6Encode(nonceBytes)
        val powDigestTrits = b1t6Encode(powDigest)
        val inputTrits = powDigestTrits + nonceTrits + byteArrayOf(0, 0, 0)
        Log.d("Test", "inputTrits: ${inputTrits.contentToString()} ${inputTrits.size} =\n ${powDigest.contentToString()} +\n ${nonceBytes.contentToString()}")
        val powDigestTrytes = b1t6EncodeToTrytes(powDigest)
        val nonceTrytes = b1t6EncodeToTrytes(nonceBytes)
        val inputTrytes = b1t6EncodeToTrytes(inputTrits)
        Log.d("Test", "Pow digest trytes: $powDigestTrytes ${powDigestTrytes.length}")
        Log.d("Test", "Nonce trytes: $nonceTrytes ${nonceTrytes.length}")
        Log.d("Test", "Input trytes: ${inputTrytes} ${inputTrytes.length}")
        val curl = JCurl()
        val b1t6 = B1t6()
        curl.absorb(inputTrits)
        val powHash = ByteArray(HASH_LENGTH)
        curl.squeeze(powHash)
        val trailingZeros = countTrailingZeros(powHash)
        Log.d("Test", "Zeros: $trailingZeros")
        Log.d("Test", "Message size: ${message.size}")
        val score = Math.pow(3.0, trailingZeros.toDouble()) / message.size
        Log.d("Test", "Score: $score")
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun testEncode() {
        val b1t6 = B1t6()
        val hex1 = "00".hexToByteArray()
        val hex2 = "0001027e7f8081fdfeff".hexToByteArray()
        val hex3 = "9ba06c78552776a596dfe360cc2b5bf644c0f9d343a10e2e71debecd30730d03".hexToByteArray()
        val es1Trits = b1t6Encode(hex1)
        val es2Trits = b1t6Encode(hex2)
        val es3Trits = b1t6Encode(hex3)
        val es1Decoded = ByteArray(b1t6.decodeLen(es1Trits.size))
        b1t6.decode(es1Decoded, es1Trits)
        val es2Decoded = ByteArray(b1t6.decodeLen(es2Trits.size))
        b1t6.decode(es2Decoded, es2Trits)
        val es3Decoded = ByteArray(b1t6.decodeLen(es3Trits.size))
        b1t6.decode(es3Decoded, es3Trits)
        val es1Trytes = b1t6EncodeToTrytes(es1Decoded)
        val es2Trytes = b1t6EncodeToTrytes(es2Decoded)
        val es3Trytes = b1t6EncodeToTrytes(es3Decoded)
        Log.d("Test", "1 -> ${hex1.contentToString()} ${hex1.size} ${es1Trits.size} ${es1Trits.contentToString()} $es1Trytes")
        Log.d("Test", "2 -> ${hex2.contentToString()} ${hex2.size} ${es2Trits.size} ${es2Trits.contentToString()} $es2Trytes")
        Log.d("Test", "3 -> ${hex3.contentToString()} ${hex3.size} ${es3Trits.size} ${es3Trits.contentToString()} $es3Trytes")

    }
}