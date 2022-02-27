#!/bin/bash
echo "########### Setting up Postgres DB ###########"

pg_restore --no-privileges --no-owner -U postgres -d university --clean /tmp/dump/university.sql