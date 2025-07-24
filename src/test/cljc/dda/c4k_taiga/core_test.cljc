(ns dda.c4k-taiga.core-test
  (:require
   [clojure.test :refer [deftest is are testing run-tests]]
   [clojure.spec.alpha :as s]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-taiga.core :as cut]))

(deftest validate-valid-resources
  (is (s/valid? ::cut/config (yaml/load-as-edn "taiga-test/valid-config.yaml")))
  (is (s/valid? ::cut/auth (yaml/load-as-edn "taiga-test/valid-auth.yaml"))))

(deftest test-whole-generation 
  (is (= 53
         (count 
          (cut/config-objects []
           (yaml/load-as-edn "taiga-test/valid-config.yaml")))))
  (is (= 4
         (count
          (cut/auth-objects []
           (yaml/load-as-edn "taiga-test/valid-config.yaml")
           (yaml/load-as-edn "taiga-test/valid-auth.yaml"))))))
