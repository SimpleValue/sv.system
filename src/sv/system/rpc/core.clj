(ns sv.system.rpc.core
  (:require [sv.rpc.ring :as r]))

(defn start [get-rpc-fn config]
  (r/ring-handler
   (assoc config :get-rpc-fn get-rpc-fn)))

(defn rpc [config]
  {:binds [:rpc :ring-handler]
   :start [start
           [:rpc :get-rpc-fn]
           (get-in config [:rpc :ring-handler])]})
