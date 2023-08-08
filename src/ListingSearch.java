import java.sql.SQLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ListingSearch {
    private static final double EARTH_RADIUS_KM = 6371.0; // Earth's radius in kilometers

    public static List<Listing> searchListingsByLocation(Connection connection, double searchLat, double searchLon, double maxDistance) throws SQLException {
        List<Listing> results = new ArrayList<>();

        String searchQuery = "SELECT * FROM listings WHERE " +
                "latitude IS NOT NULL AND longitude IS NOT NULL " +
                "AND ? * " + // Distance comparison value
                "ACOS(SIN(RADIANS(?)) * SIN(RADIANS(latitude)) + COS(RADIANS(?)) * COS(RADIANS(latitude)) * " +
                "COS(RADIANS(? - longitude))) <= ?";



        try (PreparedStatement statement = connection.prepareStatement(searchQuery)) {
            statement.setDouble(1, EARTH_RADIUS_KM);
            statement.setDouble(2, searchLat);
            statement.setDouble(3, searchLat);
            statement.setDouble(4, searchLon);
            statement.setDouble(5, maxDistance);

            ResultSet resultSet = statement.executeQuery();

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

                Listing listing = new Listing(listingId, type, latitude, longitude, address, postalCode, city, country, price);
                results.add(listing);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    public static void printSearchResults(List<Listing> listings) {
        System.out.println("Listings found within the specified distance:");
        for (Listing listing : listings) {
            System.out.println(listing);
        }
    }
}