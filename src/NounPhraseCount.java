import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.util.Properties;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;
import java.sql.*;

public class NounPhraseCount implements Comparable<NounPhraseCount> {
    private String nounPhrase;
    private int count;

    public static void createNounPhrasesTable(Connection connection) throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS NounPhrases (" +
                "phrase_id INT AUTO_INCREMENT PRIMARY KEY," +
                "listing_id INT NOT NULL," +
                "noun_phrase VARCHAR(255) NOT NULL," +
                "count INT NOT NULL," +
                "FOREIGN KEY (listing_id) REFERENCES listings(listing_id)" +
                ")";

        try (PreparedStatement statement = connection.prepareStatement(createTableSQL)) {
            statement.executeUpdate();
            System.out.println("NounPhrases table created successfully.");
        } catch (SQLException e) {
            System.out.println("An error occurred while creating the NounPhrases table: " + e.getMessage());
            throw e;
        }
    }

    public static void runNounPhrasesQuery(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String selectCommentsQuery = "SELECT listing_id, description FROM Comments";

            try (ResultSet commentsResultSet = statement.executeQuery(selectCommentsQuery)) {
                while (commentsResultSet.next()) {
                    int listingId = commentsResultSet.getInt("listing_id");
                    String description = commentsResultSet.getString("description");

                    // Extract and store noun phrases from the description
                    extractAndStoreNounPhrases(connection, listingId, description);
                }
            } catch (SQLException e) {
                System.out.println("An error occurred while executing the query: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while creating a statement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void extractAndStoreNounPhrases(Connection connection, int listingId, String text) {
        RedwoodConfiguration.current().clear().apply();

        // Set up Stanford CoreNLP pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Process the text
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        // Extract and store noun phrases
        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            String currentNounPhrase = "";

            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.originalText();
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                if (pos.startsWith("NN") || pos.startsWith("JJ")) { // Nouns or adjectives
                    currentNounPhrase += word + " ";
                } else if (!currentNounPhrase.isEmpty()) {
                    storeNounPhrase(connection, listingId, currentNounPhrase.trim());
                    currentNounPhrase = "";
                }
            }

            if (!currentNounPhrase.isEmpty()) {
                storeNounPhrase(connection, listingId, currentNounPhrase.trim());
            }
        }
    }

    private static void storeNounPhrase(Connection connection, int listingId, String nounPhrase) {
        String selectQuery = "SELECT id, count FROM NounPhrases WHERE listing_id = ? AND noun_phrase = ?";

        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
            selectStatement.setInt(1, listingId);
            selectStatement.setString(2, nounPhrase);

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    // Noun phrase exists, update the count
                    int id = resultSet.getInt("id");
                    int count = resultSet.getInt("count") + 1;
                    updateNounPhraseCount(connection, id, count);
                } else {
                    // Noun phrase doesn't exist, insert a new row
                    insertOrUpdateNounPhrase(connection, listingId, nounPhrase);
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while checking/storing noun phrase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void insertOrUpdateNounPhrase(Connection connection, int listingId, String nounPhrase) {
        String insertOrUpdateQuery = "INSERT INTO NounPhrases (listing_id, noun_phrase, count) " +
                "VALUES (?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE count = count + 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertOrUpdateQuery)) {
            preparedStatement.setInt(1, listingId);
            preparedStatement.setString(2, nounPhrase);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("An error occurred while inserting/updating noun phrase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void updateNounPhraseCount(Connection connection, int id, int count) {
        String updateQuery = "UPDATE NounPhrases SET count = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setInt(1, count);
            preparedStatement.setInt(2, id);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("An error occurred while updating noun phrase count: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public NounPhraseCount(String nounPhrase, int count) {
        this.nounPhrase = nounPhrase;
        this.count = count;
    }

    @Override
    public int compareTo(NounPhraseCount other) {
        return Integer.compare(this.count, other.count);
    }

    public String getNounPhrase() {
        return nounPhrase;
    }

    public int getCount() {
        return count;
    }
}
