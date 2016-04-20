(ns sv.system.sente.api
  (:require [sv.system.main :as m]))

(defn get-ch-chsk
  "Returns ChannelSocket's receive channel"
  []
  (get-in m/system [:sente :api :ch-recv]))

(defn chsk-send!
  "ChannelSocket's send API (fn [user-id event])
  event: [:my-app/some-req {:data \"data\"}]"
  [& args]
  (if-let [send-fn (get-in m/system [:sente :api :send-fn])]
    (apply send-fn args)
    (throw
     (ex-info "[:sente :api :send-fn] is not bound in sv.system.main/system."))))

(defn get-connected-uids
  "Watchable, read-only atom."
  []
  (get-in m/system [:sente :api :connected-uids]))
