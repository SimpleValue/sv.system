(ns sv.system.nrepl.server
  (:require [clojure.tools.nrepl.server :as s]))

(defn nrepl-server [config]
  (let [cfg (merge {:port 4000} (get-in config [:nrepl :server]))]
    {:binds [:nrepl :server :process]
     :start [apply s/start-server (apply concat cfg)]
     :stop s/stop-server}))

