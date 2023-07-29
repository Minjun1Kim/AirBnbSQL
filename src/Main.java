import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://127.0.0.1/C43";
        String username = "root";
        String password = "Mustafa0503";

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            Scanner scanner = new Scanner(System.in);

            System.out.println("Select an option:");
            System.out.println("1. Log in");
            System.out.println("2. Sign up");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            if (choice == 1) {
                // Log in
                System.out.print("Enter your username: ");
                String inputUsername = scanner.next();

                System.out.print("Enter your password: ");
                String inputPassword = scanner.next();

                if (loginUser(connection, inputUsername, inputPassword)) {
                    System.out.println("Login successful!");
                } else {
                    System.out.println("Login failed. Invalid username or password.");
                }

            } else if (choice == 2) {
                // Sign upgit
                SignUp.performSignUp(connection, scanner);
            } else {
                System.out.println("Invalid choice. Please select 1 or 2.");
            }

            // Close resources
            scanner.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean loginUser(Connection connection, String username, String password) throws SQLException {
        String selectQuery = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);

        ResultSet resultSet = preparedStatement.executeQuery();

        boolean loginSuccessful = resultSet.next();

        resultSet.close();
        preparedStatement.close();

        return loginSuccessful;
    }
}
