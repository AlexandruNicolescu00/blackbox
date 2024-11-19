package com.example.blackbox.common.iota

import java.util.Arrays

class JCurl() : ICurl {
    companion object {
        const val HASH_LENGTH = 243
        private const val STATE_LENGTH = 3 * HASH_LENGTH
        const val NUMBER_OF_ROUNDS = 81
        private val TRUTH_TABLE = byteArrayOf(1, 0, -1, 2, 1, -1, 0, 2, -1, 1, 0)
    }

    private val scratchpad = ByteArray(STATE_LENGTH)
    private var state: Trits = ByteArray(STATE_LENGTH)

    override fun absorb(
        trits: Trits,
        offset: Int,
        length: Int
    ): ICurl {
        var localOffset = offset
        var localLength = length
        do {
            System.arraycopy(trits, localOffset, state, 0, if (localLength < HASH_LENGTH) localLength else HASH_LENGTH)
            transform()
            localOffset = localOffset + HASH_LENGTH
            localLength = localLength - HASH_LENGTH
        } while (localLength > 0)

        return this
    }

    override fun absorb(trits: Trits): ICurl {
        return absorb(trits, 0, trits.size)
    }

    override fun squeeze(
        trits: Trits,
        offset: Int,
        length: Int
    ): Trits {
        var localOffset = offset
        var localLength = length

        do {
            System.arraycopy(state, 0, trits, localOffset, if (localLength < HASH_LENGTH) localLength else HASH_LENGTH)
            transform()
            localOffset = localOffset + HASH_LENGTH
            localLength = localLength - HASH_LENGTH
        } while (localLength > 0)

        return state
    }

    override fun squeeze(trits: Trits): Trits {
        return squeeze(trits, 0, trits.size)
    }

    override fun transform(): ICurl {
        var scratchpadIndex = 0
        var prevScratchpadIndex = 0
        var truthTableIndex = 0
        repeat (NUMBER_OF_ROUNDS) {
            System.arraycopy(state, 0, scratchpad, 0, STATE_LENGTH)
            for (stateIndex in 0 until STATE_LENGTH) {
                prevScratchpadIndex = scratchpadIndex
                scratchpadIndex = if (scratchpadIndex < 365) scratchpadIndex + 364 else scratchpadIndex - 365
                truthTableIndex = scratchpad[prevScratchpadIndex] +
                        (scratchpad[scratchpadIndex].toInt() shl(2)) + 5
                state[stateIndex] = TRUTH_TABLE[truthTableIndex]
            }
        }
        return this
    }

    override fun reset(pairMode: Boolean): JCurl {
        Arrays.fill(state, 0)
        return this
    }

    override fun clone(): ICurl {
        return JCurl()
    }
}