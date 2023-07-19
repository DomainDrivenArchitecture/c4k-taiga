(defproject org.domaindrivenarchitecture/c4k-website-build "0.1.1-SNAPSHOT"
  :description "website c4k-build package"
  :url "https://domaindrivenarchitecture.org"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [dda/cryogen-bootstrap "0.1.5"]]
  :plugins [[lein-ring "0.12.5"]]
  :main cryogen.core
  :ring {:init cryogen.server/init
         :handler cryogen.server/handler})
