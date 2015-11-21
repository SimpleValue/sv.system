(ns sv.system.core-test
  (:require [sv.system.core :refer :all]
            [clojure.test :refer :all]))

(defn components [stopped]
  [{:binds [:a]
    :start [identity :component-a]
    :stop [(fn []
             (swap! stopped assoc :a true))]}
   {:binds [:b]
    :start [(fn [component-a] [:component-b component-a])
            [:component-a]]
    :stop [(fn []
             (swap! stopped assoc :b true))]}
   {:bind [:c]
    :start [(fn [component-b]
              (throw (Exception. "starting c failed")))
            [:b]]
    :stop [(fn []
             (swap! stopped assoc :c true))]}])

(deftest failed-start
  (let [stopped (atom {})]
    (start-system (components stopped))
    (println stopped)
    (assert (= @stopped {:a true :b true}))))
