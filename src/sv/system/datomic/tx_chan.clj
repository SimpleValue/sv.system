(ns sv.system.datomic.tx-chan
  (:require [clojure.core.async :as a]
            [clojure.tools.logging :as log]))

(defn start [tx-queue]
  (let [tx-chan (a/chan)
        mult (a/mult tx-chan)]
    (log/info "starting tx-chan")
    (a/thread
      (loop []
        (let [tx-report (.take tx-queue)]
          (if (= tx-queue ::stop)
            (log/info "stopping tx-chan")
            (do
              (try
                (a/>!! tx-chan tx-report)
                (catch Exception e
                  (log/error
                   "Error while putting tx-report into tx-chan." e)))
              (recur))))))
    mult))

(defn stop [tx-chan tx-queue]
  (a/untap-all tx-chan)
  (.put tx-queue ::stop))

(defn tx-chan [config]
  {:binds [:datomic :tx-chan]
   :start [start [:datomic :queue]]
   :stop [stop [:datomic :tx-chan] [:datomic :queue]]})
