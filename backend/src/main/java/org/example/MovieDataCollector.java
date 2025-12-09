package org.example;

import io.javalin.Javalin;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MovieDataCollector {

    private static final String API_KEY = System.getenv("OMDB_API_KEY") != null ? System.getenv("OMDB_API_KEY") : "a4023ee2";
    private static final String BASE_URL = "https://www.omdbapi.com/";
    private final OkHttpClient client = new OkHttpClient();

    // --- 1. Data Structures ---

    public static class MovieRecord {
        public String originalQuery;
        public String title;
        public String year;
        public String imdbId;
        public String type;
        public String poster;
        public String plot; // Description

        public MovieRecord() {}

        public MovieRecord(String originalQuery, String title, String year, String imdbId, String type, String poster, String plot) {
            this.originalQuery = originalQuery;
            this.title = title;
            this.year = year;
            this.imdbId = imdbId;
            this.type = type;
            this.poster = poster;
            this.plot = plot;
        }
    }

    public static class ReviewRequest {
        public String movieId;
        public int authorId;
        public int value;
        public String review;
    }

    // --- 2. Main Method ---

    public static void main(String[] args) {
        // Load Driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ PostgreSQL Driver not found!");
        }

        // Initialize DB
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeDatabase();

        System.out.println("🌍 Starting Web Server on Port 5000...");

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> it.anyHost());
            });
        }).start(5000);

        // --- ENDPOINTS ---

        // 1. Search (Hybrid: Online with Auto-Cache -> Fallback to Offline)
        app.get("/search", ctx -> {
            String query = ctx.queryParam("q");
            if (query == null || query.isEmpty()) {
                ctx.json(new ArrayList<>());
                return;
            }
            MovieDataCollector collector = new MovieDataCollector();

            // Step 1: Try Online
            MovieRecord result = collector.searchFirstMovie(query); // Now fetches FULL details immediately
            List<MovieRecord> responseList = new ArrayList<>();

            if (result != null) {
                // Online success: Return result AND Cache it to DB immediately
                responseList.add(result);
                dbManager.insertMovie(result);
                System.out.println("🌐 Found online & Cached: " + result.title);
            } else {
                // Step 2: Online failed. Try Local DB.
                System.out.println("⚠️ Online search failed. Checking local DB for: " + query);
                responseList = dbManager.searchMoviesLocal(query);
            }

            ctx.json(responseList);
        });

        // 2. Get Full Details (Hybrid: Online -> Fallback to Offline)
        app.get("/movie/{imdbId}", ctx -> {
            String imdbId = ctx.pathParam("imdbId");
            MovieDataCollector collector = new MovieDataCollector();

            // Step 1: Try Online
            MovieRecord movie = collector.getMovieDetails(imdbId);

            if (movie != null) {
                dbManager.insertMovie(movie); // Update cache
                ctx.json(movie);
            } else {
                // Step 2: Check Local DB
                System.out.println("⚠️ Online details failed. Fetching from cache: " + imdbId);
                MovieRecord localMovie = dbManager.getMovieLocal(imdbId);

                if (localMovie != null) {
                    ctx.json(localMovie);
                } else {
                    ctx.status(404).result("Movie not found (Offline & Not Cached)");
                }
            }
        });

        // 3. Submit Review (Local DB)
        app.post("/reviews", ctx -> {
            try {
                ReviewRequest req = ctx.bodyAsClass(ReviewRequest.class);
                dbManager.addReview(req.movieId, req.authorId, req.value, req.review);
                ctx.status(201).result("Review Saved");
            } catch (Exception e) {
                ctx.status(500).result("Error saving review");
            }
        });

        // 4. Get Reviews (Local DB)
        app.get("/reviews/{movieId}", ctx -> {
            String movieId = ctx.pathParam("movieId");
            ctx.json(dbManager.getReviews(movieId));
        });

        // 5. Health Check
        app.get("/", ctx -> ctx.result("Server is Online!"));
    }

    // --- 3. Helper Methods ---

    //  Fetches the ID from search, then IMMEDIATELY gets full details (Plot)
    public MovieRecord searchFirstMovie(String query) {
        String json = searchMoviesRaw(query);
        if (json == null) return null;
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("Response") || !"True".equalsIgnoreCase(root.get("Response").getAsString())) return null;

            JsonArray searchArray = root.getAsJsonArray("Search");
            if (searchArray == null || searchArray.isEmpty()) return null;

            // Extract ID and get full details immediately
            JsonObject first = searchArray.get(0).getAsJsonObject();
            String imdbId = getAsString(first, "imdbID");

            return getMovieDetails(imdbId);

        } catch (Exception e) { return null; }
    }

    public MovieRecord getMovieDetails(String imdbId) {
        String url = BASE_URL + "?apikey=" + API_KEY + "&i=" + imdbId + "&plot=full";
        String json = makeRequest(url);
        if (json == null) return null;
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("Response") || !"True".equalsIgnoreCase(root.get("Response").getAsString())) return null;

            return new MovieRecord(
                    null, // Original query not needed for direct ID lookup
                    getAsString(root, "Title"),
                    getAsString(root, "Year"),
                    getAsString(root, "imdbID"),
                    getAsString(root, "Type"),
                    getAsString(root, "Poster"),
                    getAsString(root, "Plot") // Full Description
            );
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

    private String getAsString(JsonObject obj, String field) {
        if (obj.has(field) && !obj.get(field).isJsonNull()) return obj.get(field).getAsString();
        return "N/A";
    }
}