(ns dda.c4k-taiga.browser
  (:require
   [clojure.tools.reader.edn :as edn]
   [dda.c4k-taiga.core :as core]
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.browser :as br]))

(defn generate-content []
  (cm/concat-vec
   [(assoc
     (br/generate-needs-validation) :content
     (cm/concat-vec
      (br/generate-group
       "config"
       (br/generate-text-area "config" "Your config.edn:" "{:fqdn \"cloud.your.domain\"
          :issuer \"staging\"
          :restic-repository \"s3://yourbucket/your-repo\"
          :mon-cfg {:cluster-name \"cloud\"
                    :cluster-stage \"test\"
                    :cloud-url \"https://prometheus-prod-01-eu-west-0.grafana.net/api/prom/push\"}}"
                              "5"))
      (br/generate-group
       "auth"
       (br/generate-text-area "auth" "Your auth.edn:" "{:postgres-db-user \"taiga\"
                :postgres-db-password \"db-password\"
                :mailer-user \"mail[at]example.com\"
                :mailer-pw \"change-me\"
                :django-superuser-username \"admin\"
                :django-superuser-password \"change-me\"
                :django-superuser-email \"mail[at]example.com\"
                :rabbitmq-user \"user\"
                :rabbitmq-pw \"change-me\"
                :rabbitmq-erlang-cookie \"change-me\"
                :taiga-secret-key \"change-me\"
                :aws-access-key-id \"aws-id\"
                :aws-secret-access-key \"aws-secret\"
                :restic-password \"restic-password\"}
                :mon-auth {:grafana-cloud-user \"your-user-id\"
                           :grafana-cloud-password \"your-cloud-password\"}"
                              "5"))
      [(br/generate-br)]
      (br/generate-button "generate-button" "Generate c4k yaml")))]
   (br/generate-output "c4k-taiga-output" "Your c4k deployment.yaml:" "15")))

(defn generate-content-div
  []
  {:type :element
   :tag :div
   :content
   (generate-content)})

(defn validate-all! []
  (br/validate! "config" core/config? :deserializer edn/read-string)
  (br/validate! "auth" core/auth? :deserializer edn/read-string)
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
                              (-> (cm/generate-cm
                                   (br/get-content-from-element "config" :deserializer edn/read-string)
                                    (br/get-content-from-element "auth" :deserializer edn/read-string)
                                    core/config-defaults
                                    core/config-objects
                                    core/auth-objects
                                    false
                                    false)
                                  (br/set-output!)))))
  (add-validate-listener "config")
  (add-validate-listener "authr"))
