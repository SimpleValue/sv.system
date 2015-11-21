(ns sv.system.httpkit.core
  (:require [org.httpkit.server :as server]))

(defn start [ring-handler opts]
  (server/run-server
   ring-handler
   opts))

(defn stop [server]
  (server))

(defn httpkit-server [config]
  {:binds [:httpkit :server]
   :start [start [:ring :handler] (-> config :httpkit :opts)]
   :stop stop})

