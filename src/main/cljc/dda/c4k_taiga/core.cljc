(ns dda.c4k-taiga.core
  (:require
   [clojure.spec.alpha :as s]
   #?(:clj [orchestra.core :refer [defn-spec]]
      :cljs [orchestra.core :refer-macros [defn-spec]])
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.predicate :as cp]
   [dda.c4k-common.monitoring :as mon]
   [dda.c4k-taiga.taiga :as taiga]))

(def config-defaults {:issuer "staging"
                      :volume-size "3"})

(s/def ::mon-cfg ::mon/mon-cfg)
(s/def ::mon-auth ::mon/mon-auth)

; ToDo
(def config? (s/keys :req-un []
                     :opt-un [::mon-cfg]))

; ToDo
(def auth? (s/keys :req-un []
                   :opt-un [::mon-auth]))

; ToDo:
(defn generate-configs [config auth])

(defn-spec k8s-objects cp/map-or-seq?
  [config config?
   auth auth?]  
  (cm/concat-vec
   (map yaml/to-string
        (filter
         #(not (nil? %))
         (cm/concat-vec
          (generate-configs config auth)
          (when (:contains? config :mon-cfg)
            (mon/generate (:mon-cfg config) (:mon-auth auth))))))))
