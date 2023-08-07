import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.sql.*;

public class OptionPage {
    private static Scanner scanner = new Scanner(System.in);

    public static void displayOptions(Connection connection) throws SQLException {
        System.out.println("Welcome to the Options Page!");
        boolean running = true;

        while (running) {
            System.out.println("Select an option:");
            System.out.println("1. View list");
            System.out.println("2. search");
            System.out.println("3. Option 3");
            System.out.println("4. Exit");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            if (choice == 1) {
                performOption1(connection);
            } else if (choice == 2) {
                performOption2(connection);
            } else if (choice == 3) {
                performOption3(connection);
            } else if (choice == 4) {
                running = false;
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }

        System.out.println("Exiting the Options Page. Goodbye!");
    }

    private static void performOption1(Connection connection) {
        try {
            System.out.println("Listing Information:");

            // Execute the SQL query to fetch all listings
            String selectQuery = "SELECT * FROM listings";
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

            // Retrieve the result set
            ResultSet resultSet = preparedStatement.executeQuery();

            // Display the listing information in a grid-like format using printf
            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+");
            System.out.println("| Listing ID | Type           | Latitude      | Longitude      | Address                      | Postal   | City          | Country                 | Price     |");
            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+");

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

                System.out.printf("| %-10s | %-14s | %-13.6f | %-14.6f | %-28s | %-8s | %-13s | %-24s | $%7.2f |\n",
                        listingId, type, latitude, longitude, address, postalCode, city, country, price);
            }

            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+");

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
            System.out.print("Enter the listing type (full house, apartment, room) [Leave empty for any type]: ");
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

            // Prepare the SQL query dynamically based on user input
            StringBuilder queryBuilder = new StringBuilder("SELECT * FROM listings WHERE 1=1");
            if (!listingType.isEmpty()) {
                queryBuilder.append(" AND type = ?");
            }
            if (!city.isEmpty()) {
                queryBuilder.append(" AND city = ?");
            }
            if (!country.isEmpty()) {
                queryBuilder.append(" AND country = ?");
            }
            if (minPrice != null) {
                queryBuilder.append(" AND price >= ?");
            }
            if (maxPrice != null) {
                queryBuilder.append(" AND price <= ?");
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
                preparedStatement.setDouble(parameterIndex, maxPrice);
            }

            // Retrieve the result set
            ResultSet resultSet = preparedStatement.executeQuery();

            // Display the search results in a grid-like format
            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+");
            System.out.println("| Listing ID | Type           | Latitude      | Longitude      | Address                      | Postal   | City          | Country                 | Price     |");
            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+");

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

                System.out.printf("| %-10s | %-14s | %-13.6f | %-14.6f | %-28s | %-8s | %-13s | %-24s | $%9.2f |\n",
                        listingId, type, latitude, longitude, address, postalCode, cityName, countryName, price);
            }

            System.out.println("+------------+----------------+---------------+----------------+------------------------------+----------+---------------+-------------------------+-----------+");

            // Close the result set, prepared statement, and scanner
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void performOption3(Connection connection) throws SQLException {
        // Implement your logic for Option 3 here

        Scanner scanner2 = new Scanner(System.in);

        System.out.print("Enter the latitude of the search location: ");
        double searchLat = scanner2.nextDouble();

        System.out.print("Enter the longitude of the search location: ");
        double searchLong = scanner2.nextDouble();

        System.out.print("Enter the maximum distance (in kilometers): ");
        double maxDistance = scanner2.nextDouble();

        scanner2.close();

        List<Listing> results = ListingSearch.searchListingsByLocation(connection, searchLat, searchLong, maxDistance);

        if (results.isEmpty()) {
            System.out.println("No listings found within the specified distance.");
        } else {
            ListingSearch.printSearchResults(results);
        }
    }
}
