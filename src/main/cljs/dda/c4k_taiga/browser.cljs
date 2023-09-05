(ns dda.c4k-taiga.browser
  (:require
   [clojure.string :as st]
   [clojure.tools.reader.edn :as edn]
   [dda.c4k-common.monitoring :as mon]
   [dda.c4k-taiga.core :as core]
   [dda.c4k-taiga.taiga :as taiga]
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.predicate :as cp]
   [dda.c4k-common.browser :as br]
   [dda.c4k-common.postgres :as postgres]))

(defn generate-content []
  (cm/concat-vec
   [(assoc
     (br/generate-needs-validation) :content
     (cm/concat-vec
      (br/generate-group
       "domain"
       (cm/concat-vec
        (br/generate-input-field "fqdn" "The fully qualified domain name of your Taiga Instance:" "taiga.example.com")
        (br/generate-input-field "issuer" "(Optional) Your issuer prod/staging:" "staging")
        (br/generate-input-field "mon-cluster-name" "(Optional) monitoring cluster name:" "taiga")
        (br/generate-input-field "mon-cluster-stage" "(Optional) monitoring cluster stage:" "test")
        (br/generate-input-field "mon-cloud-url" "(Optional) grafana cloud url:" "https://prometheus-prod-01-eu-west-0.grafana.net/api/prom/push")))
      (br/generate-group
       "options"
       (cm/concat-vec
        (br/generate-input-field "public-register-enabled"      "(Optional) Allow public registration?" "false")
        (br/generate-input-field "enable-telemetry"             "(Optional) Allow anonymous collection of usage data?" "false")
        (br/generate-input-field "pv-storage-size-gb"           "(Optional) The volume size of your postgres DB:" "5")
        (br/generate-input-field "storage-class-name"           "(Optional) Name of storage class:" "local-path")
        (br/generate-input-field "storage-media-size"           "(Optional) The size of your media storage:" "5")
        (br/generate-input-field "storage-static-size"          "(Optional) The size of your static data storage:" "5")
        (br/generate-input-field "storage-async-rabbitmq-size"  "(Optional) The size of your rabbitmq async storage:" "5")
        (br/generate-input-field "storage-events-rabbitmq-size" "(Optional) The size of your rabbitmq events storage:" "5")))
      (br/generate-group
       "credentials"
       (cm/concat-vec
        (br/generate-input-field "postgres-db-user"          "Your postgres user:"                         "postgres")
        (br/generate-input-field "postgres-db-password"      "Your postgres password:"                     "change-me")
        (br/generate-input-field "mailer-user"               "Allow taiga access to a mail account:"       "mail[at]example.com")
        (br/generate-input-field "mailer-pw"                 "Allow taiga access to a mail account:"       "change-me")
        (br/generate-input-field "django-superuser-username" "The superusers username:"                    "admin")
        (br/generate-input-field "django-superuser-password" "The superusers password:"                    "change-me")
        (br/generate-input-field "django-superuser-email"    "The superusers email:"                       "mail[at]example.com")
        (br/generate-input-field "rabbitmq-user"             "User for rabbitmq:"                          "user")
        (br/generate-input-field "rabbitmq-pw"               "Password for the rabbitmq user:"             "change-me")
        (br/generate-input-field "rabbitmq-erlang-cookie"    "Random hash shared among all rabbitmq pods:" "change-me")
        (br/generate-input-field "taiga-secret-key"          "Random key shared among all taiga pods:"     "change-me")
        (br/generate-input-field "grafana-cloud-user"        "Your grafana user name:"                     "user")
        (br/generate-input-field "grafana-cloud-password"    "Your grafana password:"                      "change-me")))
      [(br/generate-br)]
      (br/generate-button "generate-button" "Generate c4k yaml")))]
   (br/generate-output "c4k-taiga-output" "Your c4k deployment.yaml:" "15")))

(defn generate-content-div
  []
  {:type :element
   :tag :div
   :content
   (generate-content)})

(defn auth-from-document []
  (let [postgres-db-user          (br/get-content-from-element "postgres-db-user"          )
        postgres-db-password      (br/get-content-from-element "postgres-db-password"      )
        mailer-user               (br/get-content-from-element "mailer-user"               )
        mailer-pw                 (br/get-content-from-element "mailer-pw"                 )
        django-superuser-username (br/get-content-from-element "django-superuser-username" )
        django-superuser-password (br/get-content-from-element "django-superuser-password" )
        django-superuser-email    (br/get-content-from-element "django-superuser-email"    )
        rabbitmq-user             (br/get-content-from-element "rabbitmq-user"             )
        rabbitmq-pw               (br/get-content-from-element "rabbitmq-pw"               )
        rabbitmq-erlang-cookie    (br/get-content-from-element "rabbitmq-erlang-cookie"    )
        taiga-secret-key          (br/get-content-from-element "taiga-secret-key"          )
        grafana-cloud-user        (br/get-content-from-element "grafana-cloud-user"        :optional true)
        grafana-cloud-password    (br/get-content-from-element "grafana-cloud-password"    :optional true)]
    (merge
     {:postgres-db-user           postgres-db-user}
     {:postgres-db-password       postgres-db-password}
     {:mailer-user                mailer-user}
     {:mailer-pw                  mailer-pw}
     {:django-superuser-username  django-superuser-username}
     {:django-superuser-password  django-superuser-password}
     {:django-superuser-email     django-superuser-email}
     {:rabbitmq-user              rabbitmq-user}
     {:rabbitmq-pw                rabbitmq-pw}
     {:rabbitmq-erlang-cookie     rabbitmq-erlang-cookie}
     {:taiga-secret-key           taiga-secret-key}
     (when (some? grafana-cloud-user)
       {:mon-auth {:grafana-cloud-user     grafana-cloud-user
                   :grafana-cloud-password grafana-cloud-password}}))))

(defn config-from-document []
  (let [issuer                       (br/get-content-from-element "issuer"                       :optional true)
        fqdn                         (br/get-content-from-element "fqdn"                         :deserializer edn/read-string)
        public-register-enabled      (br/get-content-from-element "public-register-enabled"      :deserializer edn/read-string)
        enable-telemetry             (br/get-content-from-element "enable-telemetry"             :deserializer edn/read-string)
        pv-storage-size-gb           (br/get-content-from-element "pv-storage-size-gb"           :deserializer edn/read-string)
        storage-class-name           (br/get-content-from-element "storage-class-name"           :deserializer edn/read-string)
        storage-media-size           (br/get-content-from-element "storage-media-size"           :deserializer edn/read-string)
        storage-static-size          (br/get-content-from-element "storage-static-size"          :deserializer edn/read-string)
        storage-async-rabbitmq-size  (br/get-content-from-element "storage-async-rabbitmq-size"  :deserializer edn/read-string)
        storage-events-rabbitmq-size (br/get-content-from-element "storage-events-rabbitmq-size" :deserializer edn/read-string)
        mon-cluster-name             (br/get-content-from-element "mon-cluster-name"             :optional true)
        mon-cluster-stage            (br/get-content-from-element "mon-cluster-stage"            :optional true)
        mon-cloud-url                (br/get-content-from-element "mon-cloud-url"                :optional true)]
    (merge
     {:fqdn fqdn}
     {:public-register-enabled public-register-enabled}
     {:enable-telemetry enable-telemetry}
     {:pv-storage-size-gb pv-storage-size-gb}
     {:storage-class-name storage-class-name}
     {:storage-media-size storage-media-size}
     {:storage-static-size storage-static-size}
     {:storage-async-rabbitmq-size storage-async-rabbitmq-size}
     {:storage-events-rabbitmq-size storage-events-rabbitmq-size}
     (when (not (st/blank? issuer))
       {:issuer issuer})
     (when (some? mon-cluster-name)
       {:mon-cfg {:cluster-name mon-cluster-name
                  :cluster-stage (keyword mon-cluster-stage)
                  :grafana-cloud-url mon-cloud-url}}))))

(defn validate-all! []
  (br/validate! "fqdn"                         ::taiga/fqdn                         )
  (br/validate! "issuer"                       ::taiga/issuer                       :optional true)
  (br/validate! "public-register-enabled"      ::taiga/public-register-enabled      :optional true)
  (br/validate! "enable-telemetry"             ::taiga/enable-telemetry             :optional true)
  (br/validate! "pv-storage-size-gb"           ::postgres/pv-storage-size-gb        :optional true)
  (br/validate! "storage-class-name"           ::taiga/storage-class-name           :optional true)
  (br/validate! "storage-media-size"           ::taiga/storage-media-size           :optional true)
  (br/validate! "storage-static-size"          ::taiga/storage-static-size          :optional true)
  (br/validate! "storage-async-rabbitmq-size"  ::taiga/storage-async-rabbitmq-size  :optional true)
  (br/validate! "storage-events-rabbitmq-size" ::taiga/storage-events-rabbitmq-size :optional true)
  (br/validate! "mon-cluster-name"             ::mon/cluster-name                   :optional true)
  (br/validate! "mon-cluster-stage"            ::mon/cluster-stage                  :optional true)
  (br/validate! "mon-cloud-url"                ::mon/grafana-cloud-url              :optional true)
  (br/validate! "postgres-db-user"             ::postgres/postgres-db-user          )
  (br/validate! "postgres-db-password"         ::postgres/postgres-db-password      )
  (br/validate! "mailer-user"                  ::taiga/mailer-user                  )
  (br/validate! "mailer-pw"                    ::taiga/mailer-pw                    )
  (br/validate! "django-superuser-username"    ::taiga/django-superuser-username    )
  (br/validate! "django-superuser-password"    ::taiga/django-superuser-password    )
  (br/validate! "django-superuser-email"       ::taiga/django-superuser-email       )
  (br/validate! "rabbitmq-user"                ::taiga/rabbitmq-user                )
  (br/validate! "rabbitmq-pw"                  ::taiga/rabbitmq-pw                  )
  (br/validate! "rabbitmq-erlang-cookie"       ::taiga/rabbitmq-erlang-cookie       )
  (br/validate! "taiga-secret-key"             ::taiga/taiga-secret-key             )
  (br/validate! "grafana-cloud-user"           ::mon/grafana-cloud-user             )
  (br/validate! "grafana-cloud-password"       ::mon/grafana-cloud-password         )
  (br/set-form-validated!))

(defn add-validate-listener [name]
  (-> (br/get-element-by-id name)
      (.addEventListener "blur" #(do (validate-all!)))))

(defn init []
  (br/append-hickory (generate-content-div))
  (-> js/document
      (.getElementById "generate-button")
      (.addEventListener "click"
                         #(do (validate-all!)
                              (-> (cm/generate-common
                                   (config-from-document)
                                   (auth-from-document)
                                   core/config-defaults
                                   core/k8s-objects)
                                  (br/set-output!)))))
  (add-validate-listener "fqdn")
  (add-validate-listener "issuer")
  (add-validate-listener "public-register-enabled")
  (add-validate-listener "enable-telemetry")
  (add-validate-listener "pv-storage-size-gb")
  (add-validate-listener "storage-class-name")
  (add-validate-listener "storage-media-size")
  (add-validate-listener "storage-static-size")
  (add-validate-listener "storage-async-rabbitmq-size")
  (add-validate-listener "storage-events-rabbitmq-size")
  (add-validate-listener "mon-cluster-name")
  (add-validate-listener "mon-cluster-stage")
  (add-validate-listener "mon-cloud-url")
  (add-validate-listener "postgres-db-user")
  (add-validate-listener "postgres-db-password")
  (add-validate-listener "mailer-user")
  (add-validate-listener "mailer-pw")
  (add-validate-listener "django-superuser-username")
  (add-validate-listener "django-superuser-password")
  (add-validate-listener "django-superuser-email")
  (add-validate-listener "rabbitmq-user")
  (add-validate-listener "rabbitmq-pw")
  (add-validate-listener "rabbitmq-erlang-cookie")
  (add-validate-listener "taiga-secret-key")
  (add-validate-listener "grafana-cloud-user")
  (add-validate-listener "grafana-cloud-password"))
