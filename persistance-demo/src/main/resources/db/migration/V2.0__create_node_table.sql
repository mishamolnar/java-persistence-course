CREATE TABLE notes
(
    id BIGSERIAL PRIMARY KEY ,
    title TEXT NOT NULL ,
    body TEXT NOT NULL,
    person_id BIGINT REFERENCES persons(id)
)