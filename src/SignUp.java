import java.sql.*;
import java.util.Scanner;
public class SignUp {
    public static void performSignUp(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter a new username: ");
        String newUsername = scanner.next();

        System.out.print("Enter a new password: ");
        String newPassword = scanner.next();

        String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.setString(1, newUsername);
        preparedStatement.setString(2, newPassword);

        int rowsAffected = preparedStatement.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("Sign up successful! You can now log in.");
        } else {
            System.out.println("Sign up failed. Please try again.");
        }

        preparedStatement.close();
    }
}
