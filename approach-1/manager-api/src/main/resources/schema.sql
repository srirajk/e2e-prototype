-- CREATE DATABASE IF NOT EXISTS testdb;

-- USE testdb;

CREATE TABLE IF NOT EXISTS business_product (
                                  id INT AUTO_INCREMENT,
                                  business_id VARCHAR(255),
                                  product_id VARCHAR(255),
                                  split_size INT,
                                  field_length INT,
                                  configuration_split_size INT,
                                  PRIMARY KEY (id),
                                  UNIQUE (business_id, product_id)
);

CREATE TABLE IF NOT EXISTS file_event_status (
                            id INT AUTO_INCREMENT,
                            business_id VARCHAR(255),
                            product_id VARCHAR(255),
                            file_path VARCHAR(255) UNIQUE,
                            request_id VARCHAR(255) UNIQUE,
                            status VARCHAR(255),
                            current_step VARCHAR(255),
                            PRIMARY KEY (id),
                            UNIQUE (business_id, product_id, file_path, request_id)
);

/*
CREATE SEQUENCE IF NOT EXISTS business_product_sequence START WITH 100 INCREMENT BY 1;

CREATE SEQUENCE IF NOT EXISTS file_event_status_sequence START WITH 100 INCREMENT BY 1;*/
