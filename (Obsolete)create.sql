CREATE EXTENSION IF NOT EXISTS "ltree";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";	
CREATE EXTENSION IF NOT EXISTS "postgis";


CREATE TABLE settings (
    populate_security boolean  DEFAULT TRUE,
    populate_floors boolean  DEFAULT TRUE,
    populate_rooms boolean  DEFAULT TRUE

);


\i 'Schema/00_Core.sql'
\i 'Schema/01_Lookups.sql'
\i 'Schema/02_LookupTables.sql'
\i 'Schema/DTPW.sql'
\i 'Schema/03_LifeCycle & Financials.sql'
\i 'Schema/04_SharedData.sql'
\i 'Schema/05_Transactions.sql'
\i 'Schema/06_Access Control.sql'
\i 'Schema/07_Audit.sql'
\i 'Schema/08_FinYear.sql'
\i 'Schema/Reporting.sql'

\i 'Views/ar_lite_view.sql'
\i 'Views/asset_core_view.sql'
