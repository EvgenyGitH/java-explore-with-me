DROP TABLE IF EXISTS endpointhits CASCADE;

CREATE TABLE IF NOT EXISTS endpointhits (
id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
app VARCHAR (128) NOT NULL,
uri VARCHAR (1024) NOT NULL,
ip VARCHAR (128) NOT NULL,
created TIMESTAMP WITHOUT TIME ZONE NOT NULL
)