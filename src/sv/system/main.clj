(ns sv.system.main
  (:require [sv.system.core :as sys]))

(defonce system nil)

(defonce components nil)

(defn set-components
  [components-fn]
  (alter-var-root
   #'components
   (constantly
    components-fn))
  true)

(defn start []
  (assert
   (not (nil? components))
   "No components defined. Please use sv.system.main/set-components")
  (alter-var-root
   #'system
   (constantly
    (sys/start-system
     (components))))
  true)

(defn stop []
  (sys/stop-system
   system))

(defn restart []
  (stop)
  (start))
