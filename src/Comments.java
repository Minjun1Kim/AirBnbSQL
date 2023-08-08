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

    public static void createHostCommentsTable(Connection connection) throws SQLException {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS hostComments (" +
                "comment_id INT AUTO_INCREMENT PRIMARY KEY," +
                "listing_id INT NOT NULL," +
                "host_id INT NOT NULL," +
                "renter_name VARCHAR(100) NOT NULL," +
                "description TEXT NOT NULL," +
                "rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5)," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (listing_id) REFERENCES listings(listing_id)," +
                "FOREIGN KEY (renter_name) REFERENCES users(name)," +
                "FOREIGN KEY (host_id) REFERENCES users(user_id)" +
                ")";

        try (PreparedStatement statement = connection.prepareStatement(createTableQuery)) {
            statement.execute();
        }
    }

    public static void addComment(Connection connection, int userID, String listingID, String description, int rating) throws SQLException {
        String selectQuery = "SELECT * FROM bookings WHERE user_id = ? AND listing_id = ?";

        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
            selectStatement.setInt(1, userID);
            selectStatement.setString(2, listingID);

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (!resultSet.next()) {
                    System.out.println("You can only add a comment for a listing you booked.");
                    return;
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while checking booking: " + e.getMessage());
            return;
        }

        String insertQuery = "INSERT INTO Comments (listing_id, user_id, description, rating, timestamp) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, listingID);
            preparedStatement.setInt(2, userID);
            preparedStatement.setString(3, description);
            preparedStatement.setInt(4, rating);

            // Generate timestamp based on current time
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            preparedStatement.setTimestamp(5, timestamp);

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected > 0 ? "Comment added successfully!" : "Failed to add comment.");
        } catch (SQLException e) {
            System.out.println("An error occurred while adding the comment: " + e.getMessage());
        }
    }

    public static void addHostComment(Connection connection, int hostId, String listingId, String renterName,
                                      String description, int rating) throws SQLException {
        String selectQuery = "SELECT * FROM user_listings WHERE user_id = ? AND listing_id = ?";

        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
            selectStatement.setInt(1, hostId);
            selectStatement.setString(2, listingId);

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (!resultSet.next()) {
                    System.out.println("You can only add a comment for a listing you created.");
                    return;
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while checking user listing: " + e.getMessage());
            return;
        }

        String insertQuery = "INSERT INTO hostComments (listing_id, renter_name, description, rating, timestamp) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, listingId);
            preparedStatement.setString(2, renterName);
            preparedStatement.setString(3, description);
            preparedStatement.setInt(4, rating);

            // Generate timestamp based on current time
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            preparedStatement.setTimestamp(5, timestamp);

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected > 0 ? "Comment added successfully!" : "Failed to add comment.");
        } catch (SQLException e) {
            System.out.println("An error occurred while adding the comment: " + e.getMessage());
        }
    }



}
