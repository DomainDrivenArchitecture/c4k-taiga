(ns dda.c4k-taiga.taiga
  (:require
   [clojure.spec.alpha :as s]
   #?(:clj [orchestra.core :refer [defn-spec]]
      :cljs [orchestra.core :refer-macros [defn-spec]])
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-common.ingress :as ing]
   [dda.c4k-common.base64 :as b64]
   [dda.c4k-common.predicate :as p]
   [dda.c4k-common.common :as cm]
   #?(:cljs [dda.c4k-common.macros :refer-macros [inline-resources]])))

(s/def ::taiga-secret-key p/bash-env-string?)
(s/def ::mailer-user string?)
(s/def ::mailer-pw string?)
(s/def ::django-superuser-username string?)
(s/def ::django-superuser-password string?)
(s/def ::django-superuser-email string?)
(s/def ::rabbitmq-user string?)
(s/def ::rabbitmq-pw string?)
(s/def ::rabbitmq-erlang-cookie string?)

(s/def ::issuer p/letsencrypt-issuer?)
(s/def ::fqdn p/fqdn-string?)
(s/def ::public-register-enabled string?)
(s/def ::enable-telemetry string?)
(s/def ::storage-class-name string?)
(s/def ::storage-media-size pos?)
(s/def ::storage-static-size pos?)
(s/def ::storage-async-rabbitmq-size pos?)
(s/def ::storage-events-rabbitmq-size pos?)

(s/def ::auth (s/keys :req-un [::taiga-secret-key
                               ::mailer-pw
                               ::mailer-user
                               ::django-superuser-email
                               ::django-superuser-password
                               ::django-superuser-username
                               ::rabbitmq-erlang-cookie
                               ::rabbitmq-pw
                               ::rabbitmq-user]
                      :opt-un [::mon-auth]))

(s/def ::config (s/keys :req-un [::fqdn]
                        :opt-un [::issuer
                                 ::storage-class-name
                                 ::storage-media-size
                                 ::storage-static-size
                                 ::storage-async-rabbitmq-size
                                 ::storage-events-rabbitmq-size
                                 ::public-register-enabled
                                 ::enable-telemetry
                                 ::mon-cfg]))

#?(:cljs
   (defmethod yaml/load-resource :taiga [resource-name]
     (get (inline-resources "taiga") resource-name)))

(defn-spec generate-ingress-and-cert p/map-or-seq?
  [config ::config]
  (let [{:keys [fqdn]} config]
    (ing/generate-ingress-and-cert
     (merge
      {:service-name "taiga-gateway"
       :service-port 80
       :fqdns [fqdn]}
      config))))

(defn-spec generate-async-deployment p/map-or-seq? []
  (yaml/load-as-edn "taiga/async-deployment.yaml"))

(defn-spec generate-async-service p/map-or-seq? []
  (yaml/load-as-edn "taiga/async-service.yaml"))

(defn-spec generate-async-rabbitmq-deployment p/map-or-seq? []
  (yaml/load-as-edn "taiga/async-rabbitmq-deployment.yaml"))

(defn-spec generate-events-rabbitmq-service p/map-or-seq? []
  (yaml/load-as-edn "taiga/events-rabbitmq-service.yaml"))

(defn-spec generate-async-rabbitmq-service p/map-or-seq? []
  (yaml/load-as-edn "taiga/async-rabbitmq-service.yaml"))

(defn-spec generate-back-deployment p/map-or-seq? []
  (yaml/load-as-edn "taiga/back-deployment.yaml"))

(defn-spec generate-back-service p/map-or-seq? []
  (yaml/load-as-edn "taiga/back-service.yaml"))

(defn-spec generate-events-rabbitmq-deployment p/map-or-seq? []
  (yaml/load-as-edn "taiga/events-rabbitmq-deployment.yaml"))

(defn-spec generate-events-deployment p/map-or-seq? []
  (yaml/load-as-edn "taiga/events-deployment.yaml"))

(defn-spec generate-events-service p/map-or-seq? []
  (yaml/load-as-edn "taiga/events-service.yaml"))

(defn-spec generate-front-deployment p/map-or-seq? []
  (yaml/load-as-edn "taiga/front-deployment.yaml"))

(defn-spec generate-front-service p/map-or-seq? []
  (yaml/load-as-edn "taiga/front-service.yaml"))

(defn-spec generate-gateway-configmap p/map-or-seq? []
  (yaml/load-as-edn "taiga/gateway-configmap.yaml"))

(defn-spec generate-gateway-deployment p/map-or-seq? []
  (yaml/load-as-edn "taiga/gateway-deployment.yaml"))

(defn-spec generate-gateway-service p/map-or-seq? []
  (yaml/load-as-edn "taiga/gateway-service.yaml"))

(defn-spec generate-protected-deployment p/map-or-seq? []
  (yaml/load-as-edn "taiga/protected-deployment.yaml"))

(defn-spec generate-protected-service p/map-or-seq? []
  (yaml/load-as-edn "taiga/protected-service.yaml"))

(defn-spec generate-configmap p/map-or-seq?
  [config ::config]
  (let [{:keys [fqdn enable-telemetry public-register-enabled]} config]
    (-> (yaml/load-as-edn "taiga/configmap.yaml")
        (cm/replace-key-value :TAIGA_SITES_DOMAIN fqdn)
        (cm/replace-key-value :TAIGA_URL (str "https://" fqdn))
        (cm/replace-key-value :TAIGA_WEBSOCKETS_URL (str "wss://" fqdn))
        (cm/replace-key-value :ENABLE_TELEMETRY enable-telemetry)
        (cm/replace-key-value :PUBLIC_REGISTER_ENABLED public-register-enabled))))

(defn-spec generate-pvc-taiga-media-data p/map-or-seq?
  [config ::config]
  (let [{:keys [storage-class-name storage-media-size]} config]
    (->
     (yaml/load-as-edn "taiga/pvc-taiga-media-data.yaml")
     (assoc-in [:spec :storageClassName] storage-class-name)
     (assoc-in [:spec :resources :requests :storage] (str storage-media-size "Gi")))))

(defn-spec generate-pvc-taiga-static-data p/map-or-seq?
  [config ::config]
  (let [{:keys [storage-class-name storage-static-size]} config]
    (->
     (yaml/load-as-edn "taiga/pvc-taiga-static-data.yaml")
     (assoc-in [:spec :storageClassName] storage-class-name)
     (assoc-in [:spec :resources :requests :storage] (str storage-static-size "Gi")))))

(defn-spec generate-secret p/map-or-seq?
  [auth ::auth]
  (let [{:keys [taiga-secret-key
                mailer-user mailer-pw
                rabbitmq-user rabbitmq-pw rabbitmq-erlang-cookie
                django-superuser-username django-superuser-password django-superuser-email]} auth]
    (->
     (yaml/load-as-edn "taiga/secret.yaml")
     (cm/replace-key-value :TAIGA_SECRET_KEY (b64/encode taiga-secret-key))
     (cm/replace-key-value :EMAIL_HOST_USER (b64/encode mailer-user))
     (cm/replace-key-value :EMAIL_HOST_PASSWORD (b64/encode mailer-pw))
     (cm/replace-key-value :RABBITMQ_USER (b64/encode rabbitmq-user))
     (cm/replace-key-value :RABBITMQ_PASS (b64/encode rabbitmq-pw))
     (cm/replace-key-value :RABBITMQ_ERLANG_COOKIE (b64/encode rabbitmq-erlang-cookie))
     (cm/replace-key-value :DJANGO_SUPERUSER_USERNAME (b64/encode django-superuser-username))
     (cm/replace-key-value :DJANGO_SUPERUSER_PASSWORD (b64/encode django-superuser-password))
     (cm/replace-key-value :DJANGO_SUPERUSER_EMAIL (b64/encode django-superuser-email)))))

(defn-spec generate-rabbitmq-pvc-async p/map-or-seq?
  [config ::config]
  (let [{:keys [storage-class-name storage-async-rabbitmq-size]} config]
    (->
     (yaml/load-as-edn "taiga/rabbitmq-pvc-async.yaml")
     (assoc-in [:spec :storageClassName] storage-class-name)
     (assoc-in [:spec :resources :requests :storage] (str storage-async-rabbitmq-size "Gi")))))

(defn-spec generate-rabbitmq-pvc-events p/map-or-seq?
  [config ::config]
  (let [{:keys [storage-class-name storage-events-rabbitmq-size]} config]
    (->
     (yaml/load-as-edn "taiga/rabbitmq-pvc-events.yaml")
     (assoc-in [:spec :storageClassName] storage-class-name)
     (assoc-in [:spec :resources :requests :storage] (str storage-events-rabbitmq-size "Gi")))))

