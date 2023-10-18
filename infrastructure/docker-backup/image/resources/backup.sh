#!/bin/bash

set -o pipefail

function main() {
    file_env AWS_ACCESS_KEY_ID
    file_env AWS_SECRET_ACCESS_KEY
    file_env RESTIC_DAYS_TO_KEEP 30
    file_env RESTIC_MONTHS_TO_KEEP 12

    backup-db-dump
    backup-directory "/media"
}

source /usr/local/lib/functions.sh
source /usr/local/lib/pg-functions.sh
source /usr/local/lib/file-functions.sh

main
