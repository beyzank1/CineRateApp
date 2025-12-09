package edu.augusta.scc.cinerateui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale



@Composable
fun MovieRow(
    movie: Movie,
    onClick: () -> Unit,
    onWriteReviewClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
    ) {

        // ✅ LEFT: Poster + Edit Button INSIDE SAME BOX
        Box(
            modifier = Modifier
                .size(width = 90.dp, height = 130.dp)
                .padding(end = 12.dp)
        ) {

            val poster = if (
                movie.posterUrl.isNullOrBlank() || movie.posterUrl == "N/A"
            ) {
                R.drawable.poster_placeholder
            } else {
                movie.posterUrl
            }

            AsyncImage(
                model = poster,
                contentDescription = "${movie.title} poster",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // ✅ Edit Button is NOW correctly inside the Box
            IconButton(
                onClick = onWriteReviewClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Write review",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

        }

        // ✅ RIGHT: Info Column (weight now legal again)
        Column(modifier = Modifier.weight(1f)) {
            Text(movie.title, style = MaterialTheme.typography.titleMedium)


            Spacer(Modifier.height(4.dp))
            Text(movie.description, maxLines = 2)

            Spacer(Modifier.height(4.dp))
            if (movie.avgRating > 0.0) {
                Text("⭐ %.1f".format(movie.avgRating))
            }

        }
    }
}
