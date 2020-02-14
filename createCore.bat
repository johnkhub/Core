"c:\Program Files\PostgreSQL\10\bin\dropdb" --username=postgres "CoreFrank"
"c:\Program Files\PostgreSQL\10\bin\createdb" --username=postgres "CoreFrank" || exit/b 1
"c:\Program Files\PostgreSQL\10\bin\psql.exe" -v ON_ERROR_STOP=1 --dbname=CoreFrank --host=localhost --username=postgres < create.sql  || exit/b 1