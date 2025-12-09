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
                // Movies Table (includes 'plot')
                "CREATE TABLE IF NOT EXISTS movies (\n"
                        + "    imdbId TEXT PRIMARY KEY,\n"
                        + "    originalQuery TEXT,\n"
                        + "    title TEXT NOT NULL,\n"
                        + "    year TEXT,\n"
                        + "    type TEXT,\n"
                        + "    poster TEXT,\n"
                        + "    plot TEXT,\n"
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

    public void insertMovie(MovieDataCollector.MovieRecord movie) {
        String sql = "INSERT INTO movies(originalQuery, title, year, imdbId, type, poster, plot) " +
                "VALUES(?,?,?,?,?,?,?) " +
                "ON CONFLICT (imdbId) DO UPDATE SET " +
                "originalQuery = EXCLUDED.originalQuery, " +
                "title = EXCLUDED.title, " +
                "year = EXCLUDED.year, " +
                "type = EXCLUDED.type, " +
                "poster = EXCLUDED.poster, " +
                "plot = EXCLUDED.plot";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, movie.originalQuery);
            pstmt.setString(2, movie.title);
            pstmt.setString(3, movie.year);
            pstmt.setString(4, movie.imdbId);
            pstmt.setString(5, movie.type);
            pstmt.setString(6, movie.poster);
            pstmt.setString(7, movie.plot);

            pstmt.executeUpdate();
            System.out.println("💾 Cached movie in DB: " + movie.title);

        } catch (SQLException e) {
            System.err.println("❌ Error caching movie: " + e.getMessage());
        }
    }

    // --- Search Local DB (Required for Offline Mode) ---
    public List<MovieDataCollector.MovieRecord> searchMoviesLocal(String query) {
        List<MovieDataCollector.MovieRecord> movies = new ArrayList<>();
        // Use ILIKE for case-insensitive search
        String sql = "SELECT * FROM movies WHERE title ILIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + query + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movies.add(new MovieDataCollector.MovieRecord(
                            rs.getString("originalQuery"),
                            rs.getString("title"),
                            rs.getString("year"),
                            rs.getString("imdbId"),
                            rs.getString("type"),
                            rs.getString("poster"),
                            rs.getString("plot")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error searching local DB: " + e.getMessage());
        }
        return movies;
    }

    // --- Get Movie Detail Local (Required for Offline Mode) ---
    public MovieDataCollector.MovieRecord getMovieLocal(String imdbId) {
        String sql = "SELECT * FROM movies WHERE imdbId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, imdbId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new MovieDataCollector.MovieRecord(
                            rs.getString("originalQuery"),
                            rs.getString("title"),
                            rs.getString("year"),
                            rs.getString("imdbId"),
                            rs.getString("type"),
                            rs.getString("poster"),
                            rs.getString("plot")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting local movie: " + e.getMessage());
        }
        return null;
    }

    // --- REVIEW METHODS ---

    public void ensureUserExists(int userId) {
        String sql = "INSERT INTO users (uid) VALUES (?) ON CONFLICT (uid) DO NOTHING";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Error ensuring user exists: " + e.getMessage());
        }
    }

    public void addReview(String movieId, int authorId, int value, String reviewText) {
        ensureUserExists(authorId);
        String sql = "INSERT INTO scores (movie_id, author_id, value, review) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, movieId);
            pstmt.setInt(2, authorId);
            pstmt.setInt(3, value);
            pstmt.setString(4, reviewText);
            pstmt.executeUpdate();
            System.out.println("📝 Review added for movie ID: " + movieId);
        } catch (SQLException e) {
            System.err.println("❌ Error adding review: " + e.getMessage());
        }
    }

    public List<ScoreRecord> getReviews(String movieId) {
        List<ScoreRecord> reviews = new ArrayList<>();
        String sql = "SELECT uid, movie_id, author_id, value, review FROM scores WHERE movie_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, movieId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new ScoreRecord(
                            rs.getLong("uid"),
                            rs.getString("movie_id"),
                            rs.getLong("author_id"),
                            rs.getInt("value"),
                            rs.getString("review")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching reviews: " + e.getMessage());
        }
        return reviews;
    }
}