-- Add status column to installment table for tracking active/skipped installments
ALTER TABLE installment ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE';
