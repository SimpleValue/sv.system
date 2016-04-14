(ns sv.system.datomic.core
  (:require [datomic.api :as d]))

(defn connect-dev-db [uri]
  (assert (.startsWith uri "datomic:mem:"))
  (d/delete-database uri)
  (d/create-database uri)
  (d/connect uri))

(defn connection [config]
  {:binds [:datomic :con]
   :start [(if (:dev config)
             connect-dev-db
             d/connect)
           (get-in config [:datomic :uri])]
   :stop d/release})

(defn prepare-tx-data [db-part tx-data]
  (map
   (fn [entry]
     (if-not (:db/id entry)
       (assoc entry :db/id (d/tempid db-part))
       entry))
   tx-data))

(defn- transact [prepare-fn con tx]
  (doseq [tx-data (if (list? tx)
                    tx
                    (list tx))]
    @(d/transact con (prepare-fn tx-data))))

(defn schema [config]
  (when (:dev config)
    {:binds [:datomic ::schema-transacted]
     :start [transact
             (partial prepare-tx-data :db.part/db)
             [:datomic :con]
             (-> config :datomic :schema)]}))

(defn basic-data [config]
  (when (:dev config)
    {:binds [:datomic ::basic-data]
     :start [transact
             (partial prepare-tx-data :db.part/user)
             [:datomic :con]
             (-> config :datomic :basic-data)]}))

(defn queue [config]
  {:binds [:datomic :queue]
   :start [d/tx-report-queue [:datomic :con]]
   :stop [d/remove-tx-report-queue [:datomic :con]]})

