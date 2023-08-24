(ns dda.c4k-taiga.taiga-test
  (:require
   #?(:cljs [shadow.resource :as rc])
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [clojure.spec.alpha :as s]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-taiga.core :as cut]
   [clojure.spec.alpha :as s]))

(deftest dummy-taiga-test
  (is true))