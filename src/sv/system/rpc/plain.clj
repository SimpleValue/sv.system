(ns sv.system.rpc.plain)

(defn start [rpc-fns]
  (fn [msg]
    (when-let [fn (:fn msg)]
      (get rpc-fns (keyword fn)))))

(defn plain-get-rpc-fn [config]
  {:binds [:rpc :get-rpc-fn]
   :start [start [:rpc :fns :*]]})
