(ns dda.c4k-taiga.taiga-test
  (:require
   #?(:cljs [shadow.resource :as rc])
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [clojure.spec.alpha :as s]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-taiga.taiga :as cut]))

#?(:cljs
   (defmethod yaml/load-resource :taiga-test [resource-name]
     (case resource-name
       "taiga-test/valid-config.yaml" (rc/inline "taiga-test/valid-config.yaml")
       "taiga-test/valid-auth.yaml" (rc/inline "taiga-test/valid-auth.yaml")
       (throw (js/Error. "Undefined Resource!")))))

(deftest should-generate-configmap
  (is (= {:apiVersion "v1",
          :kind "ConfigMap",
          :metadata {:name "taiga-configmap", :namespace "default"},
          :data
          {:CELERY_ENABLED "false",
           :ENABLE_TELEMETRY "false",
           :TAIGA_SITES_SCHEME "https",
           :TAIGA_SITES_DOMAIN "taiga.test.meissa.de",
           :TAIGA_SUBPATH "",
           :TAIGA_URL "https://taiga.test.meissa.de",
           :TAIGA_WEBSOCKETS_URL "wss://taiga.test.meissa.de",
           :PUBLIC_REGISTER_ENABLED "false",
           :ENABLE_GITHUB_IMPORTER "false",
           :ENABLE_JIRA_IMPORTER "false",
           :ENABLE_TRELLO_IMPORTER "false",
           :RABBITMQ_DEFAULT_VHOST "taiga",
           :SESSION_COOKIE_SECURE "false",
           :CSRF_COOKIE_SECURE "false"}}
         (cut/generate-configmap (yaml/load-as-edn "taiga-test/valid-config.yaml")))))

(deftest should-generate-pvc-taiga-media-data
  (is (= {:apiVersion "v1",
          :kind "PersistentVolumeClaim",
          :metadata
          {:name "taiga-media-data",
           :namespace "default",
           :labels {:app "taiga", :app.kubernetes.part-of "taiga"}},
          :spec
          {:storageClassName "local-path",
           :accessModes ["ReadWriteOnce"],
           :resources {:requests {:storage "2Gi"}}}}
         (cut/generate-pvc-taiga-media-data (yaml/load-as-edn "taiga-test/valid-config.yaml")))))

(deftest should-generate-pvc-taiga-static-data
  (is (= {:apiVersion "v1",
          :kind "PersistentVolumeClaim",
          :metadata
          {:name "taiga-static-data",
           :namespace "default",
           :labels {:app "taiga", :app.kubernetes.part-of "taiga"}},
          :spec
          {:storageClassName "local-path",
           :accessModes ["ReadWriteOnce"],
           :resources {:requests {:storage "3Gi"}}}}
         (cut/generate-pvc-taiga-static-data (yaml/load-as-edn "taiga-test/valid-config.yaml")))))

(deftest should-generate-rabbitmq-pvc-async
  (is (= {:apiVersion "v1",
          :kind "PersistentVolumeClaim",
          :metadata
          {:name "taiga-async-rabbitmq-data",
           :namespace "default",
           :labels {:app "taiga", :app.kubernetes.part-of "taiga"}},
          :spec
          {:storageClassName "local-path",
           :accessModes ["ReadWriteOnce"],
           :resources {:requests {:storage "4Gi"}}}}
         (cut/generate-rabbitmq-pvc-async(yaml/load-as-edn "taiga-test/valid-config.yaml")))))

(deftest should-generate-rabbitmq-pvc-events
  (is (= {:apiVersion "v1",
          :kind "PersistentVolumeClaim",
          :metadata
          {:name "taiga-events-rabbitmq-data",
           :namespace "default",
           :labels {:app "taiga", :app.kubernetes.part-of "taiga"}},
          :spec
          {:storageClassName "local-path",
           :accessModes ["ReadWriteOnce"],
           :resources {:requests {:storage "5Gi"}}}}
         (cut/generate-rabbitmq-pvc-events (yaml/load-as-edn "taiga-test/valid-config.yaml")))))

(deftest should-generate-secret
  (is (= {:apiVersion "v1",
          :kind "Secret",
          :metadata
          {:name "taiga-secret", :labels {:app.kubernetes.part-of "taiga"}},
          :data
          {:TAIGA_SECRET_KEY "c29tZS1rZXk=",
           :EMAIL_HOST_USER "bWFpbGVyLXVzZXI=",
           :EMAIL_HOST_PASSWORD "bWFpbGVyLXB3",
           :RABBITMQ_USER "cmFiYml0LXVzZXI=",
           :RABBITMQ_PASS "cmFiYml0LXB3",
           :RABBITMQ_ERLANG_COOKIE "cmFiYml0LWVybGFuZw==",
           :DJANGO_SUPERUSER_USERNAME "dGFpZ2EtYWRtaW4=",
           :DJANGO_SUPERUSER_PASSWORD "c3VwZXItcGFzc3dvcmQ=",
           :DJANGO_SUPERUSER_EMAIL "c29tZUBleGFtcGxlLmNvbQ=="}}
         (cut/generate-secret (yaml/load-as-edn "taiga-test/valid-auth.yaml")))))