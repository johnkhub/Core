rem Load the stored procedures and utility stuff
sqlcmd -d BCM  -i createField.sql || exit /b 1
sqlcmd -d BCM  -i 00_asset.sql || exit /b 1
sqlcmd -d BCM  -i 01_components.sql || exit /b 1
sqlcmd -d BCM  -i "..\Master Data\FieldNamesMerged.sql" || exit /b 1
sqlcmd -d BCM  -i 02_facilities.sql || exit /b 1
sqlcmd -d BCM  -i 03_floors.sql || exit /b 1
sqlcmd -d BCM  -i 04_rooms.sql || exit /b 1
sqlcmd -d BCM  -i 10_transactions.sql || exit /b 1
sqlcmd -d BCM  -i 11_attributes.sql || exit /b 1

rem Run the conversion
sqlcmd -d BCM  -i run.sql || exit /b 1

