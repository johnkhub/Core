CREATE TABLE finyear (
    id serial PRIMARY KEY,
    name varchar(40) NOT NULL,
    
    start_date date NOT NULL,
    end_date date NOT NULL,
    num_periods int NOT NULL DEFAULT 13 CHECK(num_periods > 0),
    current_period int NOT NULL DEFAULT 1 CHECK(current_period > 0 AND current_period < num_periods)
);

COMMENT ON COLUMN finyear.start_date IS 'The month and day of the the start of the financial year';
COMMENT ON COLUMN finyear.end_date IS 'The month and day of the end of the financial year';
COMMENT ON COLUMN finyear.num_periods IS 'The number of periods in this financial year';
COMMENT ON COLUMN finyear.current_period IS 'The current period ';