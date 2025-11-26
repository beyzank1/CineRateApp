package edu.augusta.scc.cinerateui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun RatingRow(
    selected: Int,
    onSelected: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 1..5) {
            IconToggleButton(
                checked = i <= selected,
                onCheckedChange = { onSelected(i) }
            ) {
                val icon = if (i <= selected) {
                    Icons.Filled.Star
                } else {
                    Icons.Outlined.Star
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Star $i"
                )
            }
        }
    }
}