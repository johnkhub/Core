CREATE TABLE "kv_base" (
  "k" varchar(10) PRIMARY KEY CHECK(k  <> '' AND k ~'^[\w]*$'),
  "v" varchar CHECK(k  <> ''),
  "creation_date" timestamp NOT NULL DEFAULT 'NOW()',
  "deactivated_at" timestamp NULL,
  "allow_delete" boolean DEFAULT 'false'
);


CREATE FUNCTION table_exists (
    fqn TEXT
)
RETURNS boolean
AS $$
BEGIN
    IF EXISTS (SELECT * FROM information_schema.tables WHERE table_schema = split_part(fqn,'.',1) AND table_name = split_part(fqn,'.',2)) THEN
      RETURN  true;
    ELSE
      RETURN false;
    END IF;
END; $$ LANGUAGE PLPGSQL;

CREATE TABLE "kv_type" (
  "code" varchar(10) PRIMARY KEY CHECK(code  <> '' AND code  ~'^[\w]*$'),
  "name" varchar NOT NULL CHECK(name  <> ''),
  "description" varchar,
  "owner" uuid UNIQUE,
  "table" varchar UNIQUE CHECK( table_exists("table"))
);

-- CREATE INDEX ON "kv_base" ("k"); This is pointless. It does not get inherited using the INHERITED keyword

CREATE INDEX ON "kv_type" ("code");
CREATE INDEX ON "kv_type" ("owner");
CREATE INDEX ON "kv_type" ("table");
COMMENT ON COLUMN "kv_type"."table" IS 'FQN name of the table that contains the values';