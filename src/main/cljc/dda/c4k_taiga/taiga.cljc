(ns dda.c4k-taiga.taiga
  (:require
   [clojure.spec.alpha :as s]
   #?(:cljs [shadow.resource :as rc])
   #?(:clj [orchestra.core :refer [defn-spec]]
      :cljs [orchestra.core :refer-macros [defn-spec]])
   #?(:clj [clojure.edn :as edn]
      :cljs [cljs.reader :as edn])
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.base64 :as b64]
   [dda.c4k-common.predicate :as cp]
   [dda.c4k-common.monitoring :as mon]
   [dda.c4k-common.postgres :as postgres]
   [dda.c4k-common.ingress :as ing]
   [clojure.string :as str]))


(def config-defaults {:issuer "staging"
                      :volume-size "3"})

(s/def ::mon-cfg ::mon/mon-cfg)
(s/def ::mon-auth ::mon/mon-auth)

(s/def ::fqdn cp/fqdn-string?)
(s/def ::issuer cp/letsencrypt-issuer?)
; TODO: Passwords

(def config? (s/keys :req-un [::fqdn]
                     :opt-un [::issuer
                              ::pv-storage-size-gb
                              ::pvc-storage-class-name
                              ::mon-cfg]))

(def auth? (s/keys :req-un [::postgres/postgres-db-user ::postgres/postgres-db-password]
                   :opt-un [::mon-auth]))


#?(:cljs
   (defmethod yaml/load-resource :taiga [resource-name]
     (case resource-name
       "taiga/events-rabbitmq-deployment.yaml"        (rc/inline "taiga/events-rabbitmq-deployment.yaml")
       "taiga/gateway-deployment.yaml"                (rc/inline "taiga/gateway-deployment.yaml")
       "taiga/protected-deployment.yaml"              (rc/inline "taiga/protected-deployment.yaml")
       "taiga/gateway-configmap.yaml"                 (rc/inline "taiga/gateway-configmap.yaml")
       "taiga/configmap.yaml"                         (rc/inline "taiga/configmap.yaml")
       "taiga/async-service.yaml"                     (rc/inline "taiga/async-service.yaml")
       "taiga/events-deployment.yaml"                 (rc/inline "taiga/events-deployment.yaml")
       "taiga/async-deployment.yaml"                  (rc/inline "taiga/async-deployment.yaml")
       "taiga/back-deployment.yaml"                   (rc/inline "taiga/back-deployment.yaml")
       "taiga/front-deployment.yaml"                  (rc/inline "taiga/front-deployment.yaml")
       "taiga/front-service.yaml"                     (rc/inline "taiga/front-service.yaml")
       "taiga/gateway-service.yaml"                   (rc/inline "taiga/gateway-service.yaml")
       "taiga/data-pvcs.yaml"                         (rc/inline "taiga/data-pvcs.yaml")
       "taiga/async-rabbitmq-deployment.yaml"         (rc/inline "taiga/async-rabbitmq-deployment.yaml")
       "taiga/protected-service.yaml"                 (rc/inline "taiga/protected-service.yaml")
       "taiga/secret.yaml"                            (rc/inline "taiga/secret.yaml")
       "taiga/async-rabbitmq-service.yaml"            (rc/inline "taiga/async-rabbitmq-service.yaml")
       "taiga/events-service.yaml"                    (rc/inline "taiga/events-service.yaml")
       "taiga/back-service.yaml"                      (rc/inline "taiga/back-service.yaml")
       "taiga/events-rabbitmq-service.yaml"           (rc/inline "taiga/events-rabbitmq-service.yaml")
       "taiga/rabbitmq-pvc.yaml"                      (rc/inline "taiga/rabbitmq-pvc.yaml")
       (throw (js/Error. "Undefined Resource!")))))

(defn-spec generate-ingress-and-cert cp/map-or-seq?
  [config config?]
  (ing/generate-ingress-and-cert
   (merge
    {:service-name "taiga"
     :service-port 80}
    config)))

; TODO: Check which ones need configuration or authentication information
(defn-spec generate-events-rabbitmq-deployment cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/events-rabbitmq-deployment.yaml")))

(defn-spec generate-gateway-deployment cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/gateway-deployment.yaml")))

(defn-spec generate-protected-deployment cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/protected-deployment.yaml")))

(defn-spec generate-gateway-configmap cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/gateway-configmap.yaml")))

(defn-spec generate-configmap cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/configmap.yaml")))

(defn-spec generate-async-service cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/async-service.yaml")))

(defn-spec generate-events-deployment cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/events-deployment.yaml")))

(defn-spec generate-async-deployment cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/async-deployment.yaml")))

(defn-spec generate-back-deployment cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/back-deployment.yaml")))

(defn-spec generate-front-deployment cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/front-deployment.yaml")))

(defn-spec generate-front-service cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/front-service.yaml")))

(defn-spec generate-gateway-service cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/gateway-service.yaml")))

(defn-spec generate-data-pvcs cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/data-pvcs.yaml")))

(defn-spec generate-async-rabbitmq-deployment cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/async-rabbitmq-deployment.yaml")))

(defn-spec generate-protected-service cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/protected-service.yaml")))

(defn-spec generate-secret cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/secret.yaml")))

(defn-spec generate-async-rabbitmq-service cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/async-rabbitmq-service.yaml")))

(defn-spec generate-events-service cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/events-service.yaml")))

(defn-spec generate-back-service cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/back-service.yaml")))

(defn-spec generate-events-rabbitmq-service cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/events-rabbitmq-service.yaml")))

(defn-spec generate-rabbitmq-pvc cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "taiga/rabbitmq-pvc.yaml")))

