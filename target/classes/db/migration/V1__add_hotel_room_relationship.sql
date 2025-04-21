-- Add hotel_id column to room table if it doesn't exist
ALTER TABLE room ADD COLUMN IF NOT EXISTS hotel_id INT;

-- Add foreign key constraint
ALTER TABLE room 
ADD CONSTRAINT IF NOT EXISTS fk_room_hotel 
FOREIGN KEY (hotel_id) 
REFERENCES hotels(id);

-- Update existing rooms to belong to a hotel (if any exist)
-- This is a placeholder - you'll need to run this manually with appropriate hotel IDs
-- UPDATE room SET hotel_id = (SELECT id FROM hotels LIMIT 1) WHERE hotel_id IS NULL;

-- Make hotel_id not nullable after migration is complete
-- Uncomment this after ensuring all rooms have a hotel_id
-- ALTER TABLE room ALTER COLUMN hotel_id SET NOT NULL; 