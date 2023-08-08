import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.sql.*;
public class AdminOptionPage {
    private static Scanner scanner = new Scanner(System.in);

    public static void displayOptions(Connection connection) {
        System.out.println("Welcome, admin! You have additional options here.");
        boolean running = true;

        while (running) {
            System.out.println("Select an admin option:");
            System.out.println("1. Create List");
            System.out.println("2. View your own list");
            System.out.println("3. Remove your list");
            System.out.println("4. Modify your list");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            if (choice == 1) {
                performAdminOption1(connection);
            } else if (choice == 2) {
                performAdminOption2(connection);
            } else if (choice == 3) {
                performAdminOption3(connection);
            } else if (choice == 4) {
                performAdminOption4(connection);
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }

        System.out.println("Exiting the Admin Options Page. Goodbye, admin!");
    }

    private static void performAdminOption1(Connection connection) {
        try {
            System.out.println("Create a new listing:");
            Scanner scanner = new Scanner(System.in);
            // Ask the admin to enter the listing details
            System.out.print("Enter listing type (house, apartment, room): ");
            String type = scanner.nextLine().trim();

            System.out.print("Enter latitude: ");
            double latitude = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter longitude: ");
            double longitude = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter address: ");
            String address = scanner.nextLine();

            System.out.print("Enter postal code: ");
            String postalCode = scanner.nextLine();

            System.out.print("Enter city: ");
            String city = scanner.nextLine();

            System.out.print("Enter country: ");
            String country = scanner.nextLine();

            System.out.print("Enter amenities: ");
            String amenities = scanner.nextLine();

            System.out.print("Enter price: ");
            double price = Double.parseDouble(scanner.nextLine());

            // Prepare the SQL query to insert the new listing
            String insertQuery = "INSERT INTO listings (type, latitude, longitude, address, postal_code, city, country, amenities, price) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, type);
            preparedStatement.setDouble(2, latitude);
            preparedStatement.setDouble(3, longitude);
            preparedStatement.setString(4, address);
            preparedStatement.setString(5, postalCode);
            preparedStatement.setString(6, city);
            preparedStatement.setString(7, country);
            preparedStatement.setString(8, amenities);
            preparedStatement.setDouble(9, price);

            // Execute the SQL query to insert the new listing
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Get the ID of the inserted listing
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                int listingId = -1;
                if (generatedKeys.next()) {
                    listingId = generatedKeys.getInt(1);
                }

                // Insert the association into the user_listings table
                if (listingId > 0) {
                    insertUserListingAssociation(connection, UserContext.getLoggedInUserId(), listingId);
                    System.out.println(UserContext.getLoggedInUsername());
                }

                System.out.println("New listing added successfully!");
            } else {
                System.out.println("Failed to add the listing. Please try again.");
            }

            // Close the prepared statement
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void performAdminOption2(Connection connection) {
        try {
            // Get the currently logged-in admin user's ID from the UserContext
            int adminUserId = UserContext.getLoggedInUserId();

            // Prepare the SQL query to fetch listing IDs associated with the admin user
            String selectListingsQuery = "SELECT listing_id FROM user_listings WHERE user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(selectListingsQuery);
            preparedStatement.setInt(1, adminUserId);

            // Execute the SQL query to fetch the listing IDs
            ResultSet resultSet = preparedStatement.executeQuery();

            // Store the fetched listing IDs in a list
            List<Integer> listingIds = new ArrayList<>();
            while (resultSet.next()) {
                int listingId = resultSet.getInt("listing_id");
                listingIds.add(listingId);
            }

            // Close the result set and prepared statement
            resultSet.close();
            preparedStatement.close();

            // If there are no listings associated with the admin user, display a message and return
            if (listingIds.isEmpty()) {
                System.out.println("You have no listings associated with your account.");
                return;
            }

            // Prepare the SQL query to fetch listing details based on listing IDs
            String selectListingDetailsQuery = "SELECT * FROM listings WHERE listing_id IN (";
            for (int i = 0; i < listingIds.size(); i++) {
                selectListingDetailsQuery += "?";
                if (i < listingIds.size() - 1) {
                    selectListingDetailsQuery += ",";
                }
            }
            selectListingDetailsQuery += ")";
            PreparedStatement listingDetailsStatement = connection.prepareStatement(selectListingDetailsQuery);

            // Set the listing IDs as parameters for the prepared statement
            for (int i = 0; i < listingIds.size(); i++) {
                listingDetailsStatement.setInt(i + 1, listingIds.get(i));
            }

            // Execute the SQL query to fetch listing details
            ResultSet listingDetailsResultSet = listingDetailsStatement.executeQuery();

            // Display the listing details to the admin user
            System.out.println("Listings associated with your account:");
            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-------------+");
            System.out.println("| Listing ID | Type           | Latitude      | Longitude      | Address                      | Postal   | City          | Country                 | Price       |");
            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-------------+");

            while (listingDetailsResultSet.next()) {
                int listingId = listingDetailsResultSet.getInt("listing_id");
                String type = listingDetailsResultSet.getString("type");
                double latitude = listingDetailsResultSet.getDouble("latitude");
                double longitude = listingDetailsResultSet.getDouble("longitude");
                String address = listingDetailsResultSet.getString("address");
                String postalCode = listingDetailsResultSet.getString("postal_code");
                String city = listingDetailsResultSet.getString("city");
                String country = listingDetailsResultSet.getString("country");
                double price = listingDetailsResultSet.getDouble("price");

                System.out.printf("| %-10s | %-14s | %-13.6f | %-14.6f | %-28s | %-8s | %-13s | %-24s | $%9.2f |\n",
                        listingId, type, latitude, longitude, address, postalCode, city, country, price);
            }

            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-------------+");

            // Close the result set and prepared statement
            listingDetailsResultSet.close();
            listingDetailsStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void performAdminOption3(Connection connection) {
        try {
            System.out.println("Delete a listing:");

            Scanner scanner = new Scanner(System.in);

            // Ask the admin to enter the listing ID to be deleted
            System.out.print("Enter the listing ID to delete: ");
            int listingId = scanner.nextInt();

            // Check if the listing ID exists in the "listings" table
            if (isListingExists(connection, listingId)) {
                // Check if the admin owns the listing (is associated with the listing) in the "user_listings" table
                if (isAdminOwnsListing(connection, UserContext.getLoggedInUserId(), listingId)) {
                    // Perform the deletion operation
                    deleteListing(connection, listingId);
                    System.out.println("Listing deleted successfully.");
                } else {
                    System.out.println("You don't own this listing. Cannot delete.");
                }
            } else {
                System.out.println("Invalid listing ID. Listing not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean isListingExists(Connection connection, int listingId) throws SQLException {
        String selectQuery = "SELECT listing_id FROM listings WHERE listing_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
        preparedStatement.setInt(1, listingId);

        ResultSet resultSet = preparedStatement.executeQuery();
        boolean listingExists = resultSet.next();

        resultSet.close();
        preparedStatement.close();

        return listingExists;
    }

    private static boolean isAdminOwnsListing(Connection connection, int userId, int listingId) throws SQLException {
        String selectQuery = "SELECT user_id FROM user_listings WHERE user_id = ? AND listing_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
        preparedStatement.setInt(1, userId);
        preparedStatement.setInt(2, listingId);

        ResultSet resultSet = preparedStatement.executeQuery();
        boolean isAdminOwnsListing = resultSet.next();

        resultSet.close();
        preparedStatement.close();

        return isAdminOwnsListing;
    }

    private static void deleteListing(Connection connection, int listingId) throws SQLException {
        // Delete the listing association from the "user_listings" table
        String deleteUserListingQuery = "DELETE FROM user_listings WHERE listing_id = ?";
        PreparedStatement deleteUserListingStatement = connection.prepareStatement(deleteUserListingQuery);
        deleteUserListingStatement.setInt(1, listingId);
        deleteUserListingStatement.executeUpdate();
        deleteUserListingStatement.close();

        // Delete the listing from the "listings" table
        String deleteListingQuery = "DELETE FROM listings WHERE listing_id = ?";
        PreparedStatement deleteListingStatement = connection.prepareStatement(deleteListingQuery);
        deleteListingStatement.setInt(1, listingId);
        int rowsAffected = deleteListingStatement.executeUpdate();
        deleteListingStatement.close();

        if (rowsAffected > 0) {
            System.out.println("Listing deleted successfully.");
        } else {
            System.out.println("Listing deletion failed. Listing not found.");
        }
    }


    private static void performAdminOption4(Connection connection) {
        try {

                System.out.println("Modify your listing:");

                Scanner scanner = new Scanner(System.in);

                // Ask the admin to enter the listing ID to be modified
                System.out.print("Enter the listing ID to modify: ");
                int listingId = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

            // Check if the listing ID exists in the "listings" table
            if (isListingExists(connection, listingId)) {
                // Check if the admin owns the listing (is associated with the listing) in the "user_listings" table
                if (isAdminOwnsListing(connection, UserContext.getLoggedInUserId(), listingId)) {
                    // Allow the admin to modify the listing details
                    System.out.println("Enter the new details for the listing:");
                    System.out.print("Enter listing type (house, apartment, room): ");
                    String type = scanner.nextLine().trim();

                    System.out.print("Enter latitude: ");
                    double latitude = Double.parseDouble(scanner.nextLine());

                    System.out.print("Enter longitude: ");
                    double longitude = Double.parseDouble(scanner.nextLine());

                    System.out.print("Enter address: ");
                    String address = scanner.nextLine();

                    System.out.print("Enter postal code: ");
                    String postalCode = scanner.nextLine();

                    System.out.print("Enter city: ");
                    String city = scanner.nextLine();

                    System.out.print("Enter country: ");
                    String country = scanner.nextLine();

                    System.out.print("Enter amenities: ");
                    String amenities = scanner.nextLine();

                    System.out.print("Enter price: ");
                    double price = Double.parseDouble(scanner.nextLine());

                    // Prepare the SQL query to update the listing details
                    String updateQuery = "UPDATE listings SET type = ?, latitude = ?, longitude = ?, address = ?, postal_code = ?, " +
                            "city = ?, country = ?, amenities = ?, price = ? WHERE listing_id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
                    preparedStatement.setString(1, type);
                    preparedStatement.setDouble(2, latitude);
                    preparedStatement.setDouble(3, longitude);
                    preparedStatement.setString(4, address);
                    preparedStatement.setString(5, postalCode);
                    preparedStatement.setString(6, city);
                    preparedStatement.setString(7, country);
                    preparedStatement.setString(8, amenities);
                    preparedStatement.setDouble(9, price);
                    preparedStatement.setInt(10, listingId);

                    // Execute the SQL query to update the listing
                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("Listing updated successfully.");
                    } else {
                        System.out.println("Failed to update the listing. Please try again.");
                    }

                    // Close the prepared statement
                    preparedStatement.close();
                } else {
                    System.out.println("You don't own this listing. Cannot modify.");
                }
            } else {
                System.out.println("Invalid listing ID. Listing not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    private static void insertUserListingAssociation(Connection connection, int userId, int listingId) throws SQLException {
        String insertQuery = "INSERT INTO user_listings (user_id, listing_id) VALUES (?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.setInt(1, userId);
        preparedStatement.setInt(2, listingId);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
}
