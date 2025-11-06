-- Add summary_date column
ALTER TABLE monthly_summary ADD COLUMN summary_date DATE;

-- Backfill data from year + month
UPDATE monthly_summary
SET summary_date = MAKE_DATE(year, month, 1)
WHERE summary_date IS NULL;