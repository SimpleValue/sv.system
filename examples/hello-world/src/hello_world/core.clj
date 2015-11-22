(ns hello-world.core
  (:require [sv.system.core :as sys]
            [sv.system.main :refer :all]
            [sv.system.httpkit.core :as h]
            [sv.system.ring.hello-handler :as he]))

(def config
  {:httpkit {:opts {:port 3000}}})

(defn components []
  (sys/config-components
   config
   [h/httpkit-server
    he/hello-handler]))

(set-components
 #'components)
