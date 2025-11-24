package org.example;

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

    private static final String API_KEY = System.getenv("OMDB_API_KEY") != null
            ? System.getenv("OMDB_API_KEY")
            : "a4023ee2";

    private static final String BASE_URL = "https://www.omdbapi.com/";

    private final OkHttpClient client;
    private final Gson gson;

    public MovieDataCollector() {
        this.client = new OkHttpClient();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    // ✅ FIXED: Added 'public' to the class and ALL fields below
    // This allows DatabaseManager to read them without error.
    public static class MovieRecord {
        public String originalQuery;
        public String title;
        public String year;
        public String imdbId;
        public String type;
        public String poster;

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

        String[] queries = {
                "Inception", "The Matrix", "Interstellar", "Avengers", "The Dark Knight",
                "Titanic", "Gladiator", "The Godfather", "Forrest Gump", "Star Wars",
                "Jurassic Park", "Toy Story", "Frozen", "The Lion King", "Harry Potter",
                "Pulp Fiction", "Fight Club", "The Lord of the Rings", "The Shawshank Redemption", "Goodfellas",
                "The Silence of the Lambs", "Schindler's List", "Saving Private Ryan", "The Green Mile", "Seven",
                "Spirited Away", "Parasite", "La La Land", "Whiplash", "Mad Max: Fury Road",
                "Black Panther", "Spider-Man: Into the Spider-Verse", "The Social Network", "Avatar", "Back to the Future",
                "Raiders of the Lost Ark", "E.T. the Extra-Terrestrial", "Jaws", "Rocky", "Alien",
                "Terminator 2: Judgment Day", "The Shining", "2001: A Space Odyssey", "Casablanca", "Citizen Kane",
                "Gone with the Wind", "The Wizard of Oz", "Psycho", "Vertigo", "Rear Window"
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
            try { Thread.sleep(200); } catch (InterruptedException e) {}
        }

        String jsonFilename = "movies_50.json";
        String allJson = collector.gson.toJson(collectedMovies);
        collector.saveJsonToFile(allJson, jsonFilename);

        System.out.println("\n✅ Finished collecting. " + collectedMovies.size() + " movies saved into " + jsonFilename);

        System.out.println("\n--- Database Insertion ---");
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeDatabase();
        dbManager.insertMoviesFromJson(jsonFilename);

        System.out.println("🎉 Data Collection Complete!");
    }

    public MovieRecord searchFirstMovie(String query) {
        String json = searchMoviesRaw(query);
        if (json == null) return null;

        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("Response") || !"True".equalsIgnoreCase(root.get("Response").getAsString())) {
                System.err.println("OMDb reported error for query \"" + query + "\": " + root);
                return null;
            }

            JsonArray searchArray = root.getAsJsonArray("Search");
            if (searchArray == null || searchArray.size() == 0) {
                return null;
            }

            JsonObject first = searchArray.get(0).getAsJsonObject();
            return new MovieRecord(query,
                    getAsString(first, "Title"),
                    getAsString(first, "Year"),
                    getAsString(first, "imdbID"),
                    getAsString(first, "Type"),
                    getAsString(first, "Poster"));
        } catch (Exception e) {
            System.err.println("Error parsing JSON for " + query + ": " + e.getMessage());
            return null;
        }
    }

    public String searchMoviesRaw(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String url = BASE_URL + "?apikey=" + API_KEY + "&s=" + encoded + "&type=movie";
            return makeRequest(url);
        } catch (Exception e) {
            return null;
        }
    }

    private String makeRequest(String url) {
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            return (response.isSuccessful() && response.body() != null) ? response.body().string() : null;
        } catch (IOException e) {
            return null;
        }
    }

    public void saveJsonToFile(String jsonData, String filename) {
        try (FileWriter writer = new FileWriter(new File(filename))) {
            writer.write(jsonData);
            System.out.println("💾 Saved data to: " + filename);
        } catch (IOException e) {
            System.err.println("Could not save file: " + e.getMessage());
        }
    }

    private String getAsString(JsonObject obj, String field) {
        if (obj.has(field) && !obj.get(field).isJsonNull()) {
            return obj.get(field).getAsString();
        }
        return null;
    }
}