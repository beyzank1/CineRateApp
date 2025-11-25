package org.example;

import io.javalin.Javalin; // The Server Library
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MovieDataCollector {

    private static final String API_KEY = System.getenv("OMDB_API_KEY") != null ? System.getenv("OMDB_API_KEY") : "a4023ee2";
    private static final String BASE_URL = "https://www.omdbapi.com/";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    //  Public class for data transfer
    public static class MovieRecord {
        public String originalQuery;
        public String title;
        public String year;
        public String imdbId;
        public String type;
        public String poster;

        public MovieRecord(String originalQuery, String title, String year, String imdbId, String type, String poster) {
            this.originalQuery = originalQuery;
            this.title = title;
            this.year = year;
            this.imdbId = imdbId;
            this.type = type;
            this.poster = poster;
        }
    }

    public static void main(String[] args) {
        // Force load the PostgreSQL Driver immediately on startup
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ PostgreSQL Driver not found! Check pom.xml.");
        }

        // 1. Initialize Database connection
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeDatabase();

        // 2. Start the Web Server on Port 5000
        System.out.println("🌍 Starting Web Server on Port 5000...");

        // Updated CORS configuration for Javalin 6.x
        // This prevents the "cannot find symbol variable plugins" error
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> it.anyHost());
            });
        }).start(5000);

        // --- DEFINE API ENDPOINTS ---

        // Endpoint 1: Search for movies
        // URL: http://10.0.2.2:5000/search?q=Inception
        app.get("/search", ctx -> {
            String query = ctx.queryParam("q");
            System.out.println("📞 Request received: Search for '" + query + "'");

            if (query == null || query.isEmpty()) {
                ctx.json(new ArrayList<>());
                return;
            }

            // Use the collector to fetch live data (or check DB)
            MovieDataCollector collector = new MovieDataCollector();
            MovieRecord result = collector.searchFirstMovie(query);

            List<MovieRecord> responseList = new ArrayList<>();
            if (result != null) {
                responseList.add(result);

                // Save to DB for caching using the new insertMovie method
                dbManager.insertMovie(result);
            }

            // Send the result back to the Android App
            ctx.json(responseList);
        });

        // Endpoint 2: Health Check
        app.get("/", ctx -> ctx.result("Server is Online!"));

        System.out.println("✅ Server is ready! Waiting for connections...");
    }

    // --- Helper Methods ---

    public MovieRecord searchFirstMovie(String query) {
        String json = searchMoviesRaw(query);
        if (json == null) return null;
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("Response") || !"True".equalsIgnoreCase(root.get("Response").getAsString())) return null;
            JsonArray searchArray = root.getAsJsonArray("Search");
            if (searchArray == null || searchArray.size() == 0) return null;
            JsonObject first = searchArray.get(0).getAsJsonObject();
            return new MovieRecord(query, getAsString(first, "Title"), getAsString(first, "Year"), getAsString(first, "imdbID"), getAsString(first, "Type"), getAsString(first, "Poster"));
        } catch (Exception e) { return null; }
    }

    public String searchMoviesRaw(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            return makeRequest(BASE_URL + "?apikey=" + API_KEY + "&s=" + encoded + "&type=movie");
        } catch (Exception e) { return null; }
    }

    private String makeRequest(String url) {
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            return (response.isSuccessful() && response.body() != null) ? response.body().string() : null;
        } catch (IOException e) { return null; }
    }

    public void saveJsonToFile(String jsonData, String filename) {
        try (FileWriter writer = new FileWriter(new File(filename))) {
            writer.write(jsonData);
        } catch (IOException e) { }
    }

    private String getAsString(JsonObject obj, String field) {
        if (obj.has(field) && !obj.get(field).isJsonNull()) return obj.get(field).getAsString();
        return null;
    }
}