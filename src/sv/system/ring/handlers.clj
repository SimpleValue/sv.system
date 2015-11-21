(ns sv.system.ring.handlers)

(defn start [handlers]
  (fn [request]
    (some
     (fn [handler]
       (handler request))
     (vals handlers))))

(defn handlers [config]
  {:binds [:ring :handlers-dispatcher]
   :start [start [:ring :handlers :*]]})
