-- Create the users table
CREATE TABLE IF NOT EXISTS users (
                                     uid BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     firstname VARCHAR(50) NOT NULL,
                                     lastname VARCHAR(50) NOT NULL,
                                     email VARCHAR(255) UNIQUE NOT NULL,
                                     password VARCHAR(255) NOT NULL
);

-- Create the location table
CREATE TABLE IF NOT EXISTS location (
                                        lid BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        uid BIGINT NOT NULL,
                                        name VARCHAR(255) NOT NULL,
                                        address VARCHAR(255) NOT NULL,
                                        CONSTRAINT fk_uid FOREIGN KEY (uid) REFERENCES users(uid)
                                            ON DELETE CASCADE ON UPDATE CASCADE
);

-- Create the access table
CREATE TABLE IF NOT EXISTS access (
                                      aid BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      uid BIGINT NOT NULL,
                                      lid BIGINT NOT NULL,
                                      type VARCHAR(50) NOT NULL,
                                      CONSTRAINT fk_uid_3 FOREIGN KEY (uid) REFERENCES users(uid)
                                          ON DELETE CASCADE ON UPDATE CASCADE,
                                      CONSTRAINT fk_lid FOREIGN KEY (lid) REFERENCES location(lid)
                                          ON DELETE CASCADE ON UPDATE CASCADE
);
-- Create the history table
CREATE TABLE IF NOT EXISTS history (
                         hid BIGINT AUTO_INCREMENT PRIMARY KEY,
                         action_by BIGINT,
                         object_type VARCHAR(100),
                         action VARCHAR(100),
                         action_details VARCHAR(255),
                         date TIMESTAMP
);

