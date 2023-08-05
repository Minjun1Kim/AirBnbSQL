import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class AdminOptionPage {
    private static Scanner scanner = new Scanner(System.in);

    public static void displayOptions(Connection connection) {
        System.out.println("Welcome, admin! You have additional options here.");
        boolean running = true;

        while (running) {
            System.out.println("Select an admin option:");
            System.out.println("1. Create List");
            System.out.println("2. Admin Option 2");
            System.out.println("3. Admin Option 3");
            System.out.println("4. Exit");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            if (choice == 1) {
                performAdminOption1(connection);
            } else if (choice == 2) {
                performAdminOption2();
            } else if (choice == 3) {
                performAdminOption3();
            } else if (choice == 4) {
                running = false;
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
            System.out.print("Enter listing type (full house, apartment, room): ");
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
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
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

    private static void performAdminOption2() {
        // Implement your logic for Admin Option 2 here
        System.out.println("You selected Admin Option 2.");
    }

    private static void performAdminOption3() {
        // Implement your logic for Admin Option 3 here
        System.out.println("You selected Admin Option 3.");
    }
}
