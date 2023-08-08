import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Comments {

    private static final int N = 10;

    public static void createCommentsTable(Connection connection) throws SQLException {
        String createTableSQL = "CREATE TABLE Comments (" +
                "comment_id INT AUTO_INCREMENT PRIMARY KEY," +
                "listing_id INT NOT NULL," +
                "description TEXT NOT NULL," +
                "rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5)," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (listing_id) REFERENCES listings(listing_id)" +
                ")";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
            System.out.println("Comments table created successfully.");
        } catch (SQLException e) {
            System.out.println("An error occurred while creating the Comments table: " + e.getMessage());
            throw e;
        }
    }

    public static void addComment(Connection connection, String listingID, String description, int rating) throws SQLException {
        String insertQuery = "INSERT INTO Comments (listing_id, description, rating, timestamp) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, listingID);
            preparedStatement.setString(2, description);
            preparedStatement.setInt(3, rating);

            // Generate timestamp based on current time
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            preparedStatement.setTimestamp(4, timestamp);

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected > 0 ? "Comment added successfully!" : "Failed to add comment.");
        } catch (SQLException e) {
            System.out.println("An error occurred while adding the comment: " + e.getMessage());
        }
    }

}
