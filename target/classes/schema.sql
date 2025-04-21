-- Check if hotel_id column exists in room table
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'room' AND column_name = 'hotel_id'
    ) THEN
        -- Add hotel_id column to room table
        ALTER TABLE room ADD COLUMN hotel_id INT;
        
        -- Add foreign key constraint
        ALTER TABLE room 
        ADD CONSTRAINT fk_room_hotel 
        FOREIGN KEY (hotel_id) 
        REFERENCES hotels(id);
    END IF;
END $$;

-- Make hotel_id not nullable (only if it exists and is nullable)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'room' AND column_name = 'hotel_id' AND is_nullable = 'YES'
    ) THEN
        ALTER TABLE room ALTER COLUMN hotel_id SET NOT NULL;
    END IF;
END $$;

-- Create hotels table if it doesn't exist
CREATE TABLE IF NOT EXISTS hotels (
    id SERIAL PRIMARY KEY,
    hotelname VARCHAR(255),
    owner_name VARCHAR(255),
    data BYTEA,
    gstno VARCHAR(255),
    mobile VARCHAR(255),
    totalroom VARCHAR(255),
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    hotel_description TEXT
);

CREATE TABLE IF NOT EXISTS hotel_images (
    id SERIAL PRIMARY KEY,
    hotel_id INTEGER REFERENCES hotels(id),
    image_data BYTEA,
    image_title VARCHAR(255),
    image_description TEXT
);

-- Drop existing room table if it exists
DROP TABLE IF EXISTS room;

-- Create room table with all required columns
CREATE TABLE room (
    id SERIAL PRIMARY KEY,
    roomno INTEGER,
    status VARCHAR(50),
    type VARCHAR(50),
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    hotel_id INTEGER REFERENCES hotels(id)
);

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_room_hotel'
    ) THEN
        ALTER TABLE room 
        ADD CONSTRAINT fk_room_hotel 
        FOREIGN KEY (hotel_id) 
        REFERENCES hotels(id);
    END IF;
END $$;

-- Make hotel_id not nullable
ALTER TABLE room ALTER COLUMN hotel_id SET NOT NULL; 