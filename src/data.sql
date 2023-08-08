-- Drop tables if they exist
DROP TABLE IF EXISTS NounPhrases;
DROP TABLE IF EXISTS renterNounPhrases;
DROP TABLE IF EXISTS user_listings;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS listings_amenities;
DROP TABLE IF EXISTS Comments;
DROP TABLE IF EXISTS hostComments;
DROP TABLE IF EXISTS listings;
DROP TABLE IF EXISTS user_cancellations;
DROP TABLE IF EXISTS user_deletions;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS amenities;



CREATE TABLE users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       real_name VARCHAR(100),
                       address VARCHAR(200),
                       date_of_birth DATE,
                       occupation VARCHAR(100),
                       social_insurance_number VARCHAR(20),
                       credit_card_number VARCHAR(20),
                       password VARCHAR(100) NOT NULL,
                       user_type TINYINT NOT NULL DEFAULT 0,
                       INDEX idx_name (name)
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
                          price DECIMAL(10, 2)
);


CREATE TABLE Comments (
                          comment_id INT AUTO_INCREMENT PRIMARY KEY,
                          listing_id INT NOT NULL,
                          user_id INT NOT NULL,
                          description TEXT NOT NULL,
                          rating INT NOT NULL  CHECK (rating >= 1 AND rating <= 5),
                          timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (listing_id) REFERENCES listings(listing_id),
                          FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE hostComments (
                          comment_id INT AUTO_INCREMENT PRIMARY KEY,
                          listing_id INT NOT NULL,
                          renter_name VARCHAR(100) NOT NULL,
                          description TEXT NOT NULL,
                          rating INT NOT NULL  CHECK (rating >= 1 AND rating <= 5),
                          timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (listing_id) REFERENCES listings(listing_id),
                          FOREIGN KEY (renter_name) REFERENCES users(name)
);

CREATE TABLE NounPhrases (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             listing_id INT NOT NULL,
                             noun_phrase TEXT NOT NULL,
                             count INT NOT NULL,
                             FOREIGN KEY (listing_id) REFERENCES listings(listing_id)
);


CREATE TABLE renterNounPhrases (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             listing_id INT NOT NULL,
                             renter_name VARCHAR(100) NOT NULL,
                             noun_phrase TEXT NOT NULL,
                             count INT NOT NULL,
                             FOREIGN KEY (listing_id) REFERENCES listings(listing_id),
                             FOREIGN KEY (renter_name) REFERENCES users(name)
);

CREATE TABLE user_listings (
                               user_id INT,
                               listing_id INT,
                               PRIMARY KEY (user_id, listing_id),
                               FOREIGN KEY (user_id) REFERENCES users (user_id),
                               FOREIGN KEY (listing_id) REFERENCES listings (listing_id)
);


CREATE TABLE listings_amenities (
        listing_id INT,
        amenity_id INT,
        amenity_name VARCHAR(100) NOT NULL,
        PRIMARY KEY (listing_id, amenity_name),
        FOREIGN KEY (listing_id) REFERENCES listings (listing_id)
);


CREATE TABLE amenities (
                           amenity_id INT PRIMARY KEY AUTO_INCREMENT,
                           amenity_name VARCHAR(100) NOT NULL
);

CREATE TABLE bookings (
      booking_id INT AUTO_INCREMENT PRIMARY KEY,
      listing_id INT,
      user_id INT,
      start_date DATE,
      end_date DATE,
      FOREIGN KEY (listing_id) REFERENCES listings(listing_id),
      FOREIGN KEY (user_id) REFERENCES users(user_id)
);


CREATE TABLE user_cancellations (
        user_id INT,
        year INT,
        cancellations_count INT,
        PRIMARY KEY (user_id, year),
        FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE user_deletions (
        user_id INT,
        deletion_count INT,
        PRIMARY KEY (user_id),
        FOREIGN KEY (user_id) REFERENCES users(user_id)
);


INSERT INTO users (name, real_name, address, date_of_birth, occupation, social_insurance_number, credit_card_number, password, user_type)
VALUES
    ( 'John Doe', 'John Doe', '123 Main St, Toronto', '1990-05-15', 'Software Engineer', '123456789', '1234567890123456','1', 0),
    ('Jane Smith', 'Jane Smith', '456 Oak Ave, Vancouver', '1988-09-22', 'Marketing Manager', '987654321', '9876543210987654','1', 1),
    ('Michael Lee', 'Michael Lee', '789 Elm Rd, Montreal', '1995-02-10', 'Student', '654321987', '6543219876543210','1', 0),
    ('u1', 'u1', 'l', '1995-03-04', 'l', '8839', '9578','1', 0),
    ('a1', 'a1', '7', '1995-02-04', 'l7', '8989', '978','1', 1),
    ('u', 'u', 'f', '1995-02-04', 'l', '89', '98','1', 0),
    ('a', 'a',  ' dfg', '1995-02-07', 'o', '67', '56','1', 1);


INSERT INTO listings (type, latitude, longitude, address, postal_code, city, country, price)
VALUES
    ('apartment', 43.654260, -79.383190, '789 Yonge St, Toronto', 'M4W 1J7', 'Toronto', 'Canada', 150.00),
    ('room', 49.282730, -123.120735, '456 Granville St, Vancouver', 'V6C 1T2', 'Vancouver', 'Canada', 80.00),
    ('house', 45.508888, -73.561668, '123 Maple Ave, Montreal', 'H3A 0A1', 'Montreal', 'Canada', 250.00),
    ('apartment', 43.653225, -79.383186, '789 Bay St, Toronto', 'M5G 2N8', 'Toronto', 'Canada', 180.00),
    ('room', 45.421532, -75.697189, '456 Sparks St, Ottawa', 'K1R 5A7', 'Ottawa', 'Canada', 90.00),
    ('apartment', 43.660315, -79.384975, '123 Queen St W, Toronto', 'M5H 2M9', 'Toronto', 'Canada', 200.00),
    ('house', 43.256236, -79.871101, '456 Oakville Rd, Oakville', 'L6J 2Z4', 'Oakville', 'Canada', 300.00),
    ('room', 43.466232, -80.525829, '789 King St E, Kitchener', 'N2G 2M5', 'Kitchener', 'Canada', 70.00),
    ('house', 43.708394, -79.382882, '123 York Mills Rd, Toronto', 'M2L 1K9', 'Toronto', 'Canada', 350.00),
    ('apartment', 43.450685, -80.482497, '456 Weber St E, Kitchener', 'N2H 1E7', 'Kitchener', 'Canada', 170.00),
    ('house', 43.719817, -79.383209, '789 Lawrence Ave E, Toronto', 'M3C 1P6', 'Toronto', 'Canada', 280.00);

-- Bookings
INSERT INTO bookings (listing_id, user_id, start_date, end_date)
VALUES
    (1, 1, '2023-08-01', '2023-08-05'),
    (2, 2, '2023-08-03', '2023-08-07'),
    (3, 3, '2023-08-05', '2023-08-10'),
    (4, 4, '2023-08-07', '2023-08-12'),
    (5, 5, '2023-08-09', '2023-08-14'),
    (1, 2, '2023-08-02', '2023-08-06'),
    (1, 2, '2023-08-07', '2023-08-09'),
    (2, 3, '2023-08-04', '2023-08-08'),
    (3, 4, '2023-08-06', '2023-08-11'),
    (4, 5, '2023-08-08', '2023-08-13'),
    (5, 1, '2023-08-10', '2023-08-15');


INSERT INTO user_listings (user_id, listing_id)
VALUES
   (1, 1),
    (1, 2),
    (2, 3),
    (2, 4),
    (3, 5),
    (3, 6);
    

INSERT INTO Comments (listing_id, user_id, description, rating)
VALUES
    (1, 2, 'The place was amazing and had a great view.', 5),
    (1, 3, 'Enjoyed my stay here, highly recommended!', 4),
    (2, 4, 'Nice location and spacious rooms.', 4),
    (3, 5, 'Not a pleasant experience, room was dirty.', 2),
    (4, 1, 'Beautiful interior and comfortable beds.', 5),
    (4, 3, 'Beautiful interior and comfortable chairs.', 5);

INSERT INTO hostComments (listing_id, renter_name, description, rating)
VALUES
    (1, 'John Doe', 'Terrible renter', 5),
    (2, 'u1', 'Great renter', 4),
    (3, 'Michael Lee', 'Kind renter', 4),
    (4, 'u', 'Nice person and kind renter', 5);


COMMIT;
