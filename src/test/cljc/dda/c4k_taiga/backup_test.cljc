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
                              :app.kubernetes.io/part-of "c4k-taiga"}}
          :data
          {:restic-repository "s3:restic-repository"}}
         (cut/generate-config {:restic-repository "s3:restic-repository"}))))

(deftest should-generate-secret
  (is (= {:apiVersion "v1"
          :kind "Secret"
          :metadata {:name "backup-secret", :namespace "taiga"}
          :type "Opaque"
          :data
          {:aws-access-key-id "YXdzLWlk",
           :aws-secret-access-key "YXdzLXNlY3JldA==",
           :restic-password "cmVzdGljLXB3"}}
         (cut/generate-secret {:aws-access-key-id "aws-id"
                               :aws-secret-access-key "aws-secret"
                               :restic-password "restic-pw"})))
  (is (= {:apiVersion "v1"
          :kind "Secret"
          :metadata {:name "backup-secret", :namespace "taiga"}
          :type "Opaque"
          :data
          {:aws-access-key-id "YXdzLWlk",
           :aws-secret-access-key "YXdzLXNlY3JldA==",
           :restic-password "cmVzdGljLXB3"
           :restic-new-password "bmV3LXJlc3RpYy1wdw=="}}
         (cut/generate-secret {:aws-access-key-id "aws-id"
                               :aws-secret-access-key "aws-secret"
                               :restic-password "restic-pw"
                               :restic-new-password "new-restic-pw"}))))