#!/usr/bin/env bb
(require
 '[babashka.tasks :as t]
 '[dda.backup.core :as bc]
 '[dda.backup.config :as cfg]
 '[dda.backup.postgresql :as pg]
 '[dda.backup.restore :as rs])

(def config (cfg/read-config "/usr/local/bin/config.edn"))

(defn prepare!
  []
  (bc/create-aws-credentials! (:aws-config config))
  (pg/create-pg-pass! (:db-config config)))

(defn restic-restore!
  []
  (pg/drop-create-db! (:db-config config))
  (rs/restore-db! (:db-config config))
  (rs/restore-file! (:file-restore-config config))
  (t/shell "mv /var/backups/restore/* /media")
  (t/shell "chown -R 999:999 /media"))

(prepare!)
(restic-restore!)
