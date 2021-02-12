CREATE SCHEMA IF NOT EXISTS adm;
DROP TABLE IF EXISTS adm CASCADE;


CREATE TABLE adm.ref_depreciation_method() INHERITS (public.kv_base);
ALTER TABLE  adm.ref_depreciation_method ADD PRIMARY KEY (k);

CREATE TABLE adm.ref_lifecycle_grade()  INHERITS (public.kv_base);
ALTER TABLE  adm.ref_lifecycle_grade ADD PRIMARY KEY (k);

CREATE TABLE adm.ref_measurement_model()  INHERITS (public.kv_base);
ALTER TABLE  adm.ref_measurement_model ADD PRIMARY KEY (k);

CREATE TABLE adm.ref_crc_calc_method()  INHERITS (public.kv_base);
ALTER TABLE  adm.ref_crc_calc_method ADD PRIMARY KEY (k);

CREATE TABLE adm.ref_tax_deduction_method()  INHERITS (public.kv_base);
ALTER TABLE  adm.ref_tax_deduction_method ADD PRIMARY KEY (k);

CREATE TABLE adm.ref_component_location_type()  INHERITS (public.kv_base);
ALTER TABLE  adm.ref_component_location_type ADD PRIMARY KEY (k);
	
INSERT INTO public.kv_type (code,name,"table") VALUES ('DEP_METHOD','Depreciation method','adm.ref_depreciation_method') ON CONFLICT(k) DO NOTHING;
INSERT INTO public.kv_type (code,name,"table") VALUES ('LIFECYCLE_GRADE','Lifecycle grading', 'adm.ref_lifecycle_grade') ON CONFLICT(k) DO NOTHING;
INSERT INTO public.kv_type (code,name,"table") VALUES ('MEAS_MODEL','Measurement model', 'adm.ref_measurement_model') ON CONFLICT(k) DO NOTHING;
INSERT INTO public.kv_type (code,name,"table") VALUES ('TAX_DEDUCT_METHOD','Tax deduction method', 'adm.ref_tax_deduction_method') ON CONFLICT(k) DO NOTHING;
INSERT INTO public.kv_type (code,name,"table") VALUES ('COMP_LOC_TYPE','Component location type', 'adm.ref_component_location_type') ON CONFLICT(k) DO NOTHING;

INSERT INTO adm.ref_crc_calc_method(k,v) VALUES ('METHOD1', 'Extent x Unit Rate');
INSERT INTO adm.ref_crc_calc_method(k,v) VALUES ('METHOD2', 'Extent x Unit Rate x Descriptor Size');	
INSERT INTO adm.ref_crc_calc_method(k,v) VALUES ('METHOD3', 'Manually entered');	

INSERT INTO adm.ref_measurement_model(k,v) VALUES ('COST', 'Cost');
INSERT INTO adm.ref_measurement_model(k,v) VALUES ('REVAL', 'Revaluation');	
INSERT INTO adm.ref_measurement_model(k,v) VALUES ('FAIR_VAL', 'Fair Value');	

INSERT INTO adm.ref_lifecycle_grade(k,v) VALUES ('COND','Condition');
INSERT INTO adm.ref_lifecycle_grade(k,v) VALUES ('PERF','Performance');
INSERT INTO adm.ref_lifecycle_grade(k,v) VALUES ('UTIL','Utilisation');
INSERT INTO adm.ref_lifecycle_grade(k,v) VALUES ('CRIT','Criticality');
INSERT INTO adm.ref_lifecycle_grade(k,v) VALUES ('COST_OPS','Cost of Operations');


INSERT INTO adm.ref_component_location_type(k,v) VALUES ('SITE','Site-based');
INSERT INTO adm.ref_component_location_type(k,v) VALUES ('LINEAR','Linear infrastructure');
INSERT INTO adm.ref_component_location_type(k,v) VALUES ('GROUPED','Grouped infrastructure');
INSERT INTO adm.ref_component_location_type(k,v) VALUES ('LAND','Infrastructure Land');

INSERT INTO adm.ref_depreciation_method(k,v) VALUES ('NA','N/a');
INSERT INTO adm.ref_depreciation_method(k,v) VALUES ('SL','Straight line');
INSERT INTO adm.ref_depreciation_method(k,v) VALUES ('DIM_BAL','Diminishing balance');
INSERT INTO adm.ref_depreciation_method(k,v) VALUES ('PROD_UNITS','Prod Units');

CREATE TABLE adm.depreciation_method_link (
    id int PRIMARY KEY,
    adm_id uuid NOT NULL,
    depreciation_method varchar NOT NULL,   
    
    FOREIGN KEY (depreciation_method) REFERENCES adm.ref_depreciation_method (k),
    FOREIGN KEY (adm_id) REFERENCES adm.adm (adm_id)
);


CREATE TABLE adm.production_unit_parameters (
    id int PRIMARY KEY,
    expected_production_units numeric NOT NULL CHECK(expected_production_units > 0),
    production_unit_units varchar NOT NULL,

    FOREIGN KEY (id) REFERENCES adm.depreciation_method_link (id),
    FOREIGN KEY (production_unit_units) REFERENCES public.unit (code)
);

CREATE TABLE adm.fixed_depreciation_percentage_parameters (
    id int PRIMARY KEY,
    percentage numeric NOT NULL CHECK(percentage > 0),

    FOREIGN KEY (id) REFERENCES adm.depreciation_method_link (id)
);




CREATE TABLE maintenance_budget_allocation (
    id int PRIMARY KEY,
    adm_id uuid NOT NULL,
    preventative_percentage numeric NOT NULL,
    corrective_percentage numeric NOT NULL,
    

    CONSTRAINT ch_maintenance_budget_pct CHECK (preventative_percentage + corrective_percentage = 100)
);

CREATE TABLE maintenance_budget_need (
    id int PRIMARY KEY,
	maintenance_budget_allocation int NOT NULL,
    lifecycle_grade varchar,
    lifecycle_grade_criterium int CHECK (lifecycle_grade_criterium >= 1 AND lifecycle_grade_criterium <= 5),
	maintenance_budget_need_percentage numeric NOT NULL,
	
    FOREIGN KEY (lifecycle_grade) REFERENCES adm.ref_lifecycle_grade (k),
	FOREIGN KEY (maintenance_budget_allocation) REFERENCES adm.maintenance_budget_allocation (id)
);







-- Usage:
-- Supply a UUID or one will be generated upon insert
--
-- first write to this table to apply constraints and FK stuff, then write to tx log
CREATE TABLE adm.adm (
    adm_path ltree,
    adm_id uuid NOT NULL DEFAULT (uuid_generate_v4()),

    -- We use special sentinel values instead of NULL. This makes it less of a hassle to 
    -- create a unique index over these columns
    descriptor_type varchar(80) NOT NULL DEFAULT 'NO_TYPE',
    descriptor_size numeric NOT NULL DEFAULT 'NaN',
    descriptor_class varchar(40) NOT NULL DEFAULT 'NO_CLASS',
    descriptor_general varchar NOT NULL DEFAULT 'NO_GENERAL',

    unit_rate numeric NOT NULL, 
    unit_rate_unit varchar NOT NULL,
    descriptor_size_unit varchar NOT NULL,
    crc_calc_method varchar NOT NULL,
    
    eul numeric NOT NULL,
    eul_cg int NOT NULL CHECK(eul_cg >= 1 and eul_cg <= 5 ),
    
    residual_percentage numeric NOT NULL CHECK(residual_percentage >= 0 and residual_percentage <= 100 ),
    residual_percentage_cg int NOT NULL CHECK(residual_percentage_cg >= 0 and residual_percentage_cg <= 1),

    depreciation_method int NOT NULL,

    tax_deduction_rate numeric NULL,
    tax_deduction_method varchar NULL,

    measurement_model varchar NOT NULL,


    active boolean NOT NULL DEFAULT true,
    movable boolean NOT NULL DEFAULT false,
    location_type varchar NOT NULL,

    FOREIGN KEY (unit_rate_unit) REFERENCES public.unit (code),
    FOREIGN KEY (descriptor_size_unit) REFERENCES public.unit (code),

    FOREIGN KEY (measurement_model) REFERENCES adm.ref_measurement_model (k),
    FOREIGN KEY (crc_calc_method) REFERENCES adm.ref_crc_calc_method (k), 
    FOREIGN KEY (depreciation_method) REFERENCES adm.depreciation_method_link (id), 
    FOREIGN KEY (tax_deduction_method) REFERENCES adm.ref_tax_deduction_method (k) ,
    FOREIGN KEY (location_type) REFERENCES adm.ref_component_location_type (k)
);


CREATE UNIQUE INDEX adm_param_unique ON adm.adm (
    descriptor_type,
    descriptor_size,
    descriptor_class,
    descriptor_general    
);


INSERT INTO transactions.field ("name",fqn,"type")
	SELECT "name","fqn","type"::transactions.data_type  FROM field_reverese_mapping_view
ON CONFLICT ("name") 
DO UPDATE SET fqn = EXCLUDED.fqn, "type" = EXCLUDED.type;


INSERT INTO transactions.transaction_type (code,"name") VALUES ("CREATE");
INSERT INTO transactions.transaction_type (code,"name") VALUES ("UPDATE");

INSERT INTO transaction.batch (batch_id, comments) VALUES (uuid_generate_v4(), "Initial load");


COPY transaction.transaction FROM 'transactions.csv' DELIMETER ',' CSV HEADER;



  "transaction_id" uuid PRIMARY KEY,
  "transaction_type_code" varchar(10) NOT NULL,
  "entity_id" uuid NOT NULL,
  "batch_id" uuid NOT NULL,

  "submit_date" timestamp NOT NULL,
  "effective_date" timestamp NOT NULL,
  "reason" varchar,
  
  "field" varchar NOT NULL,
  "delta_T_SLONG" bigint,
  "delta_T_ULONG" bigint,
  "amount" decimal(19,6),
  "delta_T_STRING" varchar,
  "delta_T_BOOLEAN" boolean,
  "delta_T_POLYGON" text,
  "delta_T_DATETIME" timestamp,
  "delta_T_PATH" ltree,
   "delta_T_UUID" uuid,

  "correlation_id" uuid


