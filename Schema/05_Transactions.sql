CREATE SCHEMA "transactions";

CREATE TYPE "transactions"."data_type" AS ENUM (
  'T_SLONG',
  'T_ULONG',
  'T_MONEY',
  'T_STRING',
  'T_BOOLEAN',
  'T_POLYGON',
  'T_DATETIME'
);

CREATE TABLE "transactions"."transaction" (
  "transaction_id" uuid PRIMARY KEY,
  "transaction_type_code" varchar(10) NOT NULL,
  "asset_id" uuid NOT NULL,
  "batch_id" uuid NOT NULL,
  "insert_date" timestamp NOT NULL DEFAULT (CURRENT_TIMESTAMP),
  "submit_date" timestamp NOT NULL,
  "effective_date" timestamp NOT NULL,
  "reason" varchar,
  
  "field" varchar NOT NULL,
  "delta_T_SLONG" bigint,
  "delta_T_ULONG" bigint,
  "amount" decimal(19,6),
  "delta_T_STRING" varchar,
  "delta_T_BOOLEAN" boolean,
  "delta_T_POLYGON" varchar,

  "correlation_id" uuid
);

CREATE TABLE "transactions"."transaction_type" (
  "code" varchar(10) UNIQUE PRIMARY KEY,
  "name" varchar NOT NULL CHECK(name <> ''),
  "description" text
);

CREATE TABLE "transactions"."transaction_batch" (
  "batch_id" uuid UNIQUE PRIMARY KEY,
  "comments" text
);

CREATE TABLE "transactions"."field" (
  "name" varchar UNIQUE PRIMARY KEY,
  "fqn" varchar UNIQUE CHECK(fqn <> ''),
  "type" "transactions"."data_type"
);

ALTER TABLE "transactions"."transaction" ADD FOREIGN KEY ("batch_id") REFERENCES "transactions"."transaction_batch" ("batch_id");

ALTER TABLE "transactions"."transaction" ADD FOREIGN KEY ("transaction_type_code") REFERENCES "transactions"."transaction_type" ("code");

ALTER TABLE "transactions"."transaction" ADD FOREIGN KEY ("field") REFERENCES "transactions"."field" ("name");

CREATE INDEX ON "transactions"."transaction" ("transaction_id", "asset_id");

CREATE INDEX ON "transactions"."transaction" ("batch_id");

CREATE INDEX ON "transactions"."transaction" ("correlation_id");

CREATE INDEX ON "transactions"."transaction" ("effective_date", "asset_id");

COMMENT ON COLUMN "transactions"."transaction"."delta_T_ULONG" IS 'Add checked constraint > 0 OR NULL';

COMMENT ON COLUMN "transactions"."transaction"."correlation_id" IS 'Correlate to messages within the system';

COMMENT ON COLUMN "transactions"."field"."fqn" IS '<schema>.<table>.<column> used to map values into snapshot tables';
