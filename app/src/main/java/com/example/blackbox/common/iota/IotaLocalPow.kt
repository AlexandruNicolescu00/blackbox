package com.example.blackbox.common.iota

import com.example.blackbox.data.remote.dto.FullBlockWithTaggedDataPayload

interface IotaLocalPow {
    fun performPow(block: FullBlockWithTaggedDataPayload): ULong
}
