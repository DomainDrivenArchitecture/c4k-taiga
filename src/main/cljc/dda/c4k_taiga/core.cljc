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
           (postgres/generate-service)
           ; TODO: Add configuration and authentication to relevant calls
           (taiga/generate-async-rabbitmq-deployment)
           (taiga/generate-async-rabbitmq-service)
           (taiga/generate-async-service)
           (taiga/generate-back-deployment)
           (taiga/generate-back-service)
           (taiga/generate-configmap)
           (taiga/generate-data-pvcs)
           (taiga/generate-events-deployment)
           (taiga/generate-events-rabbitmq-deployment)
           (taiga/generate-events-rabbitmq-service)
           (taiga/generate-events-service)
           (taiga/generate-front-deployment)
           (taiga/generate-front-service)
           (taiga/generate-gateway-configmap)
           (taiga/generate-gateway-deployment)
           (taiga/generate-gateway-service)
           (taiga/generate-protected-deployment)
           (taiga/generate-protected-service)
           (taiga/generate-rabbitmq-pvc)
           (taiga/generate-secret)]
          (taiga/generate-ingress-and-cert)
          (when (:contains? config :mon-cfg)
            (mon/generate (:mon-cfg config) (:mon-auth auth))))))))
