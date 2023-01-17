#!/usr/bin/env bash

docker exec -ti --user postgres clojure-game-geek-db-1 psql -Ucgg_role cggdb
