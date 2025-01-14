(ns dda.c4k-taiga.backup-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [clojure.spec.test.alpha :as st]
   [dda.c4k-taiga.backup :as cut]))

(st/instrument `cut/generate-secret)
(st/instrument `cut/generate-config)
(st/instrument `cut/generate-cron)

(deftest should-generate-config
  (is (= {:apiVersion "v1"
          :kind "ConfigMap"
          :metadata {:name "backup-config"
                     :namespace "taiga"
                     :labels {:app.kubernetes.io/name "backup"
                              :app.kubernetes.io/part-of "taiga"}}
          :data
          {:restic-repository "s3:restic-repository"}}
         (cut/generate-config {:restic-repository "s3:restic-repository"}))))