import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;


public class Report {

    private static String inputTextFilePath = "CSCC43/docs/report.txt";
    private static String outputPdfFilePath = "CSCC43/docs/report.pdf";
    //https://www.apache.org/dyn/closer.lua/pdfbox/2.0.29/pdfbox-app-2.0.29.jar

    public static void writeToTextFile(String content, String filePath, Boolean append) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, append))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> readFileLines(String filePath) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

    public static void processFileAndGeneratePdf() {
        try {
            List<String> lines = readFileLines(inputTextFilePath);

            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            PDType1Font font = PDType1Font.HELVETICA;
            contentStream.setFont(font, 12);

            float yStart = page.getMediaBox().getHeight() - 50;
            float margin = 50;
            float width = page.getMediaBox().getWidth() - 2 * margin;

            for (String line : lines) {
                String[] words = line.split(" ");
                StringBuilder currentLine = new StringBuilder();

                for (String word : words) {
                    String testLine = currentLine + word + " ";
                    float textWidth = font.getStringWidth(testLine) / 1000 * 12;

                    if (textWidth < width) {
                        currentLine = new StringBuilder(testLine);
                    } else {
                        contentStream.beginText();
                        contentStream.setFont(font, 12);
                        contentStream.newLineAtOffset(margin, yStart);
                        contentStream.showText(currentLine.toString());
                        contentStream.endText();
                        yStart -= 15;

                        currentLine = new StringBuilder(word + " ");
                    }
                }

                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(margin, yStart);
                contentStream.showText(currentLine.toString());
                contentStream.endText();
                yStart -= 15;

                if (yStart < 50) {
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(font, 12);
                    yStart = page.getMediaBox().getHeight() - 50;
                }
            }

            contentStream.close();
            document.save(outputPdfFilePath);
            document.close();

            System.out.println("PDF created successfully: " + outputPdfFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public static void generateTotalBookingsByCity(Connection connection, String startDate, String endDate) {
        String query = "SELECT l.city, COUNT(*) AS total_bookings FROM bookings b " +
                "JOIN listings l ON b.listing_id = l.listing_id " +
                "WHERE b.start_date >= ? AND b.end_date <= ? GROUP BY l.city;";
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            ResultSet rs = stmt.executeQuery();

            StringBuilder reportContent = new StringBuilder();
            while (rs.next()) {
                String city = rs.getString("city");
                int totalBookings = rs.getInt("total_bookings");
                String line = "City: " + city + ", Total Bookings: " + totalBookings;
                reportContent.append(line).append("\n");
            }

            reportContent.append("\n");

            // Specify the file path for the report
            String reportFilePath = inputTextFilePath;

            // Write the report content to the file
            writeToTextFile(reportContent.toString(), reportFilePath, false);

            System.out.println("City booking report written to: " + reportFilePath);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void generateTotalBookingsByCityAndZip(Connection connection, String startDate, String endDate) {
        String query = "SELECT l.city, l.postal_code, COUNT(*) AS total_bookings " +
                "FROM bookings b " +
                "JOIN listings l ON b.listing_id = l.listing_id " +
                "WHERE b.start_date >= ? AND b.end_date <= ? " +
                "GROUP BY l.city, l.postal_code;";
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            ResultSet rs = stmt.executeQuery();

            StringBuilder reportContent = new StringBuilder();
            while (rs.next()) {
                String city = rs.getString("city");
                String postalCode = rs.getString("postal_code");
                int totalBookings = rs.getInt("total_bookings");
                String line = "City: " + city + ", Postal Code: " + postalCode + ", Total Bookings: " + totalBookings;
                reportContent.append(line).append("\n");
            }

            reportContent.append("\n");

            // Specify the file path for the report
            String reportFilePath = inputTextFilePath;

            // Write the report content to the file
            writeToTextFile(reportContent.toString(), reportFilePath, true);

            System.out.println("City and Zip Code booking report written to: " + reportFilePath);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void generateTotalListingsReport(Connection connection) {
        String query = "SELECT country, COUNT(*) AS total_listings " +
                "FROM listings " +
                "GROUP BY country;";
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            StringBuilder reportContent = new StringBuilder();
            while (rs.next()) {
                String country = rs.getString("country");
                int totalListings = rs.getInt("total_listings");
                String line = "Country: " + country + ", Total Listings: " + totalListings;
                reportContent.append(line).append("\n");
            }

            // Add a separator between country-level and city-level listings
            reportContent.append("\n");

            // Query for listings per country and city
            query = "SELECT country, city, COUNT(*) AS total_listings " +
                    "FROM listings " +
                    "GROUP BY country, city;";
            stmt = connection.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String country = rs.getString("country");
                String city = rs.getString("city");
                int totalListings = rs.getInt("total_listings");
                String line = "Country: " + country + ", City: " + city + ", Total Listings: " + totalListings;
                reportContent.append(line).append("\n");
            }

            // Add a separator between city-level and postal-code-level listings
            reportContent.append("\n");

            // Query for listings per country, city, and postal code
            query = "SELECT country, city, postal_code, COUNT(*) AS total_listings " +
                    "FROM listings " +
                    "GROUP BY country, city, postal_code;";
            stmt = connection.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String country = rs.getString("country");
                String city = rs.getString("city");
                String postalCode = rs.getString("postal_code");
                int totalListings = rs.getInt("total_listings");
                String line = "Country: " + country + ", City: " + city + ", Postal Code: " + postalCode + ", Total Listings: " + totalListings;
                reportContent.append(line).append("\n");
            }

            reportContent.append("\n");

            // Specify the file path for the report
            String reportFilePath = inputTextFilePath;

            // Write the report content to the file
            writeToTextFile(reportContent.toString(), reportFilePath, true);

            System.out.println("Total listings report written to: " + reportFilePath);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void generateHostRankingReport(Connection connection) {
        try {
            // Query for ranking hosts by total listings per country
            String query = "SELECT u.name, l.country, COUNT(ul.listing_id) AS total_listings " +
                    "FROM users u " +
                    "JOIN user_listings ul ON u.user_id = ul.user_id " +
                    "JOIN listings l ON ul.listing_id = l.listing_id " +
                    "GROUP BY u.user_id, l.country " +
                    "ORDER BY l.country, total_listings DESC;";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            StringBuilder reportContent = new StringBuilder();
            String lastCountry = "";
            reportContent.append("Ranking by Total Listings Per Country:\n");
            while (rs.next()) {
                String country = rs.getString("country");
                String hostName = rs.getString("name");
                int totalListings = rs.getInt("total_listings");

                if (!country.equals(lastCountry)) {
                    reportContent.append("\nCountry: ").append(country).append("\n");
                    lastCountry = country;
                }

                String line = "Host: " + hostName + ", Total Listings: " + totalListings;
                reportContent.append(line).append("\n");
            }

            // Add a separator between country-level and city-level rankings
            reportContent.append("\n");

            // Query for ranking hosts by total listings per city
            query = "SELECT u.name, l.country, l.city, COUNT(ul.listing_id) AS total_listings " +
                    "FROM users u " +
                    "JOIN user_listings ul ON u.user_id = ul.user_id " +
                    "JOIN listings l ON ul.listing_id = l.listing_id " +
                    "GROUP BY u.user_id, l.country, l.city " +
                    "ORDER BY l.country, l.city, total_listings DESC;";
            stmt = connection.prepareStatement(query);
            rs = stmt.executeQuery();

            reportContent.append("Ranking by Total Listings Per City:\n");
            while (rs.next()) {
                String country = rs.getString("country");
                String city = rs.getString("city");
                String hostName = rs.getString("name");
                int totalListings = rs.getInt("total_listings");

                if (!country.equals(lastCountry)) {
                    reportContent.append("\nCountry: ").append(country).append("\n");
                    lastCountry = country;
                }

                String line = "City: " + city + ", Host: " + hostName + ", Total Listings: " + totalListings;

                reportContent.append(line).append("\n");
            }

            reportContent.append("\n");

            // Specify the file path for the report
            String reportFilePath = inputTextFilePath;

            // Write the report content to the file
            writeToTextFile(reportContent.toString(), reportFilePath, true);

            System.out.println("Host ranking report written to: " + reportFilePath);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void generatePossibleCommercialHostsReport(Connection connection) {
        String query = "SELECT u.name, l.city, l.country, COUNT(ul.listing_id) AS host_listings, " +
                "COUNT(*) AS total_listings " +
                "FROM users u " +
                "JOIN user_listings ul ON u.user_id = ul.user_id " +
                "JOIN listings l ON ul.listing_id = l.listing_id " +
                "GROUP BY u.user_id, l.city, l.country " +
                "HAVING host_listings > 0.1 * total_listings;";

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            StringBuilder reportContent = new StringBuilder("Possible Commercial Hosts:\n");

            while (rs.next()) {
                String userName = rs.getString("name");
                String city = rs.getString("city");
                String country = rs.getString("country");
                int hostListings = rs.getInt("host_listings");
                int totalListings = rs.getInt("total_listings");

                String line = "User: " + userName + ", City: " + city + ", Country: " + country +
                        ", Host Listings: " + hostListings + ", Total Listings: " + totalListings;
                reportContent.append(line).append("\n");
            }

            reportContent.append("\n");

            // Specify the file path for the report
            String reportFilePath = inputTextFilePath;

            // Write the report content to the file
            writeToTextFile(reportContent.toString(), reportFilePath, true);

            System.out.println("Possible commercial hosts report written to: " + reportFilePath);

            // Close the ResultSet and PreparedStatement
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void generateRenterBookingRankingReport(Connection connection, String startDate, String endDate) {
        String query = "SELECT u.name, COUNT(b.booking_id) AS total_bookings " +
                "FROM users u " +
                "JOIN bookings b ON u.user_id = b.user_id " +
                "WHERE b.start_date >= ? AND b.end_date <= ? " +
                "GROUP BY u.user_id " +
                "ORDER BY total_bookings DESC;";

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            ResultSet rs = stmt.executeQuery();

            StringBuilder reportContent = new StringBuilder("Renter Booking Ranking Report:\n");

            while (rs.next()) {
                String userName = rs.getString("name");
                int totalBookings = rs.getInt("total_bookings");

                String line = "User: " + userName + ", Total Bookings: " + totalBookings;
                reportContent.append(line).append("\n");
            }

            reportContent.append("\n");

            // Specify the file path for the report
            String reportFilePath = inputTextFilePath;

            // Write the report content to the file
            writeToTextFile(reportContent.toString(), reportFilePath, true);

            System.out.println("Renter booking ranking report written to: " + reportFilePath);

            // Close the ResultSet and PreparedStatement
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void generateRenterBookingRankingReportPerCity(Connection connection, String startDate, String endDate, int minBookings) {
        String query = "SELECT u.name, l.city, COUNT(b.booking_id) AS total_bookings " +
                "FROM users u " +
                "JOIN bookings b ON u.user_id = b.user_id " +
                "JOIN listings l ON b.listing_id = l.listing_id " +
                "WHERE b.start_date >= ? AND b.end_date <= ? " +
                "GROUP BY u.user_id, l.city " +
                "HAVING total_bookings >= ? " +
                "ORDER BY l.city, total_bookings DESC;";

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            stmt.setInt(3, minBookings);
            ResultSet rs = stmt.executeQuery();

            StringBuilder reportContent = new StringBuilder("Renter Booking Ranking Report per City:\n");

            while (rs.next()) {
                String userName = rs.getString("name");
                String city = rs.getString("city");
                int totalBookings = rs.getInt("total_bookings");

                String line = "User: " + userName + ", City: " + city + ", Total Bookings: " + totalBookings;
                reportContent.append(line).append("\n");
            }

            reportContent.append("\n");

            // Specify the file path for the report
            String reportFilePath = inputTextFilePath;

            // Write the report content to the file
            writeToTextFile(reportContent.toString(), reportFilePath, true);

            System.out.println("Renter booking ranking report per city written to: " + reportFilePath);

            // Close the ResultSet and PreparedStatement
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void generateTopCancellationsReport(Connection connection, int year) {
        String query = "SELECT user_id, year, cancellations_count " +
                "FROM user_cancellations " +
                "WHERE year = ? " +
                "ORDER BY cancellations_count DESC " +
                "LIMIT 10;"; // Retrieve top 10 users

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, year);
            ResultSet rs = stmt.executeQuery();

            StringBuilder reportContent = new StringBuilder("Top Cancellations Report for Year " + year + ":\n");

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                int cancellationsCount = rs.getInt("cancellations_count");

                String userType = ""; // Determine if it's a host or renter based on user_id
                // You might need additional queries or logic here to determine user type

                String line = "User ID: " + userId + ", Type: " + userType + ", Cancellations: " + cancellationsCount;
                reportContent.append(line).append("\n");
            }

            reportContent.append("\n");

            // Specify the file path for the report
            String reportFilePath = inputTextFilePath;

            // Write the report content to the file
            writeToTextFile(reportContent.toString(), reportFilePath, true);

            System.out.println("Top cancellations report written to: " + reportFilePath);

            // Close the ResultSet and PreparedStatement
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    public static void generateTopDeletionsReport(Connection connection, int year) {
//        String query = "SELECT u.name, d.deletion_count " +
//                "FROM user_deletions d " +
//                "JOIN users u ON d.user_id = u.user_id " +
//                "WHERE d.year = ? " +
//                "ORDER BY d.deletion_count DESC " +
//                "LIMIT 10;";  // You can adjust the limit as needed
//
//        try {
//            PreparedStatement stmt = connection.prepareStatement(query);
//            stmt.setInt(1, year);
//            ResultSet rs = stmt.executeQuery();
//
//            StringBuilder reportContent = new StringBuilder("Top Deletions Report for Year " + year + ":\n");
//
//            while (rs.next()) {
//                String userName = rs.getString("name");
//                int deletionCount = rs.getInt("deletion_count");
//
//                String line = "User: " + userName + ", Deletion Count: " + deletionCount;
//                reportContent.append(line).append("\n");
//            }
//
//            reportContent.append("\n");
//
//            // Specify the file path for the report
//            String reportFilePath = inputTextFilePath;  // You need to define inputTextFilePath
//
//            // Write the report content to the file
//            writeToTextFile(reportContent.toString(), reportFilePath, true);
//
//            System.out.println("Top deletions report for year " + year + " written to: " + reportFilePath);
//
//            // Close the ResultSet and PreparedStatement
//            rs.close();
//            stmt.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    public static void generateNounPhraseReport(Connection connection) throws SQLException {
        boolean isNounPhrasesTableEmpty = false;
        ResultSet checkRs = null;
        PreparedStatement checkStmt = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String checkQuery = "SELECT 1 FROM NounPhrases LIMIT 1;";
            checkStmt = connection.prepareStatement(checkQuery);
            checkRs = checkStmt.executeQuery();
            isNounPhrasesTableEmpty = !checkRs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (checkRs != null) {
                checkRs.close();
            }
            if (checkStmt != null) {
                checkStmt.close();
            }
        }

        if (isNounPhrasesTableEmpty) {
            NounPhraseCount.createNounPhrasesTable(connection);
            NounPhraseCount.runNounPhrasesQuery(connection);
        }

        try {
            String query = "SELECT listing_id, noun_phrase, count FROM NounPhrases;";
            stmt = connection.prepareStatement(query);
            rs = stmt.executeQuery();

            Map<Integer, List<String>> listingNounPhrasesMap = new HashMap<>();

            while (rs.next()) {
                int listingId = rs.getInt("listing_id");
                String nounPhrase = rs.getString("noun_phrase");
                int count = rs.getInt("count");

                listingNounPhrasesMap.putIfAbsent(listingId, new ArrayList<>());
                List<String> nounPhrases = listingNounPhrasesMap.get(listingId);
                nounPhrases.add(nounPhrase + " (Count: " + count + ")");
            }

            StringBuilder reportContent = new StringBuilder("Noun Phrase Report:\n");

            for (Map.Entry<Integer, List<String>> entry : listingNounPhrasesMap.entrySet()) {
                int listingId = entry.getKey();
                List<String> nounPhrases = entry.getValue();

                String phrases = String.join(", ", nounPhrases);
                String line = "Listing ID: " + listingId + ", Noun Phrases: " + phrases;
                reportContent.append(line).append("\n");
            }

            // Specify the file path for the report
            String reportFilePath = inputTextFilePath;

            // Write the report content to the file
            writeToTextFile(reportContent.toString(), reportFilePath, true);

            System.out.println("Noun phrase report written to: " + reportFilePath);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close the ResultSet and PreparedStatement
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
    }



    public static void generateDoc(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        writeToTextFile("", inputTextFilePath, false);

        //String[] dates = selectDate(scanner);
        //generateTotalBookingsByCity(connection, dates[0], dates[1]);

//        String[] dates2 = selectDate(scanner);
//        generateTotalBookingsByCityAndZip(connection, dates2[0], dates2[1]);

        generateTotalListingsReport(connection);

        generateHostRankingReport(connection);

        generatePossibleCommercialHostsReport(connection);

        generateRenterBookingRankingReport(connection, "2023-08-01", "2023-09-01");

        generateRenterBookingRankingReportPerCity(connection, "2023-08-01", "2023-09-01", 2);

        generateNounPhraseReport(connection);

        System.out.print("Enter the year for the top cancellations report: ");
        int year = scanner.nextInt();

        generateTopCancellationsReport(connection, year);

        processFileAndGeneratePdf();

        scanner.close();

        return;
    }


    private static String[] selectDate(Scanner scanner) {
        System.out.print("Enter start date (YYYY-MM-DD): ");
        String startDate = scanner.nextLine();

        System.out.print("Enter end date (YYYY-MM-DD): ");
        String endDate = scanner.nextLine();

        return new String[]{startDate, endDate};
    }


    public static void promptReport(Connection connection) throws SQLException {
//        convertTextToPdf();

        Scanner scanner = new Scanner(System.in);
        char res = 'n';

        System.out.println("Generate a report - Default Settings (1) or Customise (2): ");
        byte report = scanner.nextByte();

        if (report == 1) {
            generateDoc(connection);
            scanner.close();
            return;
        }

        System.out.print("Total number of bookings in a specific date range by city? (y/n): ");
        res = scanner.nextLine().charAt(0);

        if (res == 'y' || res == 'Y') {
            System.out.println("You chose to generate the report.");

            String startDate = "2023-08-01";
            String endDate = "2023-08-14";

            generateTotalBookingsByCity(connection, startDate, endDate);

        } else if (res == 'n' || res == 'N') {
            System.out.println("You chose not to generate the report.");
            // Perform other actions if needed
        } else {
            System.out.println("Invalid input. Please enter 'y' or 'n'.");
        }

        System.out.print("Total number of bookings in a specific date range by zipcode and city? (y/n): ");
        res = scanner.nextLine().charAt(0);

        if (res == 'y' || res == 'Y') {
            System.out.println("You chose to generate the report.");
            System.out.print("Enter start date (YYYY-MM-DD): ");
//            String startDate = scanner.nextLine();
            String startDate = "2023-08-01";
            System.out.print("Enter end date (YYYY-MM-DD): ");
//            String endDate = scanner.nextLine();
            String endDate = "2023-08-14";

            generateTotalBookingsByCityAndZip(connection, startDate, endDate);

        } else if (res == 'n' || res == 'N') {
            System.out.println("You chose not to generate the report.");
            // Perform other actions if needed
        } else {
            System.out.println("Invalid input. Please enter 'y' or 'n'.");
        }

        System.out.print("Total number of listings per {country}, {per country and city} as well as {per country, city and postal code}. (y/n): ");
        res = scanner.nextLine().charAt(0);

        if (res == 'y' || res == 'Y') {
            generateTotalListingsReport(connection);
        } else if (res == 'n' || res == 'N') {
            System.out.println("You chose not to generate the report.");
            // Perform other actions if needed
        } else {
            System.out.println("Invalid input. Please enter 'y' or 'n'.");
        }

        generateHostRankingReport(connection);

        scanner.close();


    }
}
