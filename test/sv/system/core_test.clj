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
   {:binds [:c]
    :start [(fn [component-b]
              (throw (Exception. "starting c failed")))
            [:b]]
    :stop [(fn []
             (swap! stopped assoc :c true))]}])

(deftest failed-start
  (let [stopped (atom {})
        exception (is
                   (thrown?
                    clojure.lang.ExceptionInfo
                    (start-system (components stopped))))
        ex-data (ex-data exception)]
    (is (= (:started-components ex-data) [[:a] [:b]]))
    (is (= (:failed-component ex-data) [:c]))
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
