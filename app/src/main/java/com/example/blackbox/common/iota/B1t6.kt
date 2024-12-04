package com.example.blackbox.common.iota

import android.util.Log


class B1t6 {

    companion object {
        // TryteValueToTritsLUT is a lookup table to convert tryte values into trits.
        val TRYTE_VALUE_TO_TRITS_LUT = arrayOf(
            byteArrayOf(-1, -1, -1), byteArrayOf(0, -1, -1), byteArrayOf(1, -1, -1),
            byteArrayOf(-1, 0, -1), byteArrayOf(0, 0, -1), byteArrayOf(1, 0, -1),
            byteArrayOf(-1, 1, -1), byteArrayOf(0, 1, -1), byteArrayOf(1, 1, -1),
            byteArrayOf(-1, -1, 0), byteArrayOf(0, -1, 0), byteArrayOf(1, -1, 0),
            byteArrayOf(-1, 0, 0), byteArrayOf(0, 0, 0), byteArrayOf(1, 0, 0),
            byteArrayOf(-1, 1, 0), byteArrayOf(0, 1, 0), byteArrayOf(1, 1, 0),
            byteArrayOf(-1, -1, 1), byteArrayOf(0, -1, 1), byteArrayOf(1, -1, 1),
            byteArrayOf(-1, 0, 1), byteArrayOf(0, 0, 1), byteArrayOf(1, 0, 1),
            byteArrayOf(-1, 1, 1), byteArrayOf(0, 1, 1), byteArrayOf(1, 1, 1)
        )
        val TRYTE_VALUE_TO_TRYTE_LUT = charArrayOf(
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M'
        )
    }

    // EncodedLen returns the trit-length of an encoding of n source bytes.
    fun encodeLen(len: Int): Int {
        return len * TRITS_PER_BYTE
    }

    // MustPutTryteTrits converts value in [-13,13] to its corresponding 3-trit value and writes this to trits.
    private fun mustPutTryteTrits(trits: Trits, offset: Int, value: Byte) {
        val idx = value - MIN_TRYTE_VALUE
        require(trits.size >= 3) { "The 'trits' array must have at least 3 elements." }
        trits[offset] = TRYTE_VALUE_TO_TRITS_LUT[idx][0]
        trits[offset + 1] = TRYTE_VALUE_TO_TRITS_LUT[idx][1]
        trits[offset + 2] = TRYTE_VALUE_TO_TRITS_LUT[idx][2]
    }

    // Encode encodes src into EncodedLen(len(in)) trits of dst. As a convenience, it returns the number of trits written,
    // but this value is always EncodedLen(len(src)).
    // Encode implements the b1t6 encoding converting a bit string into ternary.
    fun encode(destination: Trits, source: ByteArray): Int {
        var numberOfTrits = 0
        for (i in source.indices) {
            val (t1, t2) = encodeGroup(source[i])
            mustPutTryteTrits(destination, numberOfTrits, t1)
            mustPutTryteTrits(destination, numberOfTrits + TRITS_PER_TRYTE, t2)
            numberOfTrits += 6
        }
        return  numberOfTrits
    }

    // MustTritsToTryteValue converts a slice of 3 into its corresponding value.
    // It performs no validation on the provided inputs (therefore might return an invalid representation)
    private fun mustTritsToTryteValue(trits: Trits): Byte {
        if (trits.size != 3) {
            throw IllegalArgumentException("Expected exactly 3 trits")
        }

        return (trits[0] + trits[1] * 3 + trits[2] * 9).toByte()
    }

    fun decodeLen(len: Int): Int {
        return len / TRITS_PER_BYTE
    }

    // Decode decodes src into DecodedLen(len(in)) bytes of dst and returns the actual number of bytes written.
    // Decode expects that src contains a valid b1t6 encoding and that src has a length that is a multiple of 6,
    // it returns an error otherwise. If src does not contain trits, the behavior of Decode is undefined.
    fun decode(destination: ByteArray, source: Trits): Int {
        var i = 0
        var j = 0
        while (j <= source.size - TRITS_PER_BYTE) {
            val t1Portion = source.slice(j until j + 3).toByteArray()
            val t2Portion = source.slice(j + TRITS_PER_TRYTE until j + TRITS_PER_TRYTE + 3).toByteArray()
            val t1 = mustTritsToTryteValue(t1Portion)
            val t2 = mustTritsToTryteValue(t2Portion)
            val (byte, ok) = decodeGroup(t1, t2)
            if (!ok) {
                throw IllegalArgumentException("Invalid tryte value")
            }
            destination[i] = byte
            i += 1
            j += TRITS_PER_BYTE
        }
        return i
    }

    // MustTryteValueToTryte converts the value of a tryte v in [-13,13] to a tryte char in [9A-Z].
    private fun mustTryteValueToTryte(value: Byte): Char {
        val idx = (value - MIN_TRYTE_VALUE).toInt()
        if (idx !in 0 until TRYTE_VALUE_TO_TRYTE_LUT.size) {
            throw IllegalArgumentException("Invalid tryte value: $value")
        }
        return TRYTE_VALUE_TO_TRYTE_LUT[idx]
    }

    // EncodeToTrytes returns the encoding of src converted into trytes.
    fun encodeToTrytes(source: ByteArray): Trytes {
        val destination = StringBuilder()
        destination.ensureCapacity(encodeLen(source.size) / TRITS_PER_TRYTE)

        for (byte in source) {
            val (t1, t2) = encodeGroup(byte)
            destination.append(mustTryteValueToTryte(t1))
            destination.append(mustTryteValueToTryte(t2))
        }

        return destination.toString()
    }

    // encodeGroup converts a byte into two tryte values.
    fun encodeGroup(byte: Byte): Pair<Byte, Byte> {
        val v = byte.toInt() + (TRYTE_RADIX / 2) * TRYTE_RADIX + (TRYTE_RADIX / 2)

        val quotient = v / TRYTE_RADIX
        val remainder = v % TRYTE_RADIX

        return Pair(
            (remainder + MIN_TRYTE_VALUE).toByte(),
            (quotient + MIN_TRYTE_VALUE).toByte()
        )
    }

    fun decodeGroup(t1: Byte, t2: Byte): Pair<Byte, Boolean> {
        val v = t1.toInt() + t2.toInt() * TRYTE_RADIX
        Log.d("Test", "v: $v")
        if (v < Byte.MIN_VALUE || v > Byte.MAX_VALUE) {
            return Pair(0, false)
        }
        return Pair(v.toByte(), true)
    }

}