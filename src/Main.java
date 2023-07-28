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

            System.out.print("Enter your username: ");
            String inputUsername = scanner.nextLine();

            System.out.print("Enter your password: ");
            String inputPassword = scanner.nextLine();

            String selectQuery = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setString(1, inputUsername);
            preparedStatement.setString(2, inputPassword);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("Login successful!");
            } else {
                System.out.println("Login failed. Invalid username or password.");
            }

            // Close resources
            resultSet.close();
            preparedStatement.close();
            scanner.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
