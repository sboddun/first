CREATE TABLE IF NOT EXISTS customer_requests (
    id SERIAL PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL,
    start_date VARCHAR(50),
    end_date VARCHAR(50),
    page INTEGER,
    rec_limit INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
