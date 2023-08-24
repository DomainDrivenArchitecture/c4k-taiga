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


(defn-spec generate-ingress-and-cert cp/map-or-seq?
  [config config?]
  (ing/generate-ingress-and-cert
   (merge
    {:service-name "taiga"
     :service-port 80}
    config)))