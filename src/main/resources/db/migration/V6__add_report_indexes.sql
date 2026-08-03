-- Indexes supporting the Report module aggregations.
-- The report endpoints group installments by month for a single user, so every query
-- starts from transactions(user_id) and filters installment(debt_date) / installment(paid_date).

-- Installment -> Transaction join is executed on every report query
CREATE INDEX IF NOT EXISTS idx_installment_transaction_id
    ON installment (transaction_id);

-- Monthly grouping by debt date (soft deleted rows are never reported)
CREATE INDEX IF NOT EXISTS idx_installment_debt_date
    ON installment (debt_date)
    WHERE is_removed = false;

-- Remaining debt reconstruction walks paid installments by paid date
CREATE INDEX IF NOT EXISTS idx_installment_paid_date
    ON installment (paid_date)
    WHERE is_removed = false AND is_paid = true;

-- User scoped access is the entry point of every report query
CREATE INDEX IF NOT EXISTS idx_transactions_user_id
    ON transactions (user_id)
    WHERE is_removed = false;

-- Tag breakdown and top expenses resolve tags per transaction
CREATE INDEX IF NOT EXISTS idx_transaction_tag_transaction_id
    ON transaction_tag (transaction_id)
    WHERE is_removed = false;

CREATE INDEX IF NOT EXISTS idx_transaction_tag_tag_id
    ON transaction_tag (tag_id)
    WHERE is_removed = false;
