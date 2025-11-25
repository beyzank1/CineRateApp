package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    public record ScoreRecord(long scoreId, String movieId, long authorId, int value, String review) {}

    public DatabaseManager() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ PostgreSQL Driver not found in classpath! Check pom.xml.");
        }
    }

    private String getDbUrl() {
        String url = System.getenv("DB_URL");
        return (url != null) ? url : "jdbc:postgresql://localhost:5432/moviedb";
    }

    private String getDbUser() {
        String user = System.getenv("DB_USER");
        return (user != null) ? user : "user";
    }

    private String getDbPass() {
        String pass = System.getenv("DB_PASS");
        return (pass != null) ? pass : "password";
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPass());
    }

    public void initializeDatabase() {
        String[] sqlStatements = {
                "CREATE TABLE IF NOT EXISTS movies (\n"
                        + "    imdbId TEXT PRIMARY KEY,\n"
                        + "    originalQuery TEXT NOT NULL,\n"
                        + "    title TEXT NOT NULL,\n"
                        + "    year TEXT,\n"
                        + "    type TEXT,\n"
                        + "    poster TEXT,\n"
                        + "    release_date TEXT,\n"
                        + "    avg_score REAL,\n"
                        + "    cast_crew TEXT,\n"
                        + "    genre TEXT,\n"
                        + "    rating TEXT\n"
                        + ");",

                "CREATE TABLE IF NOT EXISTS users (\n"
                        + "    uid SERIAL PRIMARY KEY\n"
                        + ");",

                "CREATE TABLE IF NOT EXISTS scores (\n"
                        + "    uid SERIAL PRIMARY KEY,\n"
                        + "    movie_id TEXT NOT NULL,\n"
                        + "    author_id INTEGER NOT NULL,\n"
                        + "    value INTEGER,\n"
                        + "    review TEXT,\n"
                        + "    FOREIGN KEY (movie_id) REFERENCES movies(imdbId),\n"
                        + "    FOREIGN KEY (author_id) REFERENCES users(uid)\n"
                        + ");",

                "CREATE TABLE IF NOT EXISTS friendships (\n"
                        + "    requestor_id INTEGER NOT NULL,\n"
                        + "    receiver_id INTEGER NOT NULL,\n"
                        + "    accepted BOOLEAN,\n"
                        + "    PRIMARY KEY (requestor_id, receiver_id),\n"
                        + "    FOREIGN KEY (requestor_id) REFERENCES users(uid),\n"
                        + "    FOREIGN KEY (receiver_id) REFERENCES users(uid)\n"
                        + ");"
        };

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            for (String sql : sqlStatements) {
                stmt.execute(sql);
            }
            System.out.println("✅ Database initialized and tables are ready.");
        } catch (SQLException e) {
            System.err.println("❌ Database initialization error: " + e.getMessage());
        }
    }

    // It allows saving a single movie record to the DB.
    public void insertMovie(MovieDataCollector.MovieRecord movie) {
        String sql = "INSERT INTO movies(originalQuery, title, year, imdbId, type, poster) " +
                "VALUES(?,?,?,?,?,?) " +
                "ON CONFLICT (imdbId) DO UPDATE SET " +
                "originalQuery = EXCLUDED.originalQuery, " +
                "title = EXCLUDED.title, " +
                "year = EXCLUDED.year, " +
                "type = EXCLUDED.type, " +
                "poster = EXCLUDED.poster";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, movie.originalQuery);
            pstmt.setString(2, movie.title);
            pstmt.setString(3, movie.year);
            pstmt.setString(4, movie.imdbId);
            pstmt.setString(5, movie.type);
            pstmt.setString(6, movie.poster);

            pstmt.executeUpdate();
            System.out.println("💾 Cached movie in DB: " + movie.title);

        } catch (SQLException e) {
            System.err.println("❌ Error caching movie: " + e.getMessage());
        }
    }

    public void insertMoviesFromJson(String jsonFilePath) {
        Gson gson = new Gson();
        Type movieListType = new TypeToken<List<MovieDataCollector.MovieRecord>>(){}.getType();

        String sql = "INSERT INTO movies(originalQuery, title, year, imdbId, type, poster) " +
                "VALUES(?,?,?,?,?,?) " +
                "ON CONFLICT (imdbId) DO UPDATE SET " +
                "originalQuery = EXCLUDED.originalQuery, " +
                "title = EXCLUDED.title, " +
                "year = EXCLUDED.year, " +
                "type = EXCLUDED.type, " +
                "poster = EXCLUDED.poster";

        try (Connection conn = getConnection();
             FileReader reader = new FileReader(jsonFilePath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            List<MovieDataCollector.MovieRecord> movies = gson.fromJson(reader, movieListType);

            conn.setAutoCommit(false);

            for (MovieDataCollector.MovieRecord movie : movies) {
                pstmt.setString(1, movie.originalQuery);
                pstmt.setString(2, movie.title);
                pstmt.setString(3, movie.year);
                pstmt.setString(4, movie.imdbId);
                pstmt.setString(5, movie.type);
                pstmt.setString(6, movie.poster);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit();

            System.out.println("✅ Successfully inserted/updated " + movies.size() + " movies into the database.");

        } catch (IOException | SQLException e) {
            System.err.println("❌ Error inserting movies from JSON: " + e.getMessage());
        }
    }
}