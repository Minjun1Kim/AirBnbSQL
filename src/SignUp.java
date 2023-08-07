import java.sql.*;
import java.util.Scanner;

public class SignUp {
    public static void performSignUp(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter a new username: ");
        String newUsername = scanner.next();

        // Check if the username already exists in the database
        if (isUsernameExists(connection, newUsername)) {
            System.out.println("Username already exists. Please choose a different username.");
            return;
        }

        System.out.print("Enter your real name: ");
        String realName = scanner.next();

        System.out.print("Enter a new password: ");
        String newPassword = scanner.next();

        System.out.print("Enter your address: ");
        String address = scanner.next();

        System.out.print("Enter your date of birth (YYYY-MM-DD): ");
        String dateOfBirth = scanner.next();

        System.out.print("Enter your occupation: ");
        String occupation = scanner.next();

        System.out.print("Enter your social insurance number: ");
        String socialInsuranceNumber = scanner.next();

        System.out.print("Enter your credit card number: ");
        String creditCardNumber = scanner.next();

        System.out.print("Are you registering as an admin? (y/n): ");
        String isAdminInput = scanner.next();
        int userType = isAdminInput.equalsIgnoreCase("y") ? 1 : 0;

        String insertQuery = "INSERT INTO users (name, real_name, address, date_of_birth, occupation, social_insurance_number, credit_card_number, password, user_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.setString(1, newUsername);
        preparedStatement.setString(2, realName);
        preparedStatement.setString(3, address);
        preparedStatement.setString(4, dateOfBirth);
        preparedStatement.setString(5, occupation);
        preparedStatement.setString(6, socialInsuranceNumber);
        preparedStatement.setString(7, creditCardNumber);
        preparedStatement.setString(8, newPassword);
        preparedStatement.setInt(9, userType);

        int rowsAffected = preparedStatement.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("Sign up successful! You can now log in.");
        } else {
            System.out.println("Sign up failed. Please try again.");
        }

        preparedStatement.close();
    }


    private static boolean isUsernameExists(Connection connection, String username) throws SQLException {
        String selectQuery = "SELECT user_id FROM users WHERE name = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
        preparedStatement.setString(1, username);

        ResultSet resultSet = preparedStatement.executeQuery();
        boolean usernameExists = resultSet.next();

        resultSet.close();
        preparedStatement.close();

        return usernameExists;
    }
}
