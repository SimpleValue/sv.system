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

(defn transact-schema [con schema]
  @(d/transact
    con
    schema)
  true)

(defn schema [config]
  (when (:dev config)
    {:binds [:datomic ::schema-transacted]
     :start [transact-schema [:datomic :con] (-> config :datomic :schema)]}))

(defn prepare-tx-data [tx-data]
  (map
   (fn [entry]
     (if-not (:db/id entry)
       (assoc entry :db/id (d/tempid :db.part/user))
       entry))
   tx-data))

(defn transact-basic-data [con basic-data]
  @(d/transact
    con
    (prepare-tx-data basic-data))
  true)

(defn basic-data [config]
  (when (:dev config)
    {:binds [:datomic ::basic-data]
     :start [transact-basic-data [:datomic :con]
             (-> config :datomic :basic-data)]}))

(defn queue [config]
  {:binds [:datomic :queue]
   :start [d/tx-report-queue [:datomic :con]]
   :stop [d/remove-tx-report-queue [:datomic :con]]})
