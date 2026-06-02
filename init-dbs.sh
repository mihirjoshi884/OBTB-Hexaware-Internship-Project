#!/bin/bash
set -e

create_database() {
    local db=$1
    echo "Initializing database: $db"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        SELECT 'CREATE DATABASE $db'
        WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$db')\gexec
EOSQL
}

# Creating individual databases for your Spring Boot microservices
create_database "oauth_service_db"
create_database "user_service_db"
create_database "bus_service_db"
create_database "booking_service_db"
create_database "transaction_service_db"