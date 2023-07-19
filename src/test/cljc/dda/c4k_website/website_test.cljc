(ns dda.c4k-website.website-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [clojure.spec.test.alpha :as st]
   [dda.c4k-common.test-helper :as th]
   [dda.c4k-common.base64 :as b64]   
   [dda.c4k-website.website :as cut]   
   [clojure.spec.alpha :as s]))

(st/instrument `cut/generate-nginx-configmap)
(st/instrument `cut/generate-nginx-deployment)
(st/instrument `cut/generate-nginx-service)
(st/instrument `cut/generate-website-content-volume)
(st/instrument `cut/generate-hashfile-volume)
(st/instrument `cut/generate-website-ingress)
(st/instrument `cut/generate-website-certificate)
(st/instrument `cut/generate-website-build-cron)
(st/instrument `cut/generate-website-build-secret)

(deftest should-generate-nginx-configmap-website
  (is (= "server {\n  listen 80 default_server;\n  listen [::]:80 default_server;\n  server_name test.de www.test.de test-it.de www.test-it.de;\n  add_header Strict-Transport-Security 'max-age=31536000; includeSubDomains; preload';      \n  add_header X-Frame-Options \"SAMEORIGIN\";\n  add_header X-Content-Type-Options nosniff;\n  add_header Referrer-Policy \"strict-origin\";\n  # add_header Permissions-Policy \"permissions here\";\n  root /var/www/html/website/;\n  index index.html;\n  location / {\n    try_files $uri $uri/ /index.html =404;\n  }\n}\n"
         (:website.conf (:data (cut/generate-nginx-configmap {:unique-name "test.io",
                                                              :gitea-host "gitea.evilorg",
                                                              :gitea-repo "none",
                                                              :branchname "mablain",
                                                              :fqdns ["test.de" "www.test.de" "test-it.de" "www.test-it.de"]})))))
  (is (= "types {\n  text/html                             html htm shtml;\n  text/css                              css;\n  text/xml                              xml rss;\n  image/gif                             gif;\n  image/jpeg                            jpeg jpg;\n  application/x-javascript              js;\n  text/plain                            txt;\n  text/x-component                      htc;\n  text/mathml                           mml;\n  image/svg+xml                         svg svgz;\n  image/png                             png;\n  image/x-icon                          ico;\n  image/x-jng                           jng;\n  image/vnd.wap.wbmp                    wbmp;\n  application/java-archive              jar war ear;\n  application/mac-binhex40              hqx;\n  application/pdf                       pdf;\n  application/x-cocoa                   cco;\n  application/x-java-archive-diff       jardiff;\n  application/x-java-jnlp-file          jnlp;\n  application/x-makeself                run;\n  application/x-perl                    pl pm;\n  application/x-pilot                   prc pdb;\n  application/x-rar-compressed          rar;\n  application/x-redhat-package-manager  rpm;\n  application/x-sea                     sea;\n  application/x-shockwave-flash         swf;\n  application/x-stuffit                 sit;\n  application/x-tcl                     tcl tk;\n  application/x-x509-ca-cert            der pem crt;\n  application/x-xpinstall               xpi;\n  application/zip                       zip;\n  application/octet-stream              deb;\n  application/octet-stream              bin exe dll;\n  application/octet-stream              dmg;\n  application/octet-stream              eot;\n  application/octet-stream              iso img;\n  application/octet-stream              msi msp msm;\n  audio/mpeg                            mp3;\n  audio/x-realaudio                     ra;\n  video/mpeg                            mpeg mpg;\n  video/quicktime                       mov;\n  video/x-flv                           flv;\n  video/x-msvideo                       avi;\n  video/x-ms-wmv                        wmv;\n  video/x-ms-asf                        asx asf;\n  video/x-mng                           mng;\n}\n"
         (:mime.types (:data (cut/generate-nginx-configmap {:unique-name "test.io",
                                                            :gitea-host "gitea.evilorg",
                                                            :gitea-repo "none",
                                                            :branchname "mablain",
                                                            :fqdns ["test.de" "www.test.de" "test-it.de" "www.test-it.de"]})))))
  (is (= "user nginx;\nworker_processes 3;\nerror_log /var/log/nginx/error.log;\npid /var/log/nginx/nginx.pid;\nworker_rlimit_nofile 8192;\nevents {\n  worker_connections 4096;\n}\nhttp {\n  include /etc/nginx/mime.types;\n  default_type application/octet-stream;\n  log_format   main '$remote_addr - $remote_user [$time_local] $status'\n  '\"$request\" $body_bytes_sent \"$http_referer\"'\n  '\"$http_user_agent\" \"$http_x_forwarded_for\"';\n  access_log /var/log/nginx/access.log main;\n  sendfile on;\n  tcp_nopush on;\n  keepalive_timeout 65;\n  server_names_hash_bucket_size 128;\n  include /etc/nginx/conf.d/website.conf;\n}\n"
         (:nginx.conf (:data (cut/generate-nginx-configmap {:unique-name "test.io",
                                                            :gitea-host "gitea.evilorg",
                                                            :gitea-repo "none",
                                                            :branchname "mablain",
                                                            :fqdns ["test.de" "www.test.de" "test-it.de" "www.test-it.de"]})))))
  (is (= {:apiVersion "v1",
          :kind "ConfigMap",
          :metadata {:name "test-io-configmap",
                     :labels {:app.kubernetes.part-of "test-io-website"},
                     :namespace "default"}}
         (dissoc (cut/generate-nginx-configmap {:unique-name "test.io",
                                                :gitea-host "gitea.evilorg",
                                                :gitea-repo "none",
                                                :branchname "mablain",
                                                :fqdns ["test.de" "www.test.de" "test-it.de" "www.test-it.de"]}) :data))))

(deftest should-generate-nginx-deployment
  (is (= {:apiVersion "apps/v1",
          :kind "Deployment",
          :metadata {:name "test-io-deployment", :labels {:app.kubernetes.part-of "test-io-website"}},
          :spec
          {:replicas 1,
           :selector {:matchLabels {:app "test-io-nginx"}},
           :template
           {:metadata {:labels {:app "test-io-nginx"}},
            :spec
            {:containers
             [{:name "test-io-nginx",
               :image "nginx:latest",
               :imagePullPolicy "IfNotPresent",
               :ports [{:containerPort 80}],
               :volumeMounts
               [{:mountPath "/etc/nginx", :readOnly true, :name "nginx-config-volume"}
                {:mountPath "/var/log/nginx", :name "log"}
                {:mountPath "/var/www/html/website", :name "content-volume", :readOnly true}]}],
             :initContainers
             [{:image "domaindrivenarchitecture/c4k-website-build",
               :name "test-io-init-build-container",
               :imagePullPolicy "IfNotPresent",
               :resources {:requests {:cpu "500m", :memory "256Mi"}, :limits {:cpu "1700m", :memory "512Mi"}},
               :command ["/entrypoint.sh"],
               :envFrom [{:secretRef {:name "test-io-secret"}}],
               :env [{:name "SHA256SUM", :value "123456789ab123cd345de"} {:name "SCRIPTFILE", :value "script-file-name.sh"}],
               :volumeMounts [{:name "content-volume", :mountPath "/var/www/html/website"}
                              {:name "hashfile-volume", :mountPath "/var/hashfile.d"}]}],
             :volumes
             [{:name "nginx-config-volume",
               :configMap
               {:name "test-io-configmap",
                :items
                [{:key "nginx.conf", :path "nginx.conf"}
                 {:key "website.conf", :path "conf.d/website.conf"}
                 {:key "mime.types", :path "mime.types"}]}}
              {:name "log", :emptyDir {}}
              {:name "content-volume", :persistentVolumeClaim {:claimName "test-io-content-volume"}}
              {:name "hashfile-volume", :persistentVolumeClaim {:claimName "test-io-hashfile-volume"}}]}}}}
         (cut/generate-nginx-deployment {:gitea-host "gitlab.de",
                                         :fqdns ["test.de" "test.org" "www.test.de" "www.test.org"],
                                         :gitea-repo "repo",
                                         :sha256sum-output "123456789ab123cd345de script-file-name.sh",
                                         :issuer "staging",
                                         :branchname "main",
                                         :unique-name "test.io"}))))

(deftest should-generate-resource-requests
  (is (= {:requests {:cpu "500m", :memory "256Mi"}, :limits {:cpu "1700m", :memory "512Mi"}}
         (-> (cut/generate-nginx-deployment {:gitea-host "gitlab.de",
                                             :fqdns ["test.de" "test.org" "www.test.de" "www.test.org"],
                                             :gitea-repo "repo",
                                             :sha256sum-output "123456789ab123cd345de script-file-name.sh",
                                             :issuer "staging",
                                             :branchname "main",
                                             :unique-name "test.io"}) 
             :spec :template :spec :initContainers first :resources )))
  (is (= {:requests {:cpu "1500m", :memory "512Mi"}, :limits {:cpu "3000m", :memory "1024Mi"}}
         (-> (cut/generate-nginx-deployment {:gitea-host "gitlab.de",
                                             :fqdns ["test.de" "test.org" "www.test.de" "www.test.org"],
                                             :gitea-repo "repo",
                                             :sha256sum-output "123456789ab123cd345de script-file-name.sh",
                                             :issuer "staging",
                                             :branchname "main",
                                             :unique-name "test.io"
                                             :build-cpu-request "1500m"
                                             :build-cpu-limit "3000m"
                                             :build-memory-request "512Mi"
                                             :build-memory-limit "1024Mi"})
             :spec :template :spec :initContainers first :resources))))

(deftest should-generate-nginx-service
  (is (= {:name-c1 "test-io-service",
          :name-c2 "test-org-service",
          :app-c1 "test-io-nginx",
          :app-c2 "test-org-nginx",
          :app.kubernetes.part-of-c1 "test-io-website",
          :app.kubernetes.part-of-c2 "test-org-website"}
         (th/map-diff (cut/generate-nginx-service {:unique-name "test.io",
                                                   :gitea-host "gitea.evilorg",
                                                   :gitea-repo "none",
                                                   :branchname "mablain",
                                                   :fqdns ["test.de" "www.test.de" "test-it.de" "www.test-it.de"]})
                      (cut/generate-nginx-service {:unique-name "test.org",
                                                   :gitea-host "gitea.evilorg",
                                                   :gitea-repo "none",
                                                   :branchname "mablain",
                                                   :fqdns ["test.de" "www.test.de" "test-it.de" "www.test-it.de"]})))))

(deftest should-generate-website-build-cron
  (is (= {:apiVersion "batch/v1",
          :kind "CronJob",
          :metadata {:name "test-io-build-cron", :labels {:app.kubernetes.part-of "test-io-website"}},
          :spec
          {:schedule "0/7 * * * *",
           :successfulJobsHistoryLimit 1,
           :failedJobsHistoryLimit 1,
           :jobTemplate
           {:spec
            {:template
             {:spec
              {:containers
               [{:image "domaindrivenarchitecture/c4k-website-build",
                 :name "test-io-build-app",
                 :imagePullPolicy "IfNotPresent",
                 :resources {:requests {:cpu "500m", :memory "256Mi"}, :limits {:cpu "1700m", :memory "512Mi"}},
                 :command ["/entrypoint.sh"],
                 :envFrom [{:secretRef {:name "test-io-secret"}}],
                 :env [{:name "SHA256SUM", :value "123456789ab123cd345de"} {:name "SCRIPTFILE", :value "script-file-name.sh"}],
                 :volumeMounts [{:name "content-volume", :mountPath "/var/www/html/website"}
                                {:name "hashfile-volume", :mountPath "/var/hashfile.d"}]}],
               :volumes [{:name "content-volume", :persistentVolumeClaim {:claimName "test-io-content-volume"}}
                         {:name "hashfile-volume", :persistentVolumeClaim {:claimName "test-io-hashfile-volume"}}],
               :restartPolicy "OnFailure"}}}}}}
         (cut/generate-website-build-cron {:gitea-host "gitlab.de",
                                           :fqdns ["test.de" "test.org" "www.test.de" "www.test.org"],
                                           :gitea-repo "repo",
                                           :sha256sum-output "123456789ab123cd345de script-file-name.sh",
                                           :issuer "staging",
                                           :branchname "main",
                                           :unique-name "test.io"}))))

(deftest should-generate-website-build-secret
  (is (= {:apiVersion "v1",
          :kind "Secret",
          :metadata {:name "test-io-secret", :labels {:app.kubernetes.part-of "test-io-website"}},
          :data
          {:AUTHTOKEN "YWJlZGpnYmFzZG9kag==",
           :GITREPOURL "aHR0cHM6Ly9naXRsYWIuZGUvYXBpL3YxL3JlcG9zL3NvbWV1c2VyL3JlcG8vYXJjaGl2ZS9tYWluLnppcA==",
           :GITCOMMITURL "aHR0cHM6Ly9naXRsYWIuZGUvYXBpL3YxL3JlcG9zL3NvbWV1c2VyL3JlcG8vZ2l0L2NvbW1pdHMvSEVBRA=="}}
         (cut/generate-website-build-secret {:fqdns ["test.de" "test.org" "www.test.de" "www.test.org"],
                                             :gitea-repo "repo",
                                             :sha256sum-output "123456789ab123cd345de script-file-name.sh",
                                             :issuer "staging",
                                             :branchname "main",
                                             :unique-name "test.io",
                                             :gitea-host "gitlab.de"}
                                            {:unique-name "test.io",
                                             :authtoken "abedjgbasdodj",
                                             :username "someuser"}))))

(deftest should-generate-website-content-volume
  (is (= {:name-c1 "test-io-content-volume",
          :name-c2 "test-org-content-volume",
          :app-c1 "test-io-nginx",
          :app-c2 "test-org-nginx",
          :app.kubernetes.part-of-c1 "test-io-website", 
          :app.kubernetes.part-of-c2 "test-org-website"}
         (th/map-diff (cut/generate-website-content-volume {:unique-name "test.io",
                                                            :gitea-host "gitea.evilorg",
                                                            :gitea-repo "none",
                                                            :branchname "mablain",
                                                            :fqdns ["test.de" "www.test.de" "test-it.de" "www.test-it.de"]})
                      (cut/generate-website-content-volume {:unique-name "test.org",
                                                            :gitea-host "gitea.evilorg",
                                                            :gitea-repo "none",
                                                            :branchname "mablain",
                                                            :fqdns ["test.de" "www.test.de" "test-it.de" "www.test-it.de"]})))))

(deftest should-generate-hashfile-volume
  (is (= {:apiVersion "v1",
          :kind "PersistentVolumeClaim",
          :metadata
          {:name "test-io-hashfile-volume",
           :namespace "default",
           :labels {:app "test-io-nginx", :app.kubernetes.part-of "test-io-website"}},
          :spec {:storageClassName "local-path", :accessModes ["ReadWriteOnce"], :resources {:requests {:storage "16Mi"}}}}
         (cut/generate-hashfile-volume {:unique-name "test.io",
                                        :gitea-host "gitea.evilorg",
                                        :gitea-repo "none",
                                        :branchname "mablain",
                                        :fqdns ["test.de" "www.test.de" "test-it.de" "www.test-it.de"]}))))
