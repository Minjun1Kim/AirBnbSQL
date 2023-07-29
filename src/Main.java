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
                boolean loginSuccessful = LogIn.loginUser(connection, inputUsername, inputPassword, isUserLogin);
                boolean isAdmin = LogIn.isAdminUser(connection, inputUsername);

                LogIn.displayLoginMessage(loginSuccessful, isAdmin);
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
}
