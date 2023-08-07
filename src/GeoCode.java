import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class GeoCode {

    private static String apiKey = "AIzaSyDYZ_TJyePkWdzUGE_JzCLm1J-GqNRZKJw";
    private static Scanner scanner = new Scanner(System.in);
    private static double[] getLatLngForPostalCode(String apiKey, String postalCode) {
        try {
            URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json" +
                    "?address=" + postalCode +
                    "&key=" + apiKey);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                // Parse JSON response and extract latitude and longitude
                double latitude = 0.0;
                double longitude = 0.0;

                try {
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray results = jsonResponse.getJSONArray("results");

                    if (!results.isEmpty()) {
                        JSONObject location = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                        latitude = location.getDouble("lat");
                        longitude = location.getDouble("lng");
                    } else {
                        System.out.println("No results found for geocoding.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return new double[]{latitude, longitude};

            } else {
                System.out.println("Geocoding API request failed with response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void zipcodePrompt(Connection connection) throws SQLException {

        System.out.print("Enter a postal code: ");
        String postalCode = scanner.nextLine().trim().replace(" ", "");

        double[] latLng = getLatLngForPostalCode(apiKey, postalCode);

        if (latLng != null) {
            double latitude = latLng[0];
            double longitude = latLng[1];

            List<Listing> results = ListingSearch.searchListingsByLocation(connection, latitude, longitude, 20);
            ListingSearch.printSearchResults(results);

        } else {
            System.out.println("Failed to retrieve latitude and longitude.");
        }

        // Close the scanner when you're done using it
        scanner.close();


    }
}
