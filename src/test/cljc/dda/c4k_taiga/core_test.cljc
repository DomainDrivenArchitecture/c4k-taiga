(ns dda.c4k-taiga.core-test
  (:require
   #?(:cljs [shadow.resource :as rc])
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [clojure.spec.alpha :as s]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-taiga.core :as cut]))

#?(:cljs
   (defmethod yaml/load-resource :website-test [resource-name]
     (case resource-name
       "taiga-test/valid-config.yaml" (rc/inline "taiga-test/valid-config.yaml")
       "taiga-test/valid-auth.yaml" (rc/inline "taiga-test/valid-auth.yaml")
       (throw (js/Error. "Undefined Resource!")))))

(deftest validate-valid-resources
  (is (s/valid? cut/config? (yaml/load-as-edn "taiga-test/valid-config.yaml")))
  (is (s/valid? cut/auth? (yaml/load-as-edn "taiga-test/valid-auth.yaml"))))
