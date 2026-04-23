-- Users
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  full_name VARCHAR(255),
  email VARCHAR(255),
  password VARCHAR(255),
  phone VARCHAR(255),
  is_removed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP
);

-- Account
CREATE TABLE account (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255),
  type VARCHAR(50),
  currency VARCHAR(50),
  total_balance NUMERIC(19, 2),
  balance NUMERIC(19, 2),
  user_id BIGINT REFERENCES users(id),
  is_removed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP
);

-- Contact
CREATE TABLE contact (
  id BIGSERIAL PRIMARY KEY,
  full_name VARCHAR(255),
  note TEXT,
  user_id BIGINT REFERENCES users(id),
  is_removed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP
);

-- Transactions
CREATE TABLE transactions (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255),
  user_id BIGINT REFERENCES users(id),
  contact_id BIGINT REFERENCES contact(id),
  account_id BIGINT NOT NULL REFERENCES account(id),
  type VARCHAR(50),
  status VARCHAR(50),
  total_amount NUMERIC(19, 2),
  paid_amount NUMERIC(19, 2),
  total_installment INT,
  description VARCHAR(255),
  debt_date DATE,
  is_removed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP
);

-- Installment
CREATE TABLE installment (
  id BIGSERIAL PRIMARY KEY,
  transaction_id BIGINT REFERENCES transactions(id),
  installment_number INT,
  amount NUMERIC(19, 2),
  is_paid BOOLEAN NOT NULL DEFAULT false,
  paid_date DATE,
  description VARCHAR(255),
  debt_date DATE,
  is_removed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP
);

-- Category (will be renamed to Tag in V3)
CREATE TABLE category (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255),
  user_id BIGINT REFERENCES users(id),
  is_removed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP
);

-- TransactionCategory (will be renamed to TransactionTag in V3)
CREATE TABLE transaction_category (
  id BIGSERIAL PRIMARY KEY,
  transaction_id BIGINT REFERENCES transactions(id),
  category_id BIGINT REFERENCES category(id),
  is_removed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP
);

-- MonthlySummary
CREATE TABLE monthly_summary (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT REFERENCES users(id),
  year INT,
  month INT,
  total_income NUMERIC(19, 2),
  total_expense NUMERIC(19, 2),
  total_waiting_income NUMERIC(19, 2),
  total_waiting_expense NUMERIC(19, 2),
  type VARCHAR(50),
  is_removed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP
);
