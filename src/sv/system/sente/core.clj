(ns sv.system.sente.core
  (:require [taoensso.sente :as sente]
            sv.system.sente.api
            ring.middleware.keyword-params
            ring.middleware.params))

(def server-adapters
  {:httpkit 'taoensso.sente.server-adapters.http-kit
   :immutant 'taoensso.sente.server-adapters.immutant
   :nginx-clojure 'taoensso.sente.server-adapters.nginx-clojure})

(defn start [server-adapter-name opts]
  (let [server-adapter-sym (get server-adapters server-adapter-name)]
    (assert
     server-adapter-sym
     "Please specify a valid server-adapter-name in the config under [:sente :adapter] (see sv.system.sente.core/server-adapters)")
    (require server-adapter-sym)
    (let [server-adapter (var-get
                          (find-var
                           (symbol
                            (str server-adapter-sym)
                            "sente-web-server-adapter")))]
      (sente/make-channel-socket! server-adapter opts))))

;; TODO: appropriate release of resources
(defn stop [api])

(defn sente-api [config]
  (let [server-adapter-name (get-in config [:sente :adapter])
        opts (get-in config [:sente :opts])]
    {:binds [:sente :api]
     :start [start server-adapter-name opts]
     :stop stop}))

(defn ring-handler [sente-api path]
  (let [{:keys [ajax-get-or-ws-handshake-fn ajax-post-fn]} sente-api]
    (-> (fn [request]
          (when (= (:uri request) path)
            (case (:request-method request)
              :get (ajax-get-or-ws-handshake-fn request)
              :post (ajax-post-fn request)
              nil)))
        ring.middleware.keyword-params/wrap-keyword-params
        ring.middleware.params/wrap-params)))

(defn sente-ring-handler [config]
  {:binds [:sente :ring-handler]
   :start [ring-handler
           [:sente :api]
           (get-in config [:sente :path] "/chsk")]})

(defn sente [config]
  [(sente-api config)
   (sente-ring-handler config)])
