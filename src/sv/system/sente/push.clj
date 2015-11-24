(ns sv.system.sente.push
  (:require [clojure.core.async :as async
             :refer [<! >!! chan close! go-loop]]
            [clojure.tools.logging :as log]))

(defn start [sente-api]
  (let [{:keys [connected-uids send-fn]} sente-api
        ch (chan)]
    (go-loop []
      (let [message (<! ch)]
        (if (nil? message)
          (log/info "stopping sente-push")
          (do
            (try
              (apply send-fn message)
              (catch Exception e
                (log/error "sente-push failed" e)))
            (recur)))))
    ch))

(defn stop [ch]
  (close! ch))

(defn sente-push [config]
  {:binds [:sente :push]
   :start [start [:sente :api]]
   :stop stop})
