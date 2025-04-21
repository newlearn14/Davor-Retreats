-- Add price column to room table
ALTER TABLE room ADD COLUMN IF NOT EXISTS price DECIMAL(10,2) NOT NULL DEFAULT 0.00;

-- Update existing rooms to have a default price
UPDATE room SET price = 0.00 WHERE price IS NULL;
