import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * MovieDataCollector using OMDb API (omdbapi.com).
 *
 * Your responsibilities in the group project:
 *  - Connect to a movie API
 *  - Search for movies by title
 *  - Pick ONE main match per title (first search result)
 *  - Cache simplified movie data as one JSON array for offline use
 *
 * Anthony can later parse this single JSON file and insert into the database.
 */
public class MovieDataCollector {

    // ✅ Put your OMDb API key here — NO SPACES
    // Get/activate at: https://www.omdbapi.com/apikey.aspx
    private static final String API_KEY = "a4023ee2";

    // Base URL for OMDb
    private static final String BASE_URL = "https://www.omdbapi.com/";

    private final OkHttpClient client;
    private final Gson gson;

    public MovieDataCollector() {
        this.client = new OkHttpClient();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * A simplified representation of a movie that we'll write to our JSON file.
     * This keeps things clean for Anthony when inserting into the DB.
     */
    public static class MovieRecord {
        String originalQuery; // what we searched (e.g., "Inception")
        String title;         // "Inception"
        String year;          // "2010"
        String imdbId;        // "tt1375666"
        String type;          // "movie"
        String poster;        // URL to poster image

        public MovieRecord(String originalQuery,
                           String title,
                           String year,
                           String imdbId,
                           String type,
                           String poster) {
            this.originalQuery = originalQuery;
            this.title = title;
            this.year = year;
            this.imdbId = imdbId;
            this.type = type;
            this.poster = poster;
        }
    }

    public static void main(String[] args) {
        MovieDataCollector collector = new MovieDataCollector();

        // 🔹 15 different movie/franchise queries
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

        List<MovieRecord> collectedMovies = new ArrayList<>();

        for (String q : queries) {
            System.out.println("🔍 Collecting data for: " + q);
            MovieRecord record = collector.searchFirstMovie(q);
            if (record != null) {
                collectedMovies.add(record);
                System.out.println("✅ Picked: " + record.title + " (" + record.year + ") [" + record.imdbId + "]");
            } else {
                System.err.println("❌ No movie found for query: " + q);
            }
            System.out.println();
        }

        // 🔹 Write all 15 movies as ONE JSON array into a single file
        String jsonFilename = "movies_15.json";
        String allJson = collector.gson.toJson(collectedMovies);
        collector.saveJsonToFile(allJson, jsonFilename);

        System.out.println("\n✅ Finished collecting. 15 movies saved into " + jsonFilename);

        // 🔹 Now, let's get this data into our local database.
        System.out.println("\n--- Database Insertion ---");
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeDatabase(); // Creates DB and table if they don't exist
        dbManager.insertMoviesFromJson(jsonFilename); // Parses the JSON and inserts records

    }

    /**
     * High-level helper:
     *  - Calls the OMDb search endpoint
     *  - Reads the first result in "Search"
     *  - Converts it to a MovieRecord
     */
    public MovieRecord searchFirstMovie(String query) {
        String json = searchMoviesRaw(query);
        if (json == null) {
            return null;
        }

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        // Check if API responded successfully
        if (!root.has("Response") || !"True".equalsIgnoreCase(root.get("Response").getAsString())) {
            System.err.println("OMDb reported error for query \"" + query + "\": " + root);
            return null;
        }

        JsonArray searchArray = root.getAsJsonArray("Search");
        if (searchArray == null || searchArray.size() == 0) {
            System.err.println("No Search results array for query: " + query);
            return null;
        }

        // Take only the FIRST result as the "main" movie
        JsonObject first = searchArray.get(0).getAsJsonObject();

        String title = getAsString(first, "Title");
        String year = getAsString(first, "Year");
        String imdbId = getAsString(first, "imdbID");
        String type = getAsString(first, "Type");
        String poster = getAsString(first, "Poster");

        return new MovieRecord(query, title, year, imdbId, type, poster);
    }

    /**
     * Raw search for movies by text query.
     * Uses the OMDb search endpoint:
     *   GET /?apikey=KEY&s=QUERY&type=movie
     * Returns the entire JSON string.
     */
    public String searchMoviesRaw(String query) {
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
     * Not used in main() right now, but ready for later.
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

    /**
     * Safe JSON string getter.
     */
    private String getAsString(JsonObject obj, String field) {
        if (obj.has(field) && !obj.get(field).isJsonNull()) {
            return obj.get(field).getAsString();
        }
        return null;
    }
}
