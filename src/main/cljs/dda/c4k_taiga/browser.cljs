(ns dda.c4k-taiga.browser
  (:require
   [clojure.string :as st]
   [clojure.tools.reader.edn :as edn]
   [dda.c4k-common.monitoring :as mon]
   [dda.c4k-taiga.core :as core]
   [dda.c4k-taiga.taiga :as taiga]   
   [dda.c4k-common.common :as cm]   
   [dda.c4k-common.browser :as br]
   ))

(defn generate-content []
  (cm/concat-vec
   [(assoc
     (br/generate-needs-validation) :content
     (cm/concat-vec
      (br/generate-group
       "domain"
       (cm/concat-vec
        (br/generate-input-field "issuer" "(Optional) Your issuer prod/staging:" "staging")
        (br/generate-input-field "mon-cluster-name" "(Optional) monitoring cluster name:" "taiga")
        (br/generate-input-field "mon-cluster-stage" "(Optional) monitoring cluster stage:" "test")
        (br/generate-input-field "mon-cloud-url" "(Optional) grafana cloud url:" "https://prometheus-prod-01-eu-west-0.grafana.net/api/prom/push")))
      (br/generate-group
       "taiga-data"
        (br/generate-text-area
         "taigas" "Contains fqdns, repo infos, an optional sha256sum-output for script execution for each taiga:"
         "{ :taigas
          [{:unique-name \"test.io\",
            :fqdns [\"test.de\" \"www.test.de\"],
            :gitea-host \"githost.de\",
            :gitea-repo \"repo\",
            :branchname \"main\",
            :sha256sum-output \"123456789ab123cd345de script-file-name.sh\"}
           {:unique-name \"example.io \",
            :fqdns [\"example.org\" \"www.example.org\"],
            :gitea-host \"githost.org\",
            :gitea-repo \"repo\",
            :branchname \"main\",
            :build-cpu-request \"1500m\",
            :build-cpu-limit \"3000m\",
            :build-memory-request \"512Mi\",
            :build-memory-limit \"1024Mi\"}] }"
         "16"))
      (br/generate-group
       "credentials"
       (br/generate-text-area
        "auth" "Your authentication data for each taiga or git repo:"
        "{:mon-auth 
          {:grafana-cloud-user \"your-user-id\"
           :grafana-cloud-password \"your-cloud-password\"}
          :auth
          [{:unique-name \"test.io\",
            :username \"someuser\",
            :authtoken \"abedjgbasdodj\"}
           {:unique-name \"example.io\",
            :username \"someuser\",
            :authtoken \"abedjgbasdodj\"}]}"
        "7"))
      [(br/generate-br)]
      (br/generate-button "generate-button" "Generate c4k yaml")))]
   (br/generate-output "c4k-taiga-output" "Your c4k deployment.yaml:" "15")))

(defn generate-content-div
  []
  {:type :element
   :tag :div
   :content
   (generate-content)})

(defn config-from-document []
  (let [issuer (br/get-content-from-element "issuer" :optional true)
        taigas (br/get-content-from-element "taigas" :deserializer edn/read-string)
        mon-cluster-name (br/get-content-from-element "mon-cluster-name" :optional true)
        mon-cluster-stage (br/get-content-from-element "mon-cluster-stage" :optional true)
        mon-cloud-url (br/get-content-from-element "mon-cloud-url" :optional true)]
    (merge
     {:taigas taigas}
     (when (not (st/blank? issuer))
       {:issuer issuer})
     (when (some? mon-cluster-name)
       {:mon-cfg {:cluster-name mon-cluster-name
                  :cluster-stage (keyword mon-cluster-stage)
                  :grafana-cloud-url mon-cloud-url}}))))

(defn validate-all! []
  (br/validate! "taigas" taiga/taigas? :deserializer edn/read-string)
  (br/validate! "issuer" ::taiga/issuer :optional true)
  (br/validate! "mon-cluster-name" ::mon/cluster-name :optional true)
  (br/validate! "mon-cluster-stage" ::mon/cluster-stage :optional true)
  (br/validate! "mon-cloud-url" ::mon/grafana-cloud-url :optional true)
  (br/validate! "auth" taiga/auth? :deserializer edn/read-string)
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
                                   (br/get-content-from-element "auth" :deserializer edn/read-string)
                                   core/config-defaults
                                   core/k8s-objects)
                                  (br/set-output!)))))
  (add-validate-listener "taigas")
  (add-validate-listener "issuer")
  (add-validate-listener "mon-cluster-name")
  (add-validate-listener "mon-cluster-stage")
  (add-validate-listener "mon-cloud-url")
  (add-validate-listener "auth"))
