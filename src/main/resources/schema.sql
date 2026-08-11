CREATE TABLE IF NOT EXISTS records (
                                       id SERIAL PRIMARY KEY,
                                       date VARCHAR(10) NOT NULL,
    result VARCHAR(10) NOT NULL,
    side VARCHAR(10) NOT NULL,
    opening VARCHAR(50) NOT NULL,
    moves INT NOT NULL
    );