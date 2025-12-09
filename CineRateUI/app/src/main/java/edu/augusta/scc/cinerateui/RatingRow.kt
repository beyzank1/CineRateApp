package edu.augusta.scc.cinerateui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RatingRow(
    selected: Int,
    onSelected: (Int) -> Unit
) {
    Row {
        for (i in 1..5) {
            val isSelected = i <= selected

            Text(
                text = if (isSelected) "⭐" else "☆",
                fontSize = 28.sp,
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onSelected(i) }
            )
        }
    }
}