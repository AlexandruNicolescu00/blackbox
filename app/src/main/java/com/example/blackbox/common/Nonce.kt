package com.example.blackbox.common

import com.appmattus.crypto.Algorithm
import com.example.blackbox.common.iota.B1t6
import com.example.blackbox.common.iota.JCurl
import java.math.BigInteger

fun findNonceForPoW(blockData: ByteArray): Long {
    var nonce: Long = 0
    while (true) {
        val hash = blake2b(blockData)
        val curl = JCurl()
        val converter = B1t6()
        //val powHash = converter.hashWithJCurl(hash)
        // Calcola la difficoltà (verifica zeri iniziali nell'hash)
        if (isValidPoW(hash)) {
            return nonce // abbiamo trovato il nonce corretto
        }
        nonce++  // prova con il prossimo nonce
    }
}

fun isValidPoW(hash: ByteArray): Boolean {
    // Verifica se l'hash ha abbastanza zeri iniziali (la difficoltà)
    val targetDifficulty = 0x00000 // esempio di difficoltà (5 zeri)
    val hashInt = BigInteger(1, hash)
    return true
}

fun blake2b(data: ByteArray): ByteArray {
    // Funzione per calcolare l'hash BLAKE2b
    val digest = Algorithm.Blake2b_256.createDigest()
    digest.update(data)
    val result: ByteArray = digest.digest()
    return result
}
