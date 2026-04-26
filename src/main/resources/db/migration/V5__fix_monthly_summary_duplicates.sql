-- Remove duplicate monthly_summary records, keeping the one with the highest id
DELETE FROM monthly_summary
WHERE id NOT IN (
    SELECT MAX(id)
    FROM monthly_summary
    WHERE is_removed = false
    GROUP BY user_id, year, month, type
)
AND is_removed = false;

-- Add unique index to prevent future duplicates
CREATE UNIQUE INDEX uq_monthly_summary_user_year_month_type
    ON monthly_summary (user_id, year, month, type)
    WHERE is_removed = false;
