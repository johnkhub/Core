#!/bin/bash
psql -v ON_ERROR_STOP=1 --dbname=test_core --host=localhost --username=importer < import.sql