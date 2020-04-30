CREATE TABLE "field" (
  "name" varchar UNIQUE PRIMARY KEY,
  "fqn" varchar UNIQUE,
  "type" varchar(10)
);
GO