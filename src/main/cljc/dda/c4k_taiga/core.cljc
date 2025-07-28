(ns dda.c4k-taiga.core
  (:require
   [clojure.spec.alpha :as s]
   [orchestra.core :refer [defn-spec]]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.predicate :as cp]
   [dda.c4k-common.monitoring :as mon]
   [dda.c4k-taiga.taiga :as taiga]
   [dda.c4k-taiga.backup :as backup]
   [dda.c4k-common.ingress :as ing]
   [dda.c4k-common.postgres :as postgres]
   [dda.c4k-common.namespace :as ns]))

(def config-defaults {:namespace "taiga"
                      :issuer "staging"
                      :storage-class-name "local-path"
                      :pv-storage-size-gb "5"
                      :storage-media-size "5"
                      :storage-static-size "5"
                      :storage-async-rabbitmq-size "5"
                      :storage-events-rabbitmq-size "5"
                      :public-register-enabled "false"
                      :enable-telemetry "false"})

(s/def ::config (s/merge
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

(s/def ::auth (s/merge
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

(s/def ::config-select (s/* #{"auth" "deployment"}))

(defn-spec config-objects cp/map-or-seq?
  [config-select ::config-select
   config ::config]
  (let [resolved-config (merge config-defaults config)
        {:keys [fqdn]} resolved-config
        config-parts (if (empty? config-select)
                       ["auth" "deployment"]
                       config-select)]
    (cm/concat-vec
     (map yaml/to-string
          (if (some #(= "deployment" %) config-parts)
            (cm/concat-vec
             (ns/generate resolved-config)
             (postgres/config-objects (merge resolved-config
                                             {:postgres-size :8gb :db-name "taiga"
                                              :pv-storage-size-gb 50}))
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
             (ing/config-objects (merge
                                  {:service-name "taiga-gateway"
                                   :service-port 80
                                   :fqdns [fqdn]}
                                  resolved-config))
             (when (contains? resolved-config :restic-repository)
               [(backup/generate-config resolved-config)
                (backup/generate-cron)
                (backup/generate-backup-restore-deployment resolved-config)])
             (when (:contains? resolved-config :mon-cfg)
               (mon/config-objects (:mon-cfg resolved-config))))
            [])))))

(defn-spec auth-objects cp/map-or-seq?
  [config-select ::config-select
   config ::config
   auth ::auth]
  (let [resolved-config (merge config-defaults config)
        config-parts (if (empty? config-select)
                       ["auth" "deployment"]
                       config-select)]
    (cm/concat-vec
     (map yaml/to-string
          (if (some #(= "auth" %) config-parts)
            (cm/concat-vec
             (postgres/auth-objects resolved-config auth)
             [(taiga/generate-secret auth)]
             (when (contains? resolved-config :restic-repository)
               [(backup/generate-secret auth)])
             (when (:contains? resolved-config :mon-cfg)
               (mon/auth-objects (:mon-cfg resolved-config) (:mon-auth auth))))
            [])))))
