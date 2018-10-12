(ns user
  (:require
    [com.walmartlabs.lacinia :as lacinia]
    [clojure.java.browse :refer [browse-url]]
    [clojure-game-geek.system :as system]
    [clojure-game-geek.test-utils :refer [simplify]]
    [com.stuartsierra.component :as component]))

(defonce system nil)

(defn q
  [query-string]
  (-> system
      :schema-provider
      :schema
      (lacinia/execute query-string nil nil)
      simplify))

(defn start
  []
  (alter-var-root #'system (fn [_]
                             (-> (system/new-system)
                                 component/start-system)))
  (browse-url "http://localhost:8888/")
  :started)

(defn stop
  []
  (when (some? system)
    (component/stop-system system)
    (alter-var-root #'system (constantly nil)))
  :stopped)

(comment
  (start)
  (stop)
  )
