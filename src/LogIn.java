import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LogIn {
    public static boolean loginUser(Connection connection, String username, String password, boolean isUserLogin) throws SQLException {
        String selectQuery = "SELECT * FROM users WHERE username = ? AND password = ? AND admin = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        preparedStatement.setBoolean(3, !isUserLogin); // If isUserLogin is true, set admin = false, else set admin = true

        ResultSet resultSet = preparedStatement.executeQuery();

        boolean loginSuccessful = resultSet.next();

        resultSet.close();
        preparedStatement.close();

        return loginSuccessful;
    }

    public static boolean isAdminUser(Connection connection, String username) throws SQLException {
        String selectQuery = "SELECT admin FROM users WHERE username = ? AND admin = true";
        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
        preparedStatement.setString(1, username);

        ResultSet resultSet = preparedStatement.executeQuery();

        boolean isAdmin = resultSet.next();

        resultSet.close();
        preparedStatement.close();

        return isAdmin;
    }

    public static void displayLoginMessage(boolean loginSuccessful, boolean isAdmin) {
        if (loginSuccessful) {
            System.out.println("Login successful!");

            if (isAdmin) {
                System.out.println("Welcome, admin!");
            } else {
                System.out.println("Welcome, regular user!");
            }
        } else {
            System.out.println("Login failed. Invalid username or password.");
        }
    }
}
