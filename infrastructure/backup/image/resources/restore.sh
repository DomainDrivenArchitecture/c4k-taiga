#!/bin/bash

set -eux

function main() {

    file_env AWS_ACCESS_KEY_ID
    file_env AWS_SECRET_ACCESS_KEY

    file_env POSTGRES_DB
    file_env POSTGRES_PASSWORD
    file_env POSTGRES_USER

    # Restore latest snapshot into /var/backups/restore
    restore-directory '/var/backups/restore'
    
    mv /var/backups/restore/* /media

    # adjust file permissions for the taiga user
    chown -R 999:999 /media

    # Restore db
    drop-create-db
    restore-db
}

source /usr/local/lib/functions.sh
source /usr/local/lib/pg-functions.sh
source /usr/local/lib/file-functions.sh

main
