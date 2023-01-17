(ns user
  (:require [com.stuartsierra.component :as component]
            [my.clojure-game-geek.db :as db]
            [my.clojure-game-geek.system :as system]
            [com.walmartlabs.lacinia :as lacinia]
            [clojure.java.browse :refer [browse-url]]
            [my.clojure-game-geek.test-utils :refer [simplify]]))

(defonce system (system/new-system))

(defn q
  [query-string]
  (-> system
      :schema-provider
      :schema
      (lacinia/execute query-string nil nil)
      simplify))

(defn start
  []
  (alter-var-root #'system component/start-system)
  (browse-url "http://localhost:8888/ide")
  :started)

(defn stop
  []
  (alter-var-root #'system component/stop-system)
  :stopped)

(comment
  (start)
  (stop)

  (def db (:db system))

  (require '[my.clojure-game-geek.db :as db])

  (db/find-member-by-id db 37)
  (db/list-designers-for-game db 1237)
  (db/list-games-for-designer db 201)
  (db/list-ratings-for-game db 1234)
  (db/list-ratings-for-member db 1410)
  (db/upsert-game-rating db 1237 1410 3)
  (db/upsert-game-rating db 1234 2812 4)

  )
