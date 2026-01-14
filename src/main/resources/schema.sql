CREATE TABLE IF NOT EXISTS user_counter (
    user_id VARCHAR(255) PRIMARY KEY,
    counter INTEGER NOT NULL,
    version INTEGER NOT NULL
);

INSERT INTO user_counter (user_id, counter, version)
VALUES ('user_1', 0, 0)
ON CONFLICT (user_id) DO NOTHING;