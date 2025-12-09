package edu.augusta.scc.cinerateui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.augusta.scc.cinerateui.api.ApiClient
import edu.augusta.scc.cinerateui.api.ReviewResponse

@Composable
fun ReviewsSection(
    movieId: String,
    refreshTrigger: Int,
    onAverageCalculated: (Double) -> Unit
) {
    var reviews by remember { mutableStateOf<List<ReviewResponse>>(emptyList()) }

    LaunchedEffect(movieId, refreshTrigger) {
        try {
            reviews = ApiClient.api.getReviews(movieId)

            if (reviews.isNotEmpty()) {
                val avg = reviews.map { it.value }.average()
                onAverageCalculated(avg)
            } else {
                onAverageCalculated(0.0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onAverageCalculated(0.0)
        }
    }

    Column {
        if (reviews.isEmpty()) {
            Text("No reviews yet")
        } else {
            reviews.forEach { review ->
                Text("⭐ ${review.value}")
                Text(review.review)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}


