-- Create the database if it doesn't exist
SELECT 'CREATE DATABASE hotel_management'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hotel_management');

-- Connect to the new database
\c hotel_management; 