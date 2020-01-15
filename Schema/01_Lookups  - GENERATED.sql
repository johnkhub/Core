CREATE TABLE "kv_base" (
  "k" varchar(10) PRIMARY KEY,
  "v" varchar,
  "creation_date" timestamp NOT NULL DEFAULT 'NOW()',
  "deactivated_at" timestamp,
  "allow_delete" boolean DEFAULT 'false'
);

CREATE TABLE "kv_base_bidirectional" (
  "k" varchar(10) PRIMARY KEY,
  "v" varchar
);

CREATE TABLE "kv_type" (
  "code" varchar(10) PRIMARY KEY,
  "name" varchar,
  "description" varchar,
  "owner" uuid UNIQUE
);

CREATE INDEX ON "kv_base" ("k");

CREATE UNIQUE INDEX ON "kv_base_bidirectional" ("v");

CREATE INDEX ON "kv_type" ("code");

CREATE INDEX ON "kv_type" ("owner");

COMMENT ON COLUMN "kv_base_bidirectional"."v" IS 'Inerits from KvBase';
