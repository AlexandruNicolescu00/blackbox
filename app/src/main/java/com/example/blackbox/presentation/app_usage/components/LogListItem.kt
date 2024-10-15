package com.example.blackbox.presentation.app_usage.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.example.blackbox.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogListItem(
    packageName: String,
    lastTimeUsed: Long,
    modifier: Modifier = Modifier
) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val lastTimeFormatted = remember(lastTimeUsed) { formatter.format(Date(lastTimeUsed)) }

    Row(
        modifier = modifier
    ) {
        Text(
            text = packageName.split(".").last(),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Used at: $lastTimeFormatted",
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.space_small))
        )
    }
}