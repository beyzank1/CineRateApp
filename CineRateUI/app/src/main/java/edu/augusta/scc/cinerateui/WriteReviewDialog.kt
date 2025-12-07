package edu.augusta.scc.cinerateui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WriteReviewDialog(
    movie: Movie,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, text: String) -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Poster placeholder (will swap with Coil later)
                Surface(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = 12.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Poster")
                    }
                }

                // Movie title
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column {

                Spacer(Modifier.height(8.dp))

                // Optional: Short description preview
                Text(
                    text = movie.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )

                Spacer(Modifier.height(16.dp))

                // Rating
                Text("Your rating")
                Spacer(Modifier.height(4.dp))

                RatingRow(
                    selected = rating,
                    onSelected = { rating = it }
                )

                Spacer(Modifier.height(16.dp))

                // Review text
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Write your review") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(rating, text) }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}