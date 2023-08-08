import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.sql.*;
import java.util.Date;

public class OptionPage {
    private static Scanner scanner = new Scanner(System.in);

    public static void displayOptions(Connection connection) throws SQLException {
        System.out.println("Welcome to the Options Page!");
        boolean running = true;

        while (running) {
            System.out.println("Select an option:");
            System.out.println("1. View listings");
            System.out.println("2. Search for listings");
            System.out.println("3. Book a listing");
            System.out.println("4. Check Availabilities");
            System.out.println("5. Check Your Bookings");
            System.out.println("6. Cancel Your Bookings");
            System.out.println("7. Advanced Search (Coordinates, Zipcode, Prices");
            System.out.println("8. Add a comment");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            if (choice == 1) {
                performOption1(connection);
            } else if (choice == 2) {
                performOption2(connection);
            } else if (choice == 3) {
                performOption3(connection);
            } else if (choice == 4) {
                performOption4(connection);
            } else if (choice == 5) {
                performOption5(connection);
            } else if (choice == 6) {
                performOption6(connection);
            } else if (choice == 7) {
                advancedSearch(connection);
            } else if (choice == 8) {
                promptComment(connection);
            }else {
                System.out.println("Invalid choice. Please try again.");
            }
        }

        System.out.println("Exiting the Options Page. Goodbye!");
    }



    private static void performOption1(Connection connection) {
        try {
            System.out.println("Listing Information:");

            // Execute the SQL query to fetch all listings and their associated amenities
            String selectQuery = "SELECT l.listing_id, l.type, l.latitude, l.longitude, l.address, l.postal_code, l.city, l.country, l.price, GROUP_CONCAT(a.amenity_name SEPARATOR ', ') as amenities " +
                    "FROM listings l " +
                    "LEFT JOIN listings_amenities la ON l.listing_id = la.listing_id " +
                    "LEFT JOIN amenities a ON la.amenity_name = a.amenity_name " +
                    "GROUP BY l.listing_id";
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

            // Retrieve the result set
            ResultSet resultSet = preparedStatement.executeQuery();

            // Display the listing information and associated amenities in a grid-like format using printf
            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+------------------------------+");
            System.out.println("| Listing ID | Type           | Latitude      | Longitude      | Address                      | Postal   | City          | Country                 | Price     | Amenities                    |");
            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+------------------------------+");

            while (resultSet.next()) {
                int listingId = resultSet.getInt("listing_id");
                String type = resultSet.getString("type");
                double latitude = resultSet.getDouble("latitude");
                double longitude = resultSet.getDouble("longitude");
                String address = resultSet.getString("address");
                String postalCode = resultSet.getString("postal_code");
                String city = resultSet.getString("city");
                String country = resultSet.getString("country");
                double price = resultSet.getDouble("price");
                String amenities = resultSet.getString("amenities");

                System.out.printf("| %-10s | %-14s | %-13.6f | %-14.6f | %-28s | %-8s | %-13s | %-24s | $%7.2f | %-28s |\n",
                        listingId, type, latitude, longitude, address, postalCode, city, country, price, amenities);
            }

            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+------------------------------+");

            // Close the result set and prepared statement
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static void performOption2(Connection connection) {
        try {
            System.out.println("Search Listings:");

            Scanner scanner = new Scanner(System.in);

            // Ask the user to enter the search criteria
            System.out.print("Enter the listing type (house, apartment, room) [Leave empty for any type]: ");
            String listingType = scanner.nextLine().trim();

            System.out.print("Enter the city [Leave empty for any city]: ");
            String city = scanner.nextLine().trim();

            System.out.print("Enter the country [Leave empty for any country]: ");
            String country = scanner.nextLine().trim();

            System.out.print("Enter the minimum price [Leave empty for any price]: ");
            String minPriceInput = scanner.nextLine().trim();
            Double minPrice = null;
            if (!minPriceInput.isEmpty()) {
                minPrice = Double.parseDouble(minPriceInput);
            }

            System.out.print("Enter the maximum price [Leave empty for any price]: ");
            String maxPriceInput = scanner.nextLine().trim();
            Double maxPrice = null;
            if (!maxPriceInput.isEmpty()) {
                maxPrice = Double.parseDouble(maxPriceInput);
            }

            System.out.print("Enter amenities (separated by comma) [Leave empty for any amenities]: ");
            String amenitiesInput = scanner.nextLine().trim();
            List<String> amenitiesList = new ArrayList<>();
            boolean amenitiesProvided = !amenitiesInput.isEmpty();
            if (amenitiesProvided) {
                amenitiesList = Arrays.asList(amenitiesInput.split(","));
            } else {
                // Retrieve all amenities from the 'amenities' table
                try (Statement statement = connection.createStatement()) {
                    ResultSet amenitiesResultSet = statement.executeQuery("SELECT amenity_name FROM amenities");
                    while (amenitiesResultSet.next()) {
                        String amenityName = amenitiesResultSet.getString("amenity_name");
                        amenitiesList.add(amenityName);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            // Prepare the SQL query dynamically based on user input
            StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT l.* FROM listings l");
            if (amenitiesProvided) {
                queryBuilder.append(" INNER JOIN listings_amenities la ON l.listing_id = la.listing_id");
            }

            queryBuilder.append(" WHERE 1=1");

            if (!listingType.isEmpty()) {
                queryBuilder.append(" AND l.type = ?");
            }
            if (!city.isEmpty()) {
                queryBuilder.append(" AND l.city = ?");
            }
            if (!country.isEmpty()) {
                queryBuilder.append(" AND l.country = ?");
            }
            if (minPrice != null) {
                queryBuilder.append(" AND l.price >= ?");
            }
            if (maxPrice != null) {
                queryBuilder.append(" AND l.price <= ?");
            }
            if (amenitiesProvided) {
                queryBuilder.append(" AND la.amenity_name IN (");
                for (int i = 0; i < amenitiesList.size(); i++) {
                    if (i > 0) {
                        queryBuilder.append(", ");
                    }
                    queryBuilder.append("?");
                }
                queryBuilder.append(")");
            }


            // Execute the SQL query to fetch the matching listings
            PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString());

            int parameterIndex = 1;
            if (!listingType.isEmpty()) {
                preparedStatement.setString(parameterIndex++, listingType);
            }
            if (!city.isEmpty()) {
                preparedStatement.setString(parameterIndex++, city);
            }
            if (!country.isEmpty()) {
                preparedStatement.setString(parameterIndex++, country);
            }
            if (minPrice != null) {
                preparedStatement.setDouble(parameterIndex++, minPrice);
            }
            if (maxPrice != null) {
                preparedStatement.setDouble(parameterIndex++, maxPrice);
            }
            if (amenitiesProvided) {
                for (String amenity : amenitiesList) {
                    preparedStatement.setString(parameterIndex++, amenity.trim());
                }
            }

            // Retrieve the result set
            ResultSet resultSet = preparedStatement.executeQuery();

            // Display the search results along with all amenities for each listing
            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+-------------------------+");
            System.out.println("| Listing ID | Type           | Latitude      | Longitude      | Address                      | Postal   | City          | Country                 | Price     | Amenities               |");
            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+-------------------------+");

            while (resultSet.next()) {
                int listingId = resultSet.getInt("listing_id");
                String type = resultSet.getString("type");
                double latitude = resultSet.getDouble("latitude");
                double longitude = resultSet.getDouble("longitude");
                String address = resultSet.getString("address");
                String postalCode = resultSet.getString("postal_code");
                String cityName = resultSet.getString("city");
                String countryName = resultSet.getString("country");
                double price = resultSet.getDouble("price");

                // Retrieve all amenities for the current listing ID if amenities were provided
                List<String> listingAmenities = new ArrayList<>();
                if (amenitiesProvided) {
                    PreparedStatement amenitiesStatement = connection.prepareStatement("SELECT amenity_name FROM listings_amenities WHERE listing_id = ?");
                    amenitiesStatement.setInt(1, listingId);
                    ResultSet amenitiesResultSet = amenitiesStatement.executeQuery();
                    while (amenitiesResultSet.next()) {
                        String amenityName = amenitiesResultSet.getString("amenity_name");
                        listingAmenities.add(amenityName);
                    }
                    amenitiesResultSet.close();
                    amenitiesStatement.close();
                }

                System.out.printf("| %-10s | %-14s | %-13.6f | %-14.6f | %-28s | %-8s | %-13s | %-24s | $%9.2f | %-21s |\n",
                        listingId, type, latitude, longitude, address, postalCode, cityName, countryName, price, String.join(", ", listingAmenities));
            }

            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+-------------------------+");

            // Close the result set, prepared statement, and scanner
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    private static void performBooking(Connection connection, int userId) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Booking a Listing:");


            System.out.print("Enter the Listing ID you want to book: ");
            int listingId = scanner.nextInt();


            boolean listingExists = false;
            String checkListingQuery = "SELECT COUNT(*) AS count FROM listings WHERE listing_id = ?";
            PreparedStatement checkListingStatement = connection.prepareStatement(checkListingQuery);
            checkListingStatement.setInt(1, listingId);
            ResultSet listingResult = checkListingStatement.executeQuery();
            if (listingResult.next()) {
                int count = listingResult.getInt("count");
                if (count > 0) {
                    listingExists = true;
                }
            }

            if (!listingExists) {
                System.out.println("Listing with ID " + listingId + " does not exist.");
                return;
            }

            System.out.print("Enter the booking start date (YYYY-MM-DD): ");
            String startDateStr = scanner.next();
            System.out.print("Enter the booking end date (YYYY-MM-DD): ");
            String endDateStr = scanner.next();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);


            if (endDate.before(startDate)) {
                System.out.println("Invalid booking dates. End date should be after start date.");
                return;
            }


            String checkAvailabilityQuery = "SELECT COUNT(*) AS count FROM bookings WHERE listing_id = ? AND ((start_date <= ? AND end_date >= ?) OR (start_date <= ? AND end_date >= ?) OR (start_date >= ? AND end_date <= ?))";
            PreparedStatement checkAvailabilityStatement = connection.prepareStatement(checkAvailabilityQuery);
            checkAvailabilityStatement.setInt(1, listingId);
            checkAvailabilityStatement.setDate(2, new java.sql.Date(startDate.getTime()));
            checkAvailabilityStatement.setDate(3, new java.sql.Date(startDate.getTime()));
            checkAvailabilityStatement.setDate(4, new java.sql.Date(endDate.getTime()));
            checkAvailabilityStatement.setDate(5, new java.sql.Date(endDate.getTime()));
            checkAvailabilityStatement.setDate(6, new java.sql.Date(startDate.getTime()));
            checkAvailabilityStatement.setDate(7, new java.sql.Date(endDate.getTime()));
            ResultSet availabilityResult = checkAvailabilityStatement.executeQuery();
            if (availabilityResult.next()) {
                int count = availabilityResult.getInt("count");
                if (count > 0) {
                    System.out.println("Listing is not available for the selected dates.");
                    return;
                }
            }


            String insertBookingQuery = "INSERT INTO bookings (listing_id, user_id, start_date, end_date) VALUES (?, ?, ?, ?)";
            PreparedStatement insertBookingStatement = connection.prepareStatement(insertBookingQuery);
            insertBookingStatement.setInt(1, listingId);
            insertBookingStatement.setInt(2, userId);
            insertBookingStatement.setDate(3, new java.sql.Date(startDate.getTime()));
            insertBookingStatement.setDate(4, new java.sql.Date(endDate.getTime()));
            insertBookingStatement.executeUpdate();

            System.out.println("Booking successful! Listing with ID " + listingId + " has been booked from " + startDateStr + " to " + endDateStr + ".");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("Invalid date format. Please use the format YYYY-MM-DD.");
        }
    }





    private static void performOption3(Connection connection) {

        System.out.println("You selected Option 3.");
        performOption1(connection);
        // Get the logged-in user ID from the UserContext
        int userId = UserContext.getLoggedInUserId();

        // Perform the booking using the user ID
        performBooking(connection, userId);
    }
    private static void performOption4(Connection connection) {
        List<ListingBooking> bookedListings = fetchBookedListings(connection);
        System.out.println("These are the units that is not available, any other day is available for any user!");
        // Print the header for the table
        System.out.println("+------------+------------+---------------+----------------+------------------------------+------------+---------------+-------------------------+-----------+----------------+----------------+");
        System.out.println("| Listing ID | Type       | Latitude      | Longitude      | Address                      | Postal     | City          | Country                 | Price     | Start Date     | End Date       |");
        System.out.println("+------------+------------+---------------+----------------+------------------------------+------------+---------------+-------------------------+-----------+----------------+----------------+");

        // Print each ListingBooking object in the list
        for (ListingBooking booking : bookedListings) {
            System.out.println(booking);
        }

        // Print the footer for the table
        System.out.println("+------------+------------+---------------+----------------+------------------------------+------------+---------------+-------------------------+-----------+----------------+----------------+");
    }

    private static List<ListingBooking> fetchBookedListings(Connection connection) {
        List<ListingBooking> bookedListings = new ArrayList<>();

        try {
            // Prepare the SQL query to fetch all booked listings
            String selectQuery = "SELECT b.booking_id, l.listing_id, l.type, l.latitude, l.longitude, l.address, l.postal_code, l.city, l.country, l.price, b.start_date, b.end_date " +
                    "FROM bookings b " +
                    "JOIN listings l ON b.listing_id = l.listing_id";
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

            // Execute the SQL query and retrieve the result set
            ResultSet resultSet = preparedStatement.executeQuery();

            // Process the result set and populate the bookedListings list
            while (resultSet.next()) {
                int bookingId = resultSet.getInt("booking_id");
                int listingId = resultSet.getInt("listing_id");
                String type = resultSet.getString("type");
                double latitude = resultSet.getDouble("latitude");
                double longitude = resultSet.getDouble("longitude");
                String address = resultSet.getString("address");
                String postalCode = resultSet.getString("postal_code");
                String city = resultSet.getString("city");
                String country = resultSet.getString("country");
                double price = resultSet.getDouble("price");
                Date startDate = resultSet.getDate("start_date");
                Date endDate = resultSet.getDate("end_date");

                ListingBooking booking = new ListingBooking(bookingId, listingId, type, latitude, longitude, address, postalCode, city, country, price, startDate, endDate);
                bookedListings.add(booking);
            }

            // Close the result set and prepared statement
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bookedListings;
    }

    private static void performOption5(Connection connection) {
        try {
            int loggedInUserId = UserContext.getLoggedInUserId();

            // Execute the SQL query to fetch the bookings associated with the current user
            String selectQuery = "SELECT b.booking_id, b.listing_id, l.type, l.latitude, l.longitude, l.address, l.postal_code, l.city, l.country, l.price, b.start_date, b.end_date " +
                    "FROM bookings b " +
                    "JOIN listings l ON b.listing_id = l.listing_id " +
                    "WHERE b.user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setInt(1, loggedInUserId);

            // Retrieve the result set
            ResultSet resultSet = preparedStatement.executeQuery();

            // Display the current user's bookings in a grid-like format using printf
            System.out.println("Current User's Bookings:");
            System.out.println("+------------+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+------------+------------+");
            System.out.println("| Booking ID | Listing ID | Type           | Latitude      | Longitude      | Address                      | Postal   | City          | Country                 | Price     | Start Date | End Date   |");
            System.out.println("+------------+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+------------+------------+");

            while (resultSet.next()) {
                int bookingId = resultSet.getInt("booking_id");
                int listingId = resultSet.getInt("listing_id");
                String type = resultSet.getString("type");
                double latitude = resultSet.getDouble("latitude");
                double longitude = resultSet.getDouble("longitude");
                String address = resultSet.getString("address");
                String postalCode = resultSet.getString("postal_code");
                String city = resultSet.getString("city");
                String country = resultSet.getString("country");
                double price = resultSet.getDouble("price");
                Date startDate = resultSet.getDate("start_date");
                Date endDate = resultSet.getDate("end_date");

                System.out.printf("| %-10s | %-10s | %-14s | %-13.6f | %-14.6f | %-28s | %-8s | %-13s | %-24s| $%7.2f  | %-10s | %-10s |\n",
                        bookingId, listingId, type, latitude, longitude, address, postalCode, city, country, price, startDate, endDate);
            }

            System.out.println("+------------+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+------------+------------+");

            // Close the result set and prepared statement
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void performOption6(Connection connection) {

        try {
            int loggedInUserId = UserContext.getLoggedInUserId();

            // Execute the SQL query to fetch the bookings associated with the current user
            String selectQuery = "SELECT b.booking_id, b.listing_id, l.type, l.latitude, l.longitude, l.address, l.postal_code, l.city, l.country, l.price, b.start_date, b.end_date " +
                    "FROM bookings b " +
                    "JOIN listings l ON b.listing_id = l.listing_id " +
                    "WHERE b.user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setInt(1, loggedInUserId);

            // Retrieve the result set
            ResultSet resultSet = preparedStatement.executeQuery();

            // Display the current user's bookings in a tabular format using printf
            System.out.println("Current User's Bookings:");
            System.out.println("+------------+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+------------+------------+");
            System.out.println("| Booking ID | Listing ID | Type           | Latitude      | Longitude      | Address                      | Postal   | City          | Country                 | Price     | Start Date | End Date   |");
            System.out.println("+------------+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+------------+------------+");

            while (resultSet.next()) {
                int bookingId = resultSet.getInt("booking_id");
                int listingId = resultSet.getInt("listing_id");
                String type = resultSet.getString("type");
                double latitude = resultSet.getDouble("latitude");
                double longitude = resultSet.getDouble("longitude");
                String address = resultSet.getString("address");
                String postalCode = resultSet.getString("postal_code");
                String city = resultSet.getString("city");
                String country = resultSet.getString("country");
                double price = resultSet.getDouble("price");
                Date startDate = resultSet.getDate("start_date");
                Date endDate = resultSet.getDate("end_date");

                System.out.printf("| %-10s | %-10s | %-14s | %-13.6f | %-14.6f | %-28s | %-8s | %-13s | %-24s| $%7.2f  | %-10s | %-10s |\n",
                        bookingId, listingId, type, latitude, longitude, address, postalCode, city, country, price, startDate, endDate);
            }

            System.out.println("+------------+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+------------+------------+");

            // Close the result set and prepared statement
            resultSet.close();
            preparedStatement.close();

            // Ask the user if they want to cancel a booking
            System.out.print("Enter the Booking ID you want to cancel (or enter 0 to go back): ");
            int bookingIdToCancel = scanner.nextInt();

            if (bookingIdToCancel == 0) {
                return; // User chose to go back, so return without canceling any booking.
            }

            // Check if the booking exists for the current user
            String checkBookingQuery = "SELECT COUNT(*) AS count, start_date, end_date FROM bookings WHERE booking_id = ? AND user_id = ?";
            PreparedStatement checkBookingStatement = connection.prepareStatement(checkBookingQuery);
            checkBookingStatement.setInt(1, bookingIdToCancel);
            checkBookingStatement.setInt(2, loggedInUserId);
            ResultSet bookingResult = checkBookingStatement.executeQuery();

            if (bookingResult.next()) {
                int count = bookingResult.getInt("count");
                if (count > 0) {
                    Date startDate = bookingResult.getDate("start_date");
                    Date endDate = bookingResult.getDate("end_date");

                    // Get the year of the start_date and end_date
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(startDate);
                    int startYear = startCal.get(Calendar.YEAR);

                    Calendar endCal = Calendar.getInstance();
                    endCal.setTime(endDate);
                    int endYear = endCal.get(Calendar.YEAR);

                    // Loop through the years between startYear and endYear (inclusive)
                    for (int year = startYear; year <= endYear; year++) {
                        // Check if a record exists for the user and the current year in the user_cancellations table
                        if (isUserCancellationRecordExists(connection, loggedInUserId, year)) {
                            // If a record exists, update the cancellations_count by incrementing it by 1
                            updateCancellationsCount(connection, loggedInUserId, year);
                        } else {
                            // If no record exists, insert a new row with cancellations_count set to 1
                            insertUserCancellationRecord(connection, loggedInUserId, year);
                        }
                    }

                    // Perform the cancellation operation
                    if (cancelBooking(connection, bookingIdToCancel)) {
                        System.out.println("Booking with ID " + bookingIdToCancel + " has been canceled successfully.");
                    } else {
                        System.out.println("Failed to cancel booking with ID " + bookingIdToCancel + ". Please try again later.");
                    }
                } else {
                    System.out.println("Booking with ID " + bookingIdToCancel + " does not exist for the current user.");
                }
            } else {
                System.out.println("Failed to check booking with ID " + bookingIdToCancel + ". Please try again later.");
            }

            // Close the result set and prepared statement
            resultSet.close();
            preparedStatement.close();
            checkBookingStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static boolean isUserCancellationRecordExists(Connection connection, int userId, int year) throws SQLException {
        String selectQuery = "SELECT COUNT(*) AS count FROM user_cancellations WHERE user_id = ? AND year = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
        preparedStatement.setInt(1, userId);
        preparedStatement.setInt(2, year);
        ResultSet resultSet = preparedStatement.executeQuery();

        boolean recordExists = resultSet.next() && resultSet.getInt("count") > 0;

        resultSet.close();
        preparedStatement.close();

        return recordExists;
    }

    private static void updateCancellationsCount(Connection connection, int userId, int year) throws SQLException {
        String updateQuery = "UPDATE user_cancellations SET cancellations_count = cancellations_count + 1 WHERE user_id = ? AND year = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
        preparedStatement.setInt(1, userId);
        preparedStatement.setInt(2, year);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    private static void insertUserCancellationRecord(Connection connection, int userId, int year) throws SQLException {
        String insertQuery = "INSERT INTO user_cancellations (user_id, year, cancellations_count) VALUES (?, ?, 1)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.setInt(1, userId);
        preparedStatement.setInt(2, year);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    private static boolean cancelBooking(Connection connection, int bookingId) throws SQLException {
        // Perform the cancellation operation
        String deleteBookingQuery = "DELETE FROM bookings WHERE booking_id = ?";
        PreparedStatement deleteBookingStatement = connection.prepareStatement(deleteBookingQuery);
        deleteBookingStatement.setInt(1, bookingId);
        int rowsAffected = deleteBookingStatement.executeUpdate();
        deleteBookingStatement.close();

        return rowsAffected > 0;

    }

    public static void searchCoordinates(Connection connection) throws SQLException {
        // Implement your logic for Option 3 here

//        Scanner scanner2 = new Scanner(System.in);

        System.out.print("Enter the latitude of the search location: ");
        double searchLat = scanner.nextDouble();

        System.out.print("Enter the longitude of the search location: ");
        double searchLong = scanner.nextDouble();

        System.out.print("Enter the maximum distance (in kilometers): ");
        double maxDistance = scanner.nextDouble();



        List<Listing> results = ListingSearch.searchListingsByLocation(connection, searchLat, searchLong, maxDistance);

        if (results.isEmpty()) {
            System.out.println("No listings found within the specified distance.");
        } else {
            ListingSearch.printSearchResults(results);
        }

    }

    public static void promptComment(Connection connection) {

        performOption5(connection);
        System.out.println("\nLeave a comment");
        String bookingID;
        String description;
        int rating = 0;
        int userId = UserContext.getLoggedInUserId();

        do {
            System.out.print("Listing id: ");
            bookingID = scanner.next();

            System.out.print("Enter description: ");
            scanner.nextLine(); // Consume newline
            description = scanner.nextLine();

            System.out.print("Enter rating (1-5): ");
            rating = scanner.nextInt();

        } while (bookingID.isEmpty() || description.isEmpty() || rating < 1 || rating > 5);

        try {
            if (!Main.checkIfTableExists(connection, "Comments")) {
                Comments.createCommentsTable(connection);
            }
            Comments.addComment(connection, userId, bookingID, description, rating);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void advancedSearch(Connection connection) {

        System.out.println("Choose one of the following: ");
        System.out.println("1. Search by coordinates (latitude and longitude)");
        System.out.println("2. Search by zipcode");
        System.out.println("3. Search by prices");

        // Get user's choice
        int choice = scanner.nextInt();

        try {
            switch (choice) {
                case 1:
                    searchCoordinates(connection);
                    break;
                case 2:
                    GeoCode.zipcodePrompt(connection, scanner);
                    break;
                case 3:
                    searchByPrices(connection);
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void searchByPrices(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter 'ASC' to rank by price in ascending order, or 'DESC' to rank in descending order: ");
        String rankOrder = scanner.nextLine().toUpperCase();

        String query = "SELECT * FROM listings ORDER BY price " + rankOrder;

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int listingId = rs.getInt("listing_id");
                String type = rs.getString("type");
                double price = rs.getDouble("price");
                // ... other columns

                // Print or process the listing information
                System.out.println("Listing ID: " + listingId);
                System.out.println("Type: " + type);
                System.out.println("Price: " + price);
                // ... print other columns

                System.out.println("---------------------");
            }

            // Close the ResultSet and PreparedStatement
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
