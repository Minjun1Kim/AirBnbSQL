import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
            System.out.println("1. Create a listing");
            System.out.println("2. View your listings");
            System.out.println("3. Remove your listing");
            System.out.println("4. Modify your listing");
            System.out.println("5. View bookings for your listings");
            System.out.println("6. Add a comment/review on renter");
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
            } else if (choice == 5) {
                performAdminOption5(connection);
            } else if (choice == 6) {
                promptHostComment(connection);
            }else {
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

            System.out.print("Enter amenities (comma-separated | put none if not applicable): ");
            String amenities = scanner.nextLine();

            System.out.print("Enter price: ");
            double price = Double.parseDouble(scanner.nextLine());

            // Prepare the SQL query to insert the new listing
            String insertQuery = "INSERT INTO listings (type, latitude, longitude, address, postal_code, city, country, price) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, type);
            preparedStatement.setDouble(2, latitude);
            preparedStatement.setDouble(3, longitude);
            preparedStatement.setString(4, address);
            preparedStatement.setString(5, postalCode);
            preparedStatement.setString(6, city);
            preparedStatement.setString(7, country);
            preparedStatement.setDouble(8, price);

            // Execute the SQL query to insert the new listing
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Get the ID of the inserted listing
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                int listingId = -1;
                if (generatedKeys.next()) {
                    listingId = generatedKeys.getInt(1);
                }

                if (listingId > 0) {
                    // Split the amenities string into individual amenities
                    String[] amenitiesArray = amenities.split(",");

                    // Insert each amenity into the amenities table and get the amenity_id
                    for (String amenity : amenitiesArray) {
                        amenity = amenity.trim();
                        int amenityId = insertAmenity(connection, amenity);

                        // Insert the listing-amenity association into the listings_amenities table
                        insertListingAmenity(connection, listingId, amenityId, amenity);
                    }

                    insertUserListingAssociation(connection, UserContext.getLoggedInUserId(), listingId);
                    //System.out.println(UserContext.getLoggedInUsername());
                }

                System.out.println("New listing added successfully!");
                double suggestedPrice = suggestAveragePrice(connection, type, listingId);
                System.out.println("*****************************************************************************************************************************");
                System.out.println("According to our calculation considering various factors:");
                System.out.println("Average price for " + type + " in current market is (excluding yours): $" + suggestedPrice + " - $"+(suggestedPrice+49.723));
                System.out.println("*****************************************************************************************************************************");
                System.out.println("We recommend considering the following amenities to enhance your listing's appeal as other users also did:");

                List<String> recommendedAmenities = recommendAmenities(connection, 1);
                for (String amenity : recommendedAmenities) {
                    System.out.println("- " + amenity);
                }
                System.out.println("*****************************************************************************************************************************");

            } else {
                System.out.println("Failed to add the listing. Please try again.");
            }

            // Close the prepared statement
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static List<String> recommendAmenities(Connection connection, int count) throws SQLException {
        List<String> recommendedAmenities = new ArrayList<>();

        // Prepare the SQL query to select top N amenities
        String selectQuery = "SELECT amenity_name FROM amenities ORDER BY RAND() LIMIT ?";
        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
        preparedStatement.setInt(1, count);

        // Execute the SQL query
        ResultSet resultSet = preparedStatement.executeQuery();

        // Add the recommended amenities to the list
        while (resultSet.next()) {
            recommendedAmenities.add(resultSet.getString("amenity_name"));
        }

        return recommendedAmenities;
    }

    private static double suggestAveragePrice(Connection connection, String listingType, int excludedListingId) throws SQLException {
        String selectQuery = "SELECT AVG(price) AS average_price FROM listings WHERE type = ? AND listing_id <> ?";
        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
        preparedStatement.setString(1, listingType);
        preparedStatement.setInt(2, excludedListingId);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            return resultSet.getDouble("average_price");
        }

        return 0; // Default value if no average price is found
    }

    private static int getAmenityId(Connection connection, String amenityName) throws SQLException {
        String selectQuery = "SELECT amenity_id FROM amenities WHERE amenity_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, amenityName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("amenity_id");
                } else {
                    return -1; // Amenity does not exist
                }
            }
        }
    }
    private static void insertListingAmenity(Connection connection, int listingId, int amenityId, String amenityName) throws SQLException {
        String insertQuery = "INSERT INTO listings_amenities (listing_id, amenity_id, amenity_name) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.setInt(1, listingId);
        preparedStatement.setInt(2, amenityId);
        preparedStatement.setString(3, amenityName);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }


    private static int insertAmenity(Connection connection, String amenityName) throws SQLException {
        // Check if the amenity already exists in the amenities table
        String selectQuery = "SELECT amenity_id FROM amenities WHERE amenity_name = ?";
        PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
        selectStatement.setString(1, amenityName);
        ResultSet resultSet = selectStatement.executeQuery();

        if (resultSet.next()) {
            // Amenity already exists, return its ID
            int amenityId = resultSet.getInt("amenity_id");
            resultSet.close();
            selectStatement.close();
            return amenityId;
        } else {
            // Amenity does not exist, insert it into the amenities table
            resultSet.close();
            selectStatement.close();

            String insertQuery = "INSERT INTO amenities (amenity_name) VALUES (?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            insertStatement.setString(1, amenityName);

            // Execute the SQL query to insert the amenity
            int rowsAffected = insertStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Get the ID of the inserted amenity
                ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                int amenityId = -1;
                if (generatedKeys.next()) {
                    amenityId = generatedKeys.getInt(1);
                }
                generatedKeys.close();
                insertStatement.close();
                return amenityId;
            } else {
                insertStatement.close();
                throw new SQLException("Failed to insert the amenity.");
            }
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

            // Prepare the SQL query to fetch listing details and associated amenities based on listing IDs
            String selectListingDetailsQuery = "SELECT l.listing_id, l.type, l.latitude, l.longitude, l.address, l.postal_code, l.city, l.country, l.price, GROUP_CONCAT(a.amenity_name SEPARATOR ', ') as amenities " +
                    "FROM listings l " +
                    "LEFT JOIN listings_amenities la ON l.listing_id = la.listing_id " +
                    "LEFT JOIN amenities a ON la.amenity_name = a.amenity_name " +
                    "WHERE l.listing_id IN (";
            for (int i = 0; i < listingIds.size(); i++) {
                selectListingDetailsQuery += "?";
                if (i < listingIds.size() - 1) {
                    selectListingDetailsQuery += ",";
                }
            }
            selectListingDetailsQuery += ") GROUP BY l.listing_id"; // Group the results by listing_id
            PreparedStatement listingDetailsStatement = connection.prepareStatement(selectListingDetailsQuery);

            // Set the listing IDs as parameters for the prepared statement
            for (int i = 0; i < listingIds.size(); i++) {
                listingDetailsStatement.setInt(i + 1, listingIds.get(i));
            }

            // Execute the SQL query to fetch listing details and associated amenities
            ResultSet listingDetailsResultSet = listingDetailsStatement.executeQuery();

            // Display the listing details and associated amenities to the admin user
            System.out.println("Listings associated with your account:");
            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-------------+------------------------------+");
            System.out.println("| Listing ID | Type           | Latitude      | Longitude      | Address                      | Postal   | City          | Country                 | Price       | Amenities                    |");
            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-------------+------------------------------+");

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
                String amenities = listingDetailsResultSet.getString("amenities");

                System.out.printf("| %-10s | %-14s | %-13.6f | %-14.6f | %-28s | %-8s | %-13s | %-24s | $%9.2f | %-28s |\n",
                        listingId, type, latitude, longitude, address, postalCode, city, country, price, amenities);
            }

            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-------------+------------------------------+");

            // Close the result set and prepared statement
            listingDetailsResultSet.close();
            listingDetailsStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private static void performAdminOption3(Connection connection) {
        try {
            System.out.println("Delete a listing from:");
            performAdminOption2(connection);
            Scanner scanner = new Scanner(System.in);

            // Ask the admin to enter the listing ID to be deleted
            System.out.print("Enter the listing ID to delete: ");
            int listingId = scanner.nextInt();

            // Check if the listing ID exists in the "listings" table
            if (isListingExists(connection, listingId)) {
                // Check if the admin owns the listing (is associated with the listing) in the "user_listings" table
                if (isAdminOwnsListing(connection, UserContext.getLoggedInUserId(), listingId)) {
                    // Check if the listing is booked
                    if (!isListingBooked(connection, listingId)) {
                        // Perform the deletion operation
                        deleteListing(connection, listingId);

                        // Update the deletion count for the admin user
                        int userId = UserContext.getLoggedInUserId();
                        updateDeletionCount(connection, userId);

                        System.out.println("Listing deleted successfully.");
                    } else {
                        System.out.println("The listing is currently booked and cannot be deleted.");
                    }
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

    private static void updateDeletionCount(Connection connection, int userId) throws SQLException {
        String selectQuery = "SELECT deletion_count FROM user_deletions WHERE user_id = ?";
        String insertQuery = "INSERT INTO user_deletions (user_id, deletion_count) VALUES (?, 1)";
        String updateQuery = "UPDATE user_deletions SET deletion_count = ? WHERE user_id = ?";
        PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
        PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
        PreparedStatement updateStatement = connection.prepareStatement(updateQuery);

        selectStatement.setInt(1, userId);
        ResultSet resultSet = selectStatement.executeQuery();

        if (resultSet.next()) {
            int currentDeletionCount = resultSet.getInt("deletion_count");
            int newDeletionCount = currentDeletionCount + 1;

            updateStatement.setInt(1, newDeletionCount);
            updateStatement.setInt(2, userId);
            updateStatement.executeUpdate();
        } else {
            // If the user record does not exist, insert a new record with deletion_count = 1
            insertStatement.setInt(1, userId);
            insertStatement.executeUpdate();
        }

        resultSet.close();
        selectStatement.close();
        insertStatement.close();
        updateStatement.close();
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
        try {
            connection.setAutoCommit(false); // Set auto-commit to false

            // Delete the listing association from the "user_listings" table
            String deleteUserListingQuery = "DELETE FROM user_listings WHERE listing_id = ?";
            PreparedStatement deleteUserListingStatement = connection.prepareStatement(deleteUserListingQuery);
            deleteUserListingStatement.setInt(1, listingId);
            deleteUserListingStatement.executeUpdate();
            deleteUserListingStatement.close();

            // Delete the associated amenities from the "listings_amenities" table
            String deleteListingAmenitiesQuery = "DELETE FROM listings_amenities WHERE listing_id = ?";
            PreparedStatement deleteListingAmenitiesStatement = connection.prepareStatement(deleteListingAmenitiesQuery);
            deleteListingAmenitiesStatement.setInt(1, listingId);
            deleteListingAmenitiesStatement.executeUpdate();
            deleteListingAmenitiesStatement.close();

            // Get the amenity IDs associated with the listing from the "listings_amenities" table
            String selectAmenityIdsQuery = "SELECT amenity_id FROM listings_amenities WHERE listing_id = ?";
            PreparedStatement selectAmenityIdsStatement = connection.prepareStatement(selectAmenityIdsQuery);
            selectAmenityIdsStatement.setInt(1, listingId);
            ResultSet resultSet = selectAmenityIdsStatement.executeQuery();

            // Delete the associated amenities from the "amenities" table
            String deleteAmenitiesQuery = "DELETE FROM amenities WHERE amenity_id = ?";
            PreparedStatement deleteAmenitiesStatement = connection.prepareStatement(deleteAmenitiesQuery);
            while (resultSet.next()) {
                int amenityId = resultSet.getInt("amenity_id");
                deleteAmenitiesStatement.setInt(1, amenityId);
                deleteAmenitiesStatement.executeUpdate();
            }

            resultSet.close();
            selectAmenityIdsStatement.close();
            deleteAmenitiesStatement.close();

            // Delete the listing from the "listings" table
            String deleteListingQuery = "DELETE FROM listings WHERE listing_id = ?";
            PreparedStatement deleteListingStatement = connection.prepareStatement(deleteListingQuery);
            deleteListingStatement.setInt(1, listingId);
            int rowsAffected = deleteListingStatement.executeUpdate();
            deleteListingStatement.close();

            if (rowsAffected > 0) {
                connection.commit(); // Commit the changes
                System.out.println("Listing deleted successfully.");
            } else {
                connection.rollback(); // Rollback the changes
                System.out.println("Listing deletion failed. Listing not found.");
            }
        } catch (SQLException e) {
            connection.rollback(); // Rollback the changes on exception
            e.printStackTrace();
        } finally {
            connection.setAutoCommit(true); // Set auto-commit back to true
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
            if (isListingBooked(connection, listingId)) {
                System.out.println("The listing is currently booked and cannot be modified");
            } else {


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

                        System.out.print("Enter amenities (comma-separated): ");
                        String amenities = scanner.nextLine();

                        System.out.print("Enter price: ");
                        double price = Double.parseDouble(scanner.nextLine());

                        // Prepare the SQL query to update the listing details
                        String updateQuery = "UPDATE listings SET type = ?, latitude = ?, longitude = ?, address = ?, postal_code = ?, " +
                                "city = ?, country = ?, price = ? WHERE listing_id = ?";
                        PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
                        preparedStatement.setString(1, type);
                        preparedStatement.setDouble(2, latitude);
                        preparedStatement.setDouble(3, longitude);
                        preparedStatement.setString(4, address);
                        preparedStatement.setString(5, postalCode);
                        preparedStatement.setString(6, city);
                        preparedStatement.setString(7, country);
                        preparedStatement.setDouble(8, price);
                        preparedStatement.setInt(9, listingId);

                        // Execute the SQL query to update the listing
                        int rowsAffected = preparedStatement.executeUpdate();

                        if (rowsAffected > 0) {
                            // Delete existing listing-amenity associations
                            deleteListingAmenities(connection, listingId);

                            // Split the amenities string into individual amenities
                            String[] amenitiesArray = amenities.split(",");

                            // Insert each amenity into the amenities table and create listing-amenity associations
                            for (String amenity : amenitiesArray) {
                                amenity = amenity.trim();
                                int amenityId = insertAmenity(connection, amenity);
                                insertListingAmenity(connection, listingId, amenityId, amenity);
                            }

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
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

    }



    private static void deleteListingAmenities(Connection connection, int listingId) throws SQLException {
        String deleteListingAmenitiesQuery = "DELETE FROM listings_amenities WHERE listing_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(deleteListingAmenitiesQuery);
        preparedStatement.setInt(1, listingId);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }


    private static void insertUserListingAssociation(Connection connection, int userId, int listingId) throws SQLException {
        String insertQuery = "INSERT INTO user_listings (user_id, listing_id) VALUES (?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.setInt(1, userId);
        preparedStatement.setInt(2, listingId);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    private static void performAdminOption5(Connection connection) {
        try {
            // Get the current user ID from the UserContext
            int currentUserId = UserContext.getLoggedInUserId();

            // Query to get all listings owned by the current user
            String selectQuery = "SELECT listing_id FROM user_listings WHERE user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setInt(1, currentUserId);

            // Retrieve the result set
            ResultSet resultSet = preparedStatement.executeQuery();

            // Store the listing IDs in a list
            List<Integer> listingIds = new ArrayList<>();
            while (resultSet.next()) {
                int listingId = resultSet.getInt("listing_id");
                listingIds.add(listingId);
            }

            // If there are no listings owned by the current user, inform the user and return
            if (listingIds.isEmpty()) {
                System.out.println("You don't have any listings.");
                return;
            }

            // Query to get bookings of the listings owned by the current user
            String bookingsQuery = "SELECT b.booking_id, b.listing_id, l.type, l.latitude, l.longitude, l.address, l.postal_code, l.city, l.country, l.price, b.start_date, b.end_date " +
                    "FROM bookings b " +
                    "JOIN listings l ON b.listing_id = l.listing_id " +
                    "WHERE b.listing_id IN (" + String.join(",", Collections.nCopies(listingIds.size(), "?")) + ")";
            PreparedStatement bookingsStatement = connection.prepareStatement(bookingsQuery);

            // Set the listing IDs as parameters for the query
            for (int i = 0; i < listingIds.size(); i++) {
                bookingsStatement.setInt(i + 1, listingIds.get(i));
            }

            // Retrieve the result set
            ResultSet bookingsResultSet = bookingsStatement.executeQuery();

            // Display the bookings of the listings
            System.out.println("+------------+------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+----------------------+");
            System.out.println("| Booking ID | Listing ID | Type          | Latitude       | Longitude         | Address          | Postal Code   | City     | Country     | Price      | start_data | end_data   |");
            System.out.println("+------------+------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+----------------------+");

            while (bookingsResultSet.next()) {
                int bookingId = bookingsResultSet.getInt("booking_id");
                int listingId = bookingsResultSet.getInt("listing_id");
                String type = bookingsResultSet.getString("type");
                double latitude = bookingsResultSet.getDouble("latitude");
                double longitude = bookingsResultSet.getDouble("longitude");
                String address = bookingsResultSet.getString("address");
                String postalCode = bookingsResultSet.getString("postal_code");
                String city = bookingsResultSet.getString("city");
                String country = bookingsResultSet.getString("country");
                double price = bookingsResultSet.getDouble("price");
                Date startDate = bookingsResultSet.getDate("start_date");
                Date endDate = bookingsResultSet.getDate("end_date");

                System.out.printf("| %-10s | %-10s | %-14s| %-13.6f  | %-14.6f    | %-10s       | %-12s  | %-5s      | %-10s| $%.2f     | %s | %-10s |\n",
                        bookingId, listingId, type, latitude, longitude, address, postalCode, city, country, price, startDate, endDate);



            }

            System.out.println("+------------+------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+----------------------+");

            resultSet.close();
            preparedStatement.close();
            bookingsResultSet.close();
            bookingsStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean isListingBooked(Connection connection, int listingId) throws SQLException {
        String selectQuery = "SELECT booking_id FROM bookings WHERE listing_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
        preparedStatement.setInt(1, listingId);

        ResultSet resultSet = preparedStatement.executeQuery();
        boolean isBooked = resultSet.next();

        resultSet.close();
        preparedStatement.close();

        return isBooked;
    }

    public static void promptHostComment(Connection connection) {
        performAdminOption5(connection);

        System.out.println("\nLeave a host comment");

        int hostId = UserContext.getLoggedInUserId();

        // Check if the host has any listings
        boolean hasListings = false;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT user_id FROM user_listings WHERE user_id = ?"
            );
            preparedStatement.setInt(1, hostId);
            ResultSet resultSet = preparedStatement.executeQuery();
            hasListings = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!hasListings) {
            System.out.println("The host has not created any listings yet. Unable to leave a host comment.");
            return; // Return if the host has no listings
        }

        Scanner scanner = new Scanner(System.in); // Assuming scanner is declared earlier

        String listingId;
        String renterName;
        String description;
        int rating = 0;
        boolean hasBooking = false;
        boolean listingExists = false;

        do {
            System.out.print("Listing id: ");
            listingId = scanner.next();

            System.out.print("Renter's user name: ");
            renterName = scanner.next();

            System.out.print("Enter description: ");
            scanner.nextLine(); // Consume newline
            description = scanner.nextLine();

            System.out.print("Enter rating (1-5): ");
            rating = scanner.nextInt();

            // Check if the host has created a listing with the provided listing_id

            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT user_id FROM user_listings WHERE user_id = ? AND listing_id = ?"
                );
                preparedStatement.setInt(1, hostId);
                preparedStatement.setString(2, listingId);
                ResultSet resultSet = preparedStatement.executeQuery();
                listingExists = resultSet.next();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (!listingExists) {
                System.out.println("The host has not created a listing with the provided listing_id.");
                continue;
            }

            // Check if there is a booking associated with the provided listing

            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT booking_id FROM bookings WHERE listing_id = ?"
                );
                preparedStatement.setString(1, listingId);
                ResultSet resultSet = preparedStatement.executeQuery();
                hasBooking = resultSet.next();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (!hasBooking) {
                System.out.println("No booking associated with the provided listing_id. Unable to leave a host comment.");
                return;
            }

        } while (listingId.isEmpty() || renterName.isEmpty() || description.isEmpty() || rating < 1 || rating > 5 || !listingExists || !hasBooking);

        try {
            if (!Main.checkIfTableExists(connection, "hostComments")) {
                Comments.createHostCommentsTable(connection);
            }
            Comments.addHostComment(connection, hostId, listingId, renterName, description, rating);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
