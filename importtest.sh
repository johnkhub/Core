#!/bin/bash
psql -v ON_ERROR_STOP=1 --dbname=core-dtpw-uat-21may --host=pcs-dtpw-qa --username=postgres < burgert.sql