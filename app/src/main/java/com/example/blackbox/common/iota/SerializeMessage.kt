package com.example.blackbox.common.iota

import com.example.blackbox.data.remote.dto.FullBlockWithTaggedDataPayload

const val PAYLOAD_TYPE_LEN_BYTES: UInt = 4u
const val PAYLOAD_TAG_LEN_BYTES: UInt = 1u
const val PAYLOAD_DATA_LEN_BYTES: UInt = 4u

@OptIn(ExperimentalStdlibApi::class)
fun serializeMessage(
    block: FullBlockWithTaggedDataPayload,
): ByteArray {
    var serializedMessage = ByteArray(0)
    val protocolVersion: UByte = block.protocolVersion.toUByte()
    serializedMessage += protocolVersion.toLittleEndianByteArray(size = 1)

    val parentsCount: UByte = block.parents.size.toUByte()
    serializedMessage += parentsCount.toLittleEndianByteArray(size = 1)

    var parents = ByteArray(0)
    for (parent in block.parents) {
        parents += if (parent.startsWith("0x")) parent.removePrefix("0x").hexToByteArray() else parent.hexToByteArray()
    }
    serializedMessage += parents

    var payloadSize: UInt = 0u
    var payloadTag = ByteArray(0)
    var payloadTagLength: UByte = 0u
    var payloadData = ByteArray(0)
    var payloadDataLength: UInt = 0u

    if (block.payload != null) {
        payloadTag = block.payload.tag.toByteArray()
        payloadTagLength = payloadTag.size.toUByte()
        payloadData = block.payload.data.toByteArray()
        payloadDataLength = payloadData.size.toUInt()
        payloadSize= (PAYLOAD_TYPE_LEN_BYTES + PAYLOAD_TAG_LEN_BYTES + PAYLOAD_DATA_LEN_BYTES + payloadTagLength + payloadDataLength).toUInt()
    }
    serializedMessage += payloadSize.toLittleEndianByteArray(size = 4)

    if (payloadSize > 0u) {
        val payloadType: UInt = 5u
        serializedMessage += payloadType.toLittleEndianByteArray(size = 4) + payloadTagLength.toLittleEndianByteArray(size = 1) + payloadTag + payloadDataLength.toLittleEndianByteArray(size = 4) + payloadData
    }

    return serializedMessage
}

fun Any.toLittleEndianByteArray(size: Int): ByteArray {
    return ByteArray(size) { i ->
        when (this) {
            is UByte -> this.toByte()
            is UInt -> (this shr (i * 8) and 0xFFu).toByte()
            is ULong -> (this shr (i * 8) and 0xFFu).toByte()
            is Byte -> this // Byte is 1 byte
            is Int -> (this shr (i * 8) and 0xFF).toByte()
            is Long -> (this shr (i * 8) and 0xFF).toByte()
            else -> throw IllegalArgumentException("Unsupported type: ${this::class.simpleName}")
        }
    }
}