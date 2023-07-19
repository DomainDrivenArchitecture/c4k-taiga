(ns dda.c4k-website.website
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
   [dda.c4k-common.predicate :as pred]
   [dda.c4k-common.ingress :as ing]
   [clojure.string :as str]))

(defn fqdn-list?
  [input]
  (every? true? (map pred/fqdn-string? input)))

(s/def ::unique-name string?)
(s/def ::sha256sum-output string?)
(s/def ::issuer pred/letsencrypt-issuer?)
(s/def ::volume-size pred/integer-string?)
(s/def ::authtoken pred/bash-env-string?)
(s/def ::fqdns (s/coll-of pred/fqdn-string?))
(s/def ::gitea-host pred/fqdn-string?)
(s/def ::gitea-repo string?)
(s/def ::branchname string?)
(s/def ::username string?)
(s/def ::build-cpu-request string?)
(s/def ::build-memory-request string?)
(s/def ::build-cpu-limit string?)
(s/def ::build-memory-limit string?)

(def websiteconfig? (s/keys :req-un [::unique-name
                                     ::fqdns
                                     ::gitea-host
                                     ::gitea-repo
                                     ::branchname]
                            :opt-un [::issuer
                                     ::volume-size
                                     ::sha256sum-output
                                     ::build-cpu-request
                                     ::build-cpu-limit
                                     ::build-memory-request
                                     ::build-memory-limit]))

(def websiteauth? (s/keys :req-un [::unique-name ::username ::authtoken]))

(s/def ::websites (s/coll-of websiteconfig?))

(s/def ::auth (s/coll-of websiteauth?))

(def websites? (s/keys :req-un [::websites]))

(def auth? (s/keys :req-un [::auth]))

(defn-spec get-hash-from-sha256sum-output string?
  [sha256sum-output string?]
  (if (nil? sha256sum-output)
    nil
    (first (str/split sha256sum-output #"\ +"))))

(defn-spec get-file-name-from-sha256sum-output string?
  [sha256sum-output string?]
  (if (nil? sha256sum-output)
    nil
    (second (str/split (str/trim sha256sum-output) #"\ +"))))

(defn-spec replace-dots-by-minus string?
  [fqdn pred/fqdn-string?]
  (str/replace fqdn #"\." "-"))

(defn-spec generate-app-name string?
  [unique-name pred/fqdn-string?]
  (str (replace-dots-by-minus unique-name) "-website"))

(defn-spec generate-service-name string?
  [unique-name pred/fqdn-string?]
  (str (replace-dots-by-minus unique-name) "-service"))

(defn-spec generate-cert-name string?
  [unique-name pred/fqdn-string?]
  (str (replace-dots-by-minus unique-name) "-cert"))

(defn-spec generate-ingress-name string?
  [unique-name pred/fqdn-string?]
  (str (replace-dots-by-minus unique-name) "-ingress"))

; https://your.gitea.host/api/v1/repos/<owner>/<repo>/archive/<branch>.zip
(defn-spec generate-gitrepourl string?
  [host pred/fqdn-string?
   repo string?
   user string?
   branch string?]
  (str "https://" host "/api/v1/repos/" user "/" repo "/archive/" branch ".zip"))

; https://your.gitea.host/api/v1/repos/<owner>/<repo>/git/commits/HEAD
(defn-spec generate-gitcommiturl string?
  [host pred/fqdn-string?
   repo string?
   user string?]
  (str "https://" host "/api/v1/repos/" user "/" repo "/git/" "commits/" "HEAD"))

(defn-spec replace-all-matching-substrings-beginning-with pred/map-or-seq?
  [col pred/map-or-seq?
   value-to-partly-match string?
   value-to-inplace string?]
  (clojure.walk/postwalk #(if (and (= (type value-to-partly-match) (type %))
                                   (re-matches (re-pattern (str value-to-partly-match ".*")) %))
                            (str/replace % value-to-partly-match value-to-inplace) %)
                         col))

(defn-spec replace-common-data pred/map-or-seq?
  [resource-file string?
   config websiteconfig?]
  (let [{:keys [unique-name]} config]
    (->
     (yaml/load-as-edn resource-file)
     (assoc-in [:metadata :labels :app.kubernetes.part-of] (generate-app-name unique-name))
     (replace-all-matching-substrings-beginning-with "NAME" (replace-dots-by-minus unique-name)))))

(defn-spec replace-build-data pred/map-or-seq?
  [resource-file string?
   config websiteconfig?]
  (let [{:keys [sha256sum-output build-cpu-request build-cpu-limit build-memory-request build-memory-limit]
         :or {build-cpu-request "500m" build-cpu-limit "1700m" build-memory-request "256Mi" build-memory-limit "512Mi"}} config]
    (->
     (replace-common-data resource-file config)
     (cm/replace-all-matching-values-by-new-value "CHECK_SUM" (get-hash-from-sha256sum-output sha256sum-output))
     (cm/replace-all-matching-values-by-new-value "SCRIPT_FILE" (get-file-name-from-sha256sum-output sha256sum-output))
     (cm/replace-all-matching-values-by-new-value "BUILD_CPU_REQUEST" build-cpu-request)
     (cm/replace-all-matching-values-by-new-value "BUILD_CPU_LIMIT" build-cpu-limit)
     (cm/replace-all-matching-values-by-new-value "BUILD_MEMORY_REQUEST" build-memory-request)
     (cm/replace-all-matching-values-by-new-value "BUILD_MEMORY_LIMIT" build-memory-limit))))

#?(:cljs
   (defmethod yaml/load-resource :website [resource-name]
     (case resource-name
       "website/nginx-configmap.yaml" (rc/inline "website/nginx-configmap.yaml")
       "website/nginx-deployment.yaml" (rc/inline "website/nginx-deployment.yaml")
       "website/nginx-service.yaml" (rc/inline "website/nginx-service.yaml")
       "website/website-build-cron.yaml" (rc/inline "website/website-build-cron.yaml")
       "website/website-build-secret.yaml" (rc/inline "website/website-build-secret.yaml")
       "website/website-content-volume.yaml" (rc/inline "website/website-content-volume.yaml")
       "website/hashfile-volume.yaml" (rc/inline "website/hashfile-volume.yaml")
       (throw (js/Error. "Undefined Resource!")))))

(defn-spec generate-nginx-deployment pred/map-or-seq?
  [config websiteconfig?]
  (replace-build-data "website/nginx-deployment.yaml" config))

(defn-spec generate-nginx-configmap pred/map-or-seq?
  [config websiteconfig?]
  (let [{:keys [fqdns]} config]
    (->
     (replace-common-data "website/nginx-configmap.yaml" config)     
     (#(assoc-in %
                 [:data :website.conf]
                 (str/replace
                  (-> % :data :website.conf) #"FQDN" (str (str/join " " fqdns) ";")))))))

(defn-spec generate-nginx-service pred/map-or-seq?
  [config websiteconfig?]
  (replace-common-data "website/nginx-service.yaml" config))

(defn-spec generate-website-content-volume pred/map-or-seq?
  [config websiteconfig?]
  (let [{:keys [volume-size]
         :or {volume-size "3"}} config]
    (->
     (replace-common-data "website/website-content-volume.yaml" config)     
     (cm/replace-all-matching-values-by-new-value "WEBSITESTORAGESIZE" (str volume-size "Gi")))))

(defn-spec generate-hashfile-volume pred/map-or-seq?
  [config websiteconfig?]
  (replace-common-data "website/hashfile-volume.yaml" config))


(defn-spec generate-website-ingress pred/map-or-seq?
  [config websiteconfig?]
  (let [{:keys [unique-name fqdns]} config]
    (ing/generate-ingress {:fqdns fqdns
                           :app-name (generate-app-name unique-name)
                           :ingress-name (generate-ingress-name unique-name)
                           :service-name (generate-service-name unique-name)
                           :service-port 80})))

(defn-spec generate-website-certificate pred/map-or-seq?
  [config websiteconfig?]
  (let [{:keys [unique-name issuer fqdns]
         :or {issuer "staging"}} config]
    (ing/generate-certificate {:fqdns fqdns
                               :app-name (generate-app-name unique-name)
                               :cert-name (generate-cert-name unique-name)
                               :issuer issuer})))

(defn-spec generate-website-build-cron pred/map-or-seq?
  [config websiteconfig?]
  (replace-build-data "website/website-build-cron.yaml" config))

(defn-spec generate-website-build-secret pred/map-or-seq?
  [config websiteconfig?
   auth websiteauth?]
  (let [{:keys [gitea-host
                gitea-repo
                branchname]} config
        {:keys [authtoken
                username]} auth]
    (->
     (replace-common-data "website/website-build-secret.yaml" config)
     (cm/replace-all-matching-values-by-new-value "TOKEN" (b64/encode authtoken))
     (cm/replace-all-matching-values-by-new-value "REPOURL" (b64/encode
                                                             (generate-gitrepourl
                                                              gitea-host
                                                              gitea-repo
                                                              username
                                                              branchname)))
     (cm/replace-all-matching-values-by-new-value "COMMITURL" (b64/encode
                                                               (generate-gitcommiturl
                                                                gitea-host
                                                                gitea-repo
                                                                username))))))

