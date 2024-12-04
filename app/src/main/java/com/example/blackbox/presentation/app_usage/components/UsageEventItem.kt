package com.example.blackbox.presentation.app_usage.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.example.blackbox.R
import com.example.blackbox.common.dateFormat

@Composable
fun UsageEventItem(
    packageName: String,
    eventType: String,
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        Text(
            text = "$eventType $packageName",//.split(".").last(),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "at: ${dateFormat(timestamp)}",
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.space_small))
        )
    }
}