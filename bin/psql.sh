#!/usr/bin/env bash

docker exec -ti --user postgres cgg_db_1 psql -Ucgg_role cggdb
