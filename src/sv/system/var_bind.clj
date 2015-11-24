(ns sv.system.var-bind)

(defn bind-vars [system mappings]
  (doseq [[var path] mappings]
    (alter-var-root
     var
     (constantly (get-in system path)))))

(defn var-name-mappings
  [prefix vars]
  (map
   (fn [var]
     [var (conj
           (vec prefix)
           (keyword (:name (meta var))))])
   vars))

(defn watch-var [system-var key mappings]
  (add-watch
   system-var
   key
   (fn [key var old-state new-state]
     (bind-vars
      new-state
      mappings))))
