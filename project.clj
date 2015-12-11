(defproject sv/system "0.1.0-SNAPSHOT"
  :description "System is an approach to implement component-based systems"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.stuartsierra/dependency "0.2.0"]
                 [ring/ring-core "1.4.0"]
                 [http-kit "2.1.18"]
                 [com.taoensso/sente "1.6.0"]
                 [com.datomic/datomic-free "0.9.5186"
                  :exclusions [org.slf4j/slf4j-nop]]
                 [sv.rpc/ring "0.1.0-SNAPSHOT"]
                 [org.clojure/tools.nrepl "0.2.11"]]
  
  :profiles {:dev {:dependencies [[org.slf4j/slf4j-simple "1.7.13"]]
                   :plugins [[lein-repack "0.2.10"]]}}

  ;; TODO: repack manifest does not include com.datomic/datomic-free
  ;;       dependency for branch "system.datomic"
  :repack [{:type :clojure
            :path "src"
            :levels 2}]
  ;; TODO: repack-opts is only available in include-name branch
  :repack-opts {:include-name false})
