(ns sv.system.datomic.tx-inspect
  (:require [clojure.core.async :as a]))

(defonce tx-reports (atom (list)))

(defn start [tx-chan]
  (reset! tx-reports (list))
  (let [chan (a/chan)]
    (a/tap tx-chan chan)
    (a/thread
      (while true
        (let [tx-report (a/<!! chan)]
          (swap! tx-reports conj tx-report))))
    chan))

(defn stop [chan tx-chan]
  (reset! tx-reports (list))
  (a/untap tx-chan chan))

(defn tx-inspect [config]
  (when (:dev config)
    {:binds [:datomic ::tx-inspect]
     :start [start [:datomic :tx-chan]]
     :stop [stop
            [:datomic ::tx-inspect]
            [:datomic :tx-chan]]}))
