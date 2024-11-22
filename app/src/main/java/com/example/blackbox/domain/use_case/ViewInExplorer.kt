package com.example.blackbox.domain.use_case

import android.content.Context
import android.content.Intent
import android.net.Uri

class ViewInExplorer(
    private val context: Context
) {
    operator fun invoke(blockId: String) {
        val url = "https://explorer.iota.org/iota-testnet/block/$blockId"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}