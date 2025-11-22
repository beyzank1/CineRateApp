import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * MovieDataCollector using OMDb API (omdbapi.com).
 *
 * Your responsibilities in the group project:
 *  - Connect to a movie API
 *  - Search for movies by title
 *  - Fetch details for a specific title
 *  - Cache raw JSON to local files for offline use
 *
 * Anthony can later parse these JSON files and insert into the database.
 */
public class MovieDataCollector {

    // TODO: put your OMDb API key here or read from an env var
    // Get one at: https://www.omdbapi.com/apikey.aspx
    private static final String API_KEY = "a4023ee2";

    // Base URL for OMDb
    private static final String BASE_URL = "https://www.omdbapi.com/";

    private final OkHttpClient client;

    public MovieDataCollector() {
        this.client = new OkHttpClient();
    }

    public static void main(String[] args) {
        MovieDataCollector collector = new MovieDataCollector();

        // Example bulk collection of a few popular franchises
        String[] queries = {
                "Inception",
                "The Matrix",
                "Interstellar",
                "Avengers",
                "The Dark Knight",
                "Titanic",
                "Gladiator",
                "The Godfather",
                "Forrest Gump",
                "Star Wars",
                "Jurassic Park",
                "Toy Story",
                "Frozen",
                "The Lion King",
                "Harry Potter"
        };

        for (String q : queries) {
            System.out.println("🔍 Collecting data for: " + q);
            String searchJson = collector.searchMovies(q);
            if (searchJson != null) {
                System.out.println("✅ Search returned " + searchJson.length() + " characters");
                collector.saveJsonToFile(searchJson, "search_" + q.replace(" ", "_") + ".json");
            } else {
                System.err.println("❌ Search failed for: " + q);
            }
            System.out.println();
        }

        System.out.println("✅ Bulk Collection Complete!");
    }

    /**
     * Search for movies by text query.
     * Uses the OMDb search endpoint:
     *   GET /?apikey=KEY&s=QUERY&type=movie
     * It returns a list of matches in JSON.
     */
    public String searchMovies(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String url = BASE_URL
                    + "?apikey=" + API_KEY
                    + "&s=" + encoded
                    + "&type=movie";

            return makeRequest(url);
        } catch (Exception e) {
            System.err.println("Encoding error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get detailed info for a specific movie by IMDb ID (e.g. tt0848228).
     * Endpoint:
     *   GET /?apikey=KEY&i=IMDB_ID&plot=full
     *
     * This is useful once Anthony wants more details to store in DB.
     */
    public String getMovieDetailsById(String imdbId) {
        String url = BASE_URL
                + "?apikey=" + API_KEY
                + "&i=" + imdbId
                + "&plot=full";

        return makeRequest(url);
    }

    /**
     * Core HTTP GET helper using OkHttp.
     */
    private String makeRequest(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                System.err.println("Server returned code: " + response.code() + " " + response.message());
                if (response.body() != null) {
                    System.err.println("Body: " + response.body().string());
                }
                return null;
            }
        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Saves JSON data to a local file (for offline caching).
     */
    public void saveJsonToFile(String jsonData, String filename) {
        if (jsonData == null) {
            System.err.println("No data to save for " + filename);
            return;
        }

        try (FileWriter writer = new FileWriter(new File(filename))) {
            writer.write(jsonData);
            System.out.println("💾 Saved data to: " + filename);
        } catch (IOException e) {
            System.err.println("Could not save file: " + e.getMessage());
        }
    }
}
