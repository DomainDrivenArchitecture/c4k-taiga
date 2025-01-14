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
   [dda.c4k-taiga.backup :as backup]
   [dda.c4k-common.postgres :as postgres]
   [dda.c4k-common.namespace :as ns]))

(def defaults {:namespace "taiga"
               :issuer "staging"
               :storage-class-name "local-path"
               :pv-storage-size-gb "5"
               :storage-media-size "5"
               :storage-static-size "5"
               :storage-async-rabbitmq-size "5"
               :storage-events-rabbitmq-size "5"
               :public-register-enabled "false"
               :enable-telemetry "false"})

(def config? (s/merge
              ::backup/config              
              (s/keys :req-un [::taiga/fqdn]
                      :opt-un [::taiga/issuer
                               ::taiga/storage-class-name
                               ::taiga/storage-media-size
                               ::taiga/storage-static-size
                               ::taiga/storage-async-rabbitmq-size
                               ::taiga/storage-events-rabbitmq-size
                               ::taiga/public-register-enabled
                               ::taiga/enable-telemetry
                               ::postgres/pv-storage-size-gb
                               ::mon/mon-cfg])))

(def auth? (s/merge
            ::backup/auth
            (s/keys :req-un [::postgres/postgres-db-user
                             ::postgres/postgres-db-password
                             ::taiga/taiga-secret-key
                             ::taiga/mailer-pw
                             ::taiga/mailer-user
                             ::taiga/django-superuser-email
                             ::taiga/django-superuser-password
                             ::taiga/django-superuser-username
                             ::taiga/rabbitmq-erlang-cookie
                             ::taiga/rabbitmq-pw
                             ::taiga/rabbitmq-user
                             ::mon/mon-auth])))

(defn-spec config-objects cp/map-or-seq?
  [config config?]
  (let [resolved-config (merge defaults config)
        db-config (merge resolved-config {:postgres-size :8gb :db-name "taiga"
                                          :pv-storage-size-gb 50})]
  (cm/concat-vec
    (map yaml/to-string
         (filter
          #(not (nil? %))
          (cm/concat-vec
           (ns/generate resolved-config)
           (postgres/generate-config db-config)
           [(taiga/generate-async-deployment)
            (taiga/generate-async-rabbitmq-deployment)
            (taiga/generate-async-rabbitmq-service)
            (taiga/generate-async-service)
            (taiga/generate-back-deployment)
            (taiga/generate-back-service)
            (taiga/generate-configmap resolved-config)
            (taiga/generate-pvc-taiga-media-data resolved-config)
            (taiga/generate-pvc-taiga-static-data resolved-config)
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
            (taiga/generate-rabbitmq-pvc-async resolved-config)
            (taiga/generate-rabbitmq-pvc-events resolved-config)]
           (taiga/generate-ingress-and-cert resolved-config)
           (when (contains? resolved-config :restic-repository)
             [(backup/generate-config resolved-config)
              (backup/generate-cron)
              (backup/generate-backup-restore-deployment resolved-config)])
           (when (:contains? resolved-config :mon-cfg)
             (mon/generate-config))))))))

(defn-spec auth-objects cp/map-or-seq?
  [config config?
   auth auth?]
  (let [resolved-config (merge defaults config)]
  (cm/concat-vec
    (map yaml/to-string
         (filter
          #(not (nil? %))
          (cm/concat-vec
           [(postgres/generate-secret auth)
            (taiga/generate-secret auth)]
           (when (contains? resolved-config :restic-repository)
             [(backup/generate-secret auth)])
           (when (:contains? resolved-config :mon-cfg)
             (mon/generate-auth (:mon-cfg resolved-config) (:mon-auth auth)))))))))
