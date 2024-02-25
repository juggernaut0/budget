CREATE TABLE budget
(
    user_id UUID NOT NULL ,
    name    TEXT NOT NULL,
    version INT NOT NULL,
    data    JSONB NOT NULL,
    PRIMARY KEY (user_id, name)
);
