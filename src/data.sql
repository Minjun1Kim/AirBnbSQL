-- Drop tables if they exist
DROP TABLE IF EXISTS NounPhrases;
DROP TABLE IF EXISTS Comments;
DROP TABLE IF EXISTS listings;
DROP TABLE IF EXISTS user_listings;
DROP TABLE IF EXISTS users;


CREATE TABLE users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       address VARCHAR(200),
                       date_of_birth DATE,
                       occupation VARCHAR(100),
                       social_insurance_number VARCHAR(20),
                       credit_card_number VARCHAR(20),
                       password VARCHAR(100) NOT NULL,
                       user_type TINYINT NOT NULL DEFAULT 0
);



CREATE TABLE listings (
                          listing_id INT AUTO_INCREMENT PRIMARY KEY,
                          type ENUM('house', 'apartment', 'room') NOT NULL,
                          latitude DECIMAL(10, 6),
                          longitude DECIMAL(10, 6),
                          address VARCHAR(200),
                          postal_code VARCHAR(10),
                          city VARCHAR(100),
                          country VARCHAR(100),
                          amenities VARCHAR(200),
                          price DECIMAL(10, 2)
);
CREATE TABLE user_listings (
                               user_id INT,
                               listing_id INT,
                               PRIMARY KEY (user_id, listing_id),
                               FOREIGN KEY (user_id) REFERENCES users (user_id),
                               FOREIGN KEY (listing_id) REFERENCES listings (listing_id)
);

INSERT INTO users (name, address, date_of_birth, occupation, social_insurance_number, credit_card_number, password, user_type)
VALUES
    ('John Doe', '123 Main St, Toronto', '1990-05-15', 'Software Engineer', '123456789', '1234567890123456','1', 0),
    ('Jane Smith', '456 Oak Ave, Vancouver', '1988-09-22', 'Marketing Manager', '987654321', '9876543210987654','1', 1),
    ('Michael Lee', '789 Elm Rd, Montreal', '1995-02-10', 'Student', '654321987', '6543219876543210','1', 0),
    ('u1', 'l', '1995-03-04', 'l', '8839', '9578','1', 0),
    ('a1', '7', '1995-02-04', 'l7', '8989', '978','1', 1),
    ('u', 'f', '1995-02-04', 'l', '89', '98','1', 0),
    ('a', ' dfg', '1995-02-07', 'o', '67', '56','1', 1);

INSERT INTO listings (type, latitude, longitude, address, postal_code, city, country, amenities, price)
VALUES
    ('apartment', 43.654260, -79.383190, '789 Yonge St, Toronto', 'M4W 1J7', 'Toronto', 'Canada', 'Wi-Fi, Kitchen, TV', 150.00),
    ('room', 49.282730, -123.120735, '456 Granville St, Vancouver', 'V6C 1T2', 'Vancouver', 'Canada', 'Wi-Fi, Laundry', 80.00),
    ('house', 45.508888, -73.561668, '123 Maple Ave, Montreal', 'H3A 0A1', 'Montreal', 'Canada', 'Pool, Parking, Garden', 250.00),
    ('apartment', 43.653225, -79.383186, '789 Bay St, Toronto', 'M5G 2N8', 'Toronto', 'Canada', 'Wi-Fi, Gym, Pool', 180.00),
    ('room', 45.421532, -75.697189, '456 Sparks St, Ottawa', 'K1R 5A7', 'Ottawa', 'Canada', 'Wi-Fi, Laundry', 90.00),
    ('apartment', 43.660315, -79.384975, '123 Queen St W, Toronto', 'M5H 2M9', 'Toronto', 'Canada', 'Wi-Fi, Gym, Pool', 200.00),
    ('house', 43.256236, -79.871101, '456 Oakville Rd, Oakville', 'L6J 2Z4', 'Oakville', 'Canada', 'Garage, Garden', 300.00),
    ('room', 43.466232, -80.525829, '789 King St E, Kitchener', 'N2G 2M5', 'Kitchener', 'Canada', 'Wi-Fi', 70.00),
    ('house', 43.708394, -79.382882, '123 York Mills Rd, Toronto', 'M2L 1K9', 'Toronto', 'Canada', 'Pool, Garage, Garden', 350.00),
    ('apartment', 43.450685, -80.482497, '456 Weber St E, Kitchener', 'N2H 1E7', 'Kitchener', 'Canada', 'Wi-Fi, Gym', 170.00),
    ('house', 43.719817, -79.383209, '789 Lawrence Ave E, Toronto', 'M3C 1P6', 'Toronto', 'Canada', 'Pool, Garden', 280.00);


COMMIT;
