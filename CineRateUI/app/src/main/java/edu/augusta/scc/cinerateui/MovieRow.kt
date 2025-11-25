package edu.augusta.scc.cinerateui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MovieRow(
    movie: Movie,
    onClick: () -> Unit,
    onWriteReviewClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }       // <– whole row clickable
            .padding(8.dp)
    ) {
        // LEFT: Poster + edit icon (unchanged except for param names)
        Box(
            modifier = Modifier
                .size(width = 90.dp, height = 130.dp)
                .padding(end = 12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Poster")
                }
            }

            IconButton(
                onClick = onWriteReviewClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Write review"
                )
            }
        }

        // RIGHT: summary info (as you already had)
        Column(modifier = Modifier.weight(1f)) {
            Text(movie.title, style = MaterialTheme.typography.titleMedium)
            movie.year?.let {
                Text(it.toString(), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(4.dp))
            Text(movie.description, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text(
                "Cast: " + movie.cast.joinToString(", "),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(4.dp))
            Text("⭐ ${movie.avgRating}")
        }
    }
}