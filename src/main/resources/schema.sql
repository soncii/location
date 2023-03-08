-- Create the users table
CREATE TABLE IF NOT EXISTS users  (
                       uid identity PRIMARY KEY,
                       firstname VARCHAR(50) NOT NULL,
                       lastname VARCHAR(50) NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL
);

-- Create the friendship table
CREATE TABLE IF NOT EXISTS friendship (
                            fid identity PRIMARY KEY,
                            uid_1 INT NOT NULL,
                            uid_2 INT NOT NULL,
                            CONSTRAINT fk_uid_1 FOREIGN KEY (uid_1) REFERENCES users(uid)
                                ON DELETE CASCADE ON UPDATE CASCADE,
                            CONSTRAINT fk_uid_2 FOREIGN KEY (uid_2) REFERENCES users(uid)
                                ON DELETE CASCADE ON UPDATE CASCADE,
                            CONSTRAINT unique_friendship UNIQUE (uid_1, uid_2)
);

-- Create the location table
CREATE TABLE IF NOT EXISTS location (
                          lid identity PRIMARY KEY,
                          uid INT NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          address VARCHAR(255) NOT NULL,
                          CONSTRAINT fk_uid FOREIGN KEY (uid) REFERENCES users(uid)
                              ON DELETE CASCADE ON UPDATE CASCADE
);

-- Create the access table
CREATE TABLE IF NOT EXISTS access (
                        aid identity  PRIMARY KEY,
                        uid INT NOT NULL,
                        lid INT NOT NULL,
                        type VARCHAR(50) NOT NULL,
                        CONSTRAINT fk_uid_3 FOREIGN KEY (uid) REFERENCES users(uid)
                            ON DELETE CASCADE ON UPDATE CASCADE,
                        CONSTRAINT fk_lid FOREIGN KEY (lid) REFERENCES location(lid)
                            ON DELETE CASCADE ON UPDATE CASCADE
);