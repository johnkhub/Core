rem "c:\Program Files\PostgreSQL\10\bin\psql.exe" -v ON_ERROR_STOP=1 --dbname=CoreFrank --host=localhost --username=importer --password=importer  < import.sql  || exit/b 1
"c:\Program Files\PostgreSQL\10\bin\psql.exe" -v ON_ERROR_STOP=1 --dbname=CoreFrank --host=localhost --username=postgres < import.sql  || exit/b 1