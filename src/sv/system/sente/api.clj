(ns sv.system.sente.api
  (:require [sv.system.main :as m]
            [sv.system.var-bind :as v]))

(def ch-chsk) ; ChannelSocket's receive channel

(def chsk-send!) ; ChannelSocket's send API (fn [user-id event]) event: [:my-app/some-req {:data "data"}]

(def connected-uids) ; Watchable, read-only atom

(v/watch-var
 #'m/system
 ::sente
 {#'ch-chsk [:sente :api :ch-recv]
  #'chsk-send! [:sente :api :send-fn]
  #'connected-uids [:sente :api :connected-uids]})
