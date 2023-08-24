(ns dda.c4k-taiga.core
  (:require
   [clojure.spec.alpha :as s]
   #?(:clj [orchestra.core :refer [defn-spec]]
      :cljs [orchestra.core :refer-macros [defn-spec]])
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.predicate :as cp]
   [dda.c4k-common.monitoring :as mon]
   [dda.c4k-taiga.taiga :as taiga]
   [dda.c4k-common.postgres :as postgres]))

(def default-storage-class :local-path)

(def config? taiga/config?)
(def auth? taiga/auth?)

(defn-spec k8s-objects cp/map-or-seq?
  [config taiga/config?
   auth taiga/auth?]  
  (cm/concat-vec
   (map yaml/to-string
        (filter
         #(not (nil? %))
         (cm/concat-vec
          [(postgres/generate-config {:postgres-size :8gb :db-name "taiga"})
           (postgres/generate-secret auth)
           (postgres/generate-pvc {:pv-storage-size-gb 50
                                   :pvc-storage-class-name default-storage-class})
           (postgres/generate-deployment)
           (postgres/generate-service)]
          (taiga/generate-ingress-and-cert)
          (when (:contains? config :mon-cfg)
            (mon/generate (:mon-cfg config) (:mon-auth auth))))))))
