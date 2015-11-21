(ns sv.system.ring.hello-handler)

(defn hello-handler-fn []
  (fn [request]
    {:status 200
     :body "Hello"
     :content-type "text/plain"}))

(defn hello-handler [config]
  {:binds [:ring :handler]
   :start [hello-handler-fn]})
