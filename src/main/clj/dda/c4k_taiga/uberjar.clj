(ns dda.c4k-taiga.uberjar
  (:gen-class)
  (:require
   [dda.c4k-common.uberjar :as uberjar]
   [dda.c4k-taiga.core :as core]))


(defn -main [& cmd-args]
  (uberjar/main-common
   "c4k-taiga"
   core/config?
   core/auth?
   core/config-defaults
   core/k8s-objects
   cmd-args))
