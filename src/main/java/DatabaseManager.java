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
import java.util.List;

/**
 * Manages the local SQLite database connection and data operations.
 *
 * Responsibilities:
 *  - Establish connection to the SQLite database file.
 *  - Create the necessary table schema.
 *  - Parse the JSON file and insert movie records into the database.
 */
public class DatabaseManager {

    // The name of the SQLite database file.
    private static final String DB_URL = "jdbc:sqlite:cinerate.db";

    /**
     * Establishes a connection to the database and creates the movies table
     * if it doesn't already exist.
     */
    public void initializeDatabase() {
        // SQL statements for creating tables based on db_schemas.txt
        String[] sqlStatements = {
            "CREATE TABLE IF NOT EXISTS movies (\n"
                + "    imdbId TEXT PRIMARY KEY,\n" // Using imdbId from OMDb as the primary key
                + "    originalQuery TEXT NOT NULL,\n"
                + "    title TEXT NOT NULL,\n"
                + "    year TEXT,\n"
                + "    type TEXT,\n"
                + "    poster TEXT,\n"
                + "    release_date TEXT,\n"
                + "    avg_score REAL,\n"
                + "    cast_crew TEXT,\n" // Stored as JSON string or delimited text
                + "    genre TEXT,\n"     // Stored as JSON string or delimited text
                + "    rating TEXT\n"
                + ");",

            "CREATE TABLE IF NOT EXISTS users (\n"
                + "    uid INTEGER PRIMARY KEY AUTOINCREMENT\n"
                + ");",

            "CREATE TABLE IF NOT EXISTS scores (\n"
                + "    uid INTEGER PRIMARY KEY AUTOINCREMENT,\n"
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

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            // Enable foreign key support in SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Create tables
            for (String sql : sqlStatements) {
                stmt.execute(sql);
            }
            System.out.println("✅ Database initialized and tables are ready.");
        } catch (SQLException e) {
            System.err.println("❌ Database initialization error: " + e.getMessage());
        }
    }

    /**
     * Reads movie records from a JSON file and inserts them into the database.
     * It uses a transaction for efficiency and data integrity.
     *
     * @param jsonFilePath The path to the movie data JSON file.
     */
    public void insertMoviesFromJson(String jsonFilePath) {
        Gson gson = new Gson();
        Type movieListType = new TypeToken<List<MovieDataCollector.MovieRecord>>(){}.getType();

        String sql = "INSERT OR REPLACE INTO movies(originalQuery, title, year, imdbId, type, poster) VALUES(?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             FileReader reader = new FileReader(jsonFilePath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            List<MovieDataCollector.MovieRecord> movies = gson.fromJson(reader, movieListType);

            conn.setAutoCommit(false); // Start transaction

            for (MovieDataCollector.MovieRecord movie : movies) {
                pstmt.setString(1, movie.originalQuery);
                pstmt.setString(2, movie.title);
                pstmt.setString(3, movie.year);
                pstmt.setString(4, movie.imdbId);
                pstmt.setString(5, movie.type);
                pstmt.setString(6, movie.poster);
                pstmt.addBatch();
            }

            pstmt.executeBatch(); // Execute all statements in the batch
            conn.commit(); // Commit transaction

            System.out.println("✅ Successfully inserted/updated " + movies.size() + " movies into the database.");

        } catch (IOException | SQLException e) {
            System.err.println("❌ Error inserting movies from JSON: " + e.getMessage());
        }
    }

    /**
     * Searches for movies in the local database where the title contains the given query string.
     *
     * @param titleQuery The text to search for in movie titles.
     * @return A list of matching MovieRecord objects.
     */
    public List<MovieDataCollector.MovieRecord> searchMoviesByTitle(String titleQuery) {
        List<MovieDataCollector.MovieRecord> foundMovies = new java.util.ArrayList<>();
        // SQL query to find movies with titles that contain the search query (case-insensitive).
        String sql = "SELECT originalQuery, title, year, imdbId, type, poster FROM movies WHERE title LIKE ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the parameter for the LIKE clause. The '%' are wildcards.
            pstmt.setString(1, "%" + titleQuery + "%");

            ResultSet rs = pstmt.executeQuery();

            // Loop through the results and build MovieRecord objects
            while (rs.next()) {
                foundMovies.add(new MovieDataCollector.MovieRecord(
                        rs.getString("originalQuery"),
                        rs.getString("title"),
                        rs.getString("year"),
                        rs.getString("imdbId"),
                        rs.getString("type"),
                        rs.getString("poster")
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ Database search error: " + e.getMessage());
        }

        return foundMovies;
    }
}