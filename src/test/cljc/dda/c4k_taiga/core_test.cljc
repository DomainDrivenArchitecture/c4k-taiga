(ns dda.c4k-taiga.core-test
  (:require
   #?(:cljs [dda.c4k-common.macros :refer-macros [inline-resources]])
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [clojure.spec.alpha :as s]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-taiga.core :as cut]))

#?(:cljs
   (defmethod yaml/load-resource :taiga-test [resource-name]
     (get (inline-resources "taiga-test") resource-name)))

(deftest validate-valid-resources
  (is (s/valid? cut/config? (yaml/load-as-edn "taiga-test/valid-config.yaml")))
  (is (s/valid? cut/auth? (yaml/load-as-edn "taiga-test/valid-auth.yaml"))))

(deftest test-whole-generation 
  (is (= 49
         (count 
          (cut/config-objects 
           (yaml/load-as-edn "taiga-test/valid-config.yaml")))))
  (is (= 4
         (count
          (cut/auth-objects 
           (yaml/load-as-edn "taiga-test/valid-config.yaml")
           (yaml/load-as-edn "taiga-test/valid-auth.yaml"))))))
