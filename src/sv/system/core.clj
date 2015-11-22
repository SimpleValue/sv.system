(ns sv.system.core
  (:require [com.stuartsierra.dependency :as dep]
            [clojure.tools.logging :as log]))

(defn dependency-declaration?
  "A dependency declaration is a vector with keywords like:
   [:ring :handler]

   It is the path to the dependency in the system
   map (c.f. clojure.core/get-in)."
  [x]
  (and (vector? x)
       (every? keyword? x)))

(defn dependencies [component]
  (map
   (fn [dependency-declaration]
     [(:binds component) dependency-declaration])
   (filter
    dependency-declaration?
    (:start component))))

(defn wildcard? [x]
  (= x :*))

(defn wildcard-dependency? [x]
  (and (dependency-declaration? x)
       (wildcard? (last x))))

;; TODO: assert that no dependency declaration starts with a wildcard
;;       and that there is always only one wildcard at the end
(defn wildcard-replacements [components]
  (let [binds (map :binds components)
        ;; TODO: maybe memoize edges calculation
        edges (mapcat
               dependencies
               components)
        dependencies (map second edges)
        wildcard-dependencies (filter
                               wildcard-dependency?
                               dependencies)]
    (into
     {}
     (map
      (fn [wildcard-dependency]
        (let [prefix (drop-last wildcard-dependency)]
          [wildcard-dependency
           (filter
            (fn [bind]
              (= prefix
                 (take (count prefix) bind)))
            binds)]))
      wildcard-dependencies))))

(defn replace-wildcard-dependencies [edges components]
  (let [replacements (wildcard-replacements components)]
    (mapcat
     (fn [edge]
       (if-let [replacements* (get replacements (second edge))]
         (map
          (fn [dependent]
            [(first edge)
             dependent])
          replacements*)
         [edge]))
     edges)))

(defn dependency-edges [components]
  (let [edges (mapcat
               dependencies
               components)]
    (concat
     (map
      (fn [component]
        [(:binds component) :root])
      (remove
       (fn [component]
         (seq (dependencies component)))
       components))
     (replace-wildcard-dependencies
      edges
      components))))

(defn dependency-graph [components]
  (reduce
   (fn [g dependency]
     (apply dep/depend g dependency))
   (dep/graph)
   (dependency-edges components)))

(defn components-by-binds [components]
  (into
   {}
   (map
    (juxt :binds identity)
    components)))

(defn start-order [components]
  (assert (= (count components)
             (count (distinct (map :binds components))))
          "binds must be unique")
  (let [index (components-by-binds components)]
    (keep
     index
     (dep/topo-sort
      (dependency-graph
       (remove
        nil?
        components))))))

(defn fill-args [system invocation]
  (map
   (fn [arg]
     (if (dependency-declaration? arg)
       (get-in system (remove wildcard? arg))
       arg))
   (rest invocation)))

(defn invoke-start [system component]
  (let [args (fill-args system (:start component))
        f (first (:start component))]
    (apply f args)))

(defn invoke-stop [system component]
  (when-let [stop (:stop component)]
    (cond
      (fn? stop)
      (stop (get-in system (:binds component)))

      (and (vector? stop) (fn? (first stop)))
      (let [args (fill-args system stop)]
        (apply (first stop) args)))))

(defn stop-components [system order]
  (doseq [component order]
    (log/info "stopping" (:binds component))
    (try
      (invoke-stop system component)
      (catch Exception e
        (log/error (str "failed to stop component:"
                        (:binds component))
                   e)))))

(defn start-components [order]
  (loop [system {}
         started []
         left order]
    (if-let [component (first left)]
      (do
        (log/info "starting" (:binds component))
        (let [c (try
                  (invoke-start system component)
                  (catch Exception e
                    (log/error "component start failed" e)
                    ::component-start-failed))]
          (if (= ::component-start-failed c)
            (do
              (log/error "stopping system since start failed")
              (stop-components system (reverse started))
              false)
            (let [new-system (assoc-in system (:binds component) c)]
              (recur new-system
                     (conj started component)
                     (rest left))))))
      system)))

;; TODO: stop system if some parts fail to start
(defn start-system [components]
  (let [order (start-order components)
        system (start-components order)]
    (when system
      (vary-meta
       system
       assoc ::order order))))

(defn stop-system [system]
  (let [order (::order (meta system))]
    (when-not order
      (throw (Exception. "system has no order meta data")))
    (stop-components system (reverse order))))

(defn config-components [config components]
  (doall
   (map
    (fn [component]
      (component config))
    components)))
