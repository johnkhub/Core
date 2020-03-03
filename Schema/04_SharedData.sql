CREATE TABLE postal_code (
  id serial PRIMARY KEY,
  suburb text,
  box_code text,
  street_code text, 
  area text
);

 CREATE INDEX ON postal_code (suburb,box_code);
 CREATE INDEX ON postal_code (suburb,street_code);

/*
CREATE TABLE "supplier" (
  "code" varchar(10) UNIQUE PRIMARY KEY,
  "name" varchar UNIQUE NOT NULL,
  "description" varchar,
  "supplier_type_code" varchar(10) NOT NULL
);

CREATE TABLE "supplier_type" (
  "code" varchar(10) UNIQUE PRIMARY KEY,
  "name" varchar NOT NULL,
  "description" varchar
);

ALTER TABLE "supplier" ADD FOREIGN KEY ("supplier_type_code") REFERENCES "supplier_type" ("code");

CREATE INDEX ON "supplier" ("code");
*/



