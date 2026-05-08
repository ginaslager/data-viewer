CREATE TABLE IF NOT EXISTS roofvogel (
    id   BIGINT PRIMARY KEY,
    naam VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS dier (
    id           BIGINT PRIMARY KEY,
    naam         VARCHAR(255),
    roofvogel_id BIGINT,
    FOREIGN KEY (roofvogel_id) REFERENCES roofvogel(id)
);

CREATE TABLE IF NOT EXISTS functie (
    id     BIGINT PRIMARY KEY,
    naam   VARCHAR(255),
    dier_id BIGINT,
    FOREIGN KEY (dier_id) REFERENCES dier(id)
);

CREATE TABLE IF NOT EXISTS slang (
    id           BIGINT PRIMARY KEY,
    naam         VARCHAR(255),
    roofvogel_id BIGINT,
    FOREIGN KEY (roofvogel_id) REFERENCES roofvogel(id)
);

CREATE TABLE IF NOT EXISTS kip (
    id           BIGINT PRIMARY KEY,
    naam         VARCHAR(255),
    kip_slang_id BIGINT,
    dier_id      BIGINT,
    FOREIGN KEY (dier_id) REFERENCES dier(id)
);
