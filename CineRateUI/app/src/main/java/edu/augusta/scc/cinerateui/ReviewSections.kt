package edu.augusta.scc.cinerateui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import edu.augusta.scc.cinerateui.api.ApiClient

@Composable
fun ReviewsSection(movieId: String, refreshKey: Int) {

    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(movieId, refreshKey) {
        try {
            // This will fail silently until backend exists — and that's OK
            reviews = ApiClient.api.getReviews(movieId)
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Text(
                text = "Loading reviews...",
                style = MaterialTheme.typography.bodySmall
            )
        }

        error != null -> {
            Text(
                text = "Reviews unavailable",
                style = MaterialTheme.typography.bodySmall
            )
        }

        reviews.isEmpty() -> {
            Text(
                text = "No reviews yet. Be the first!",
                style = MaterialTheme.typography.bodySmall
            )
        }

        else -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                reviews.forEach { review ->
                    ReviewCard(review)
                }
            }
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("⭐ ${review.value} / 5", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(review.review, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
