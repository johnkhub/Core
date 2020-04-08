"c:\Program Files\PostgreSQL\10\bin\psql.exe" -v ON_ERROR_STOP=1 --dbname=test_core --host=localhost --username=importer < import.sql  || exit/b 1
