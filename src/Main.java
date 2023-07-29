import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://127.0.0.1/C43";
        String username = "root";
        String password = "Mustafa0503";

        try {
            //make connection
            Connection connection = DriverManager.getConnection(url, username, password);
            Scanner scanner = new Scanner(System.in);

            System.out.println("Select an option:");
            System.out.println("1. Log in as user");
            System.out.println("2. Log in as admin");
            System.out.println("3. Sign up");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            if (choice == 1 || choice == 2) {
                // Log in
                System.out.print("Enter your username: ");
                String inputUsername = scanner.next();

                System.out.print("Enter your password: ");
                String inputPassword = scanner.next();

                boolean isUserLogin = choice == 1;
                boolean loginSuccessful = loginUser(connection, inputUsername, inputPassword, isUserLogin);

                if (loginSuccessful) {
                    System.out.println("Login successful!");

                    if (isUserLogin) {
                        System.out.println("hello, user!");
                    } else {
                        // Check if the user is an admin in the database
                        if (isAdminUser(connection, inputUsername)) {
                            System.out.println("Welcome, admin!");
                        } else {
                            System.out.println("You do not have authentication as admin.");
                        }
                    }
                } else {
                    System.out.println("Login failed. Invalid username or password.");
                }

            } else if (choice == 3) {
                // Sign up
                SignUp.performSignUp(connection, scanner);
            } else {
                System.out.println("Invalid choice. Please select 1, 2, or 3.");
            }

            // Close resources
            scanner.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean loginUser(Connection connection, String username, String password, boolean isUserLogin) throws SQLException {
        String selectQuery = "SELECT * FROM users WHERE username = ? AND password = ? AND admin = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        preparedStatement.setBoolean(3, !isUserLogin);

        ResultSet resultSet = preparedStatement.executeQuery();

        boolean loginSuccessful = resultSet.next();

        resultSet.close();
        preparedStatement.close();

        return loginSuccessful;
    }

    private static boolean isAdminUser(Connection connection, String username) throws SQLException {
        String selectQuery = "SELECT admin FROM users WHERE username = ? AND admin = true";
        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
        preparedStatement.setString(1, username);

        ResultSet resultSet = preparedStatement.executeQuery();

        boolean isAdmin = resultSet.next();

        resultSet.close();
        preparedStatement.close();

        return isAdmin;
    }
}
