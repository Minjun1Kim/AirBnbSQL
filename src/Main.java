import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String correctUsername = "mustafa";
        String correctPassword = "123";
        System.out.print("Enter your username: ");
        String inputUsername = scanner.nextLine();
        System.out.print("Enter your password: ");
        String inputPassword = scanner.nextLine();
        if (correctUsername.equals(inputUsername) && correctPassword.equals(inputPassword)) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Login failed. Invalid username or password.");
        }

        scanner.close();
    }
}
