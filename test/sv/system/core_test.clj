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
    (is (= @stopped {:a true :b true}))))

(deftest wildcard-dependency
  (let [components [{:binds [:handlers :a]
                     :start [identity :component-a]}
                    {:binds [:handlers :b]
                     :start [identity :component-b]}
                    {:binds [:handlers-dispatcher]
                     :start [identity [:handlers :*]]}]]
    (is
     (= (wildcard-replacements components)
        {[:handlers :*] [[:handlers :a] [:handlers :b]]}))
    (is
     (= (dependency-edges components)
        [[[:handlers :a] :root]
         [[:handlers :b] :root]
         [[:handlers-dispatcher] [:handlers :a]]
         [[:handlers-dispatcher] [:handlers :b]]]))))
