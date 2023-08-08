
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
                          price DECIMAL(10, 2)
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

INSERT INTO users (name, address, date_of_birth, occupation, social_insurance_number, credit_card_number, password, user_type)
VALUES
    ('John Doe', '123 Main St, Toronto', '1990-05-15', 'Software Engineer', '123456789', '1234567890123456','1', 0),
    ('Jane Smith', '456 Oak Ave, Vancouver', '1988-09-22', 'Marketing Manager', '987654321', '9876543210987654','1', 1),
    ('Michael Lee', '789 Elm Rd, Montreal', '1995-02-10', 'Student', '654321987', '6543219876543210','1', 0),
    ('u1', 'l', '1995-03-04', 'l', '8839', '9578','1', 0),
    ('a1', '7', '1995-02-04', 'l7', '8989', '978','1', 1),
    ('u', 'f', '1995-02-04', 'l', '89', '98','1', 0),
    ('a', ' dfg', '1995-02-07', 'o', '67', '56','1', 1);

INSERT INTO listings (type, latitude, longitude, address, postal_code, city, country, price)
VALUES
    ('apartment', 43.654260, -79.383190, '789 Yonge St, Toronto', 'M4W 1J7', 'Toronto', 'Canada', 150.00),
    ('room', 49.282730, -123.120735, '456 Granville St, Vancouver', 'V6C 1T2', 'Vancouver', 'Canada', 80.00),
    ('house', 45.508888, -73.561668, '123 Maple Ave, Montreal', 'H3A 0A1', 'Montreal', 'Canada', 250.00);

COMMIT;
