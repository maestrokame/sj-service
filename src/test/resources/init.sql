CREATE SCHEMA IF NOT EXISTS communications;

CREATE TABLE IF NOT EXISTS communications.message_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

