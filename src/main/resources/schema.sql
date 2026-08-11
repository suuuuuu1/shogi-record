CREATE TABLE IF NOT EXISTS records (
                                       id SERIAL PRIMARY KEY,
                                       date VARCHAR(10) NOT NULL,
    result VARCHAR(10) NOT NULL,
    side VARCHAR(10) NOT NULL,
    opening VARCHAR(50) NOT NULL,
    moves INT NOT NULL

    );

CREATE TABLE IF NOT EXISTS games(
    id SERIAL PRIMARY KEY,
    winner VARCHAR(10),
    kifu TEXT
);