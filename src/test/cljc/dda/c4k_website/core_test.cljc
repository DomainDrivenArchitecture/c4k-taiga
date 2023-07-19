(ns dda.c4k-website.core-test
  (:require
   #?(:cljs [shadow.resource :as rc])
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [clojure.spec.alpha :as s]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-website.core :as cut]
   [clojure.spec.alpha :as s]))

#?(:cljs
   (defmethod yaml/load-resource :website-test [resource-name]
     (case resource-name
       "website-test/valid-auth.yaml"   (rc/inline "website-test/valid-auth.yaml")
       "website-test/valid-config.yaml" (rc/inline "website-test/valid-config.yaml")
       (throw (js/Error. "Undefined Resource!")))))

(deftest validate-valid-resources
  (is (s/valid? cut/config? (yaml/load-as-edn "website-test/valid-config.yaml")))
  (is (s/valid? cut/auth? (yaml/load-as-edn "website-test/valid-auth.yaml"))))

(def websites1
  {:websites
   [{:unique-name "example.io"
     :fqdns ["example.org", "www.example.com"]
     :gitea-host "finegitehost.net"
     :gitea-repo "repo"
     :branchname "main"}
    {:unique-name "test.io"
     :fqdns ["test.de" "test.org" "www.test.de" "www.test.org"]
     :gitea-host "gitlab.de"
     :gitea-repo "repo"
     :branchname "main"}]})

(def websites2
  {:websites
   [{:unique-name "test.io"
     :fqdns ["test.de" "test.org" "www.test.de" "www.test.org"]
     :gitea-host "gitlab.de"
     :gitea-repo "repo"
     :branchname "main"}
    {:unique-name "example.io"
     :fqdns ["example.org", "www.example.com"]
     :gitea-host "finegitehost.net"
     :gitea-repo "repo"
     :branchname "main"}]})

(def auth1
  {:auth
   [{:unique-name "example.io"
     :username "someuser"
     :authtoken "abedjgbasdodj"}
    {:unique-name "test.io"
     :username "someuser"
     :authtoken "abedjgbasdodj"}]})

(def auth2
  {:auth
   [{:unique-name "test.io"
     :username "someuser"
     :authtoken "abedjgbasdodj"}
    {:unique-name "example.io"
     :username "someuser"
     :authtoken "abedjgbasdodj"}]})

(def flattened-and-reduced-config
  {:unique-name "example.io",
   :fqdns ["example.org" "www.example.com"],
   :gitea-host "finegitehost.net",
   :gitea-repo "repo",
   :branchname "main"})

(def flattened-and-reduced-auth
  {:unique-name "example.io",
   :username "someuser",
   :authtoken "abedjgbasdodj"})

(deftest sorts-config
  (is (= {:issuer "staging",
          :websites
          [{:unique-name "example.io",
            :fqdns ["example.org" "www.example.com"],
            :gitea-host "finegitehost.net",
            :gitea-repo "repo",
            :branchname "main"},
           {:unique-name "test.io",
            :fqdns ["test.de" "test.org" "www.test.de" "www.test.org"],
            :gitea-host "gitlab.de",
            :gitea-repo "repo",
            :branchname "main",
            :sha256sum-output "123456789ab123cd345de script-file-name.sh"}],
          :mon-cfg {:grafana-cloud-url "url-for-your-prom-remote-write-endpoint", :cluster-name "jitsi", :cluster-stage "test"}}
         (cut/sort-config
          {:issuer "staging",
           :websites
           [{:unique-name "test.io",
             :fqdns ["test.de" "test.org" "www.test.de" "www.test.org"],
             :gitea-host "gitlab.de",
             :gitea-repo "repo",
             :branchname "main",
             :sha256sum-output "123456789ab123cd345de script-file-name.sh"}
            {:unique-name "example.io",
             :fqdns ["example.org" "www.example.com"],
             :gitea-host "finegitehost.net",
             :gitea-repo "repo",
             :branchname "main"}],
           :mon-cfg {:grafana-cloud-url "url-for-your-prom-remote-write-endpoint", :cluster-name "jitsi", :cluster-stage "test"}}))))

(deftest test-flatten-and-reduce-config
  (is (=
       flattened-and-reduced-config
       (cut/flatten-and-reduce-config (cut/sort-config websites1))))
  (is (=
       flattened-and-reduced-config
       (cut/flatten-and-reduce-config (cut/sort-config websites2)))))

(deftest test-flatten-and-reduce-auth
  (is (= flattened-and-reduced-auth
         (cut/flatten-and-reduce-auth (cut/sort-auth auth1))))
  (is (= flattened-and-reduced-auth
         (cut/flatten-and-reduce-auth (cut/sort-auth auth2)))))
