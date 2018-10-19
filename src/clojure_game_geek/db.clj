(ns clojure-game-geek.db
  (:require
    [com.stuartsierra.component :as component]
    [postgres.async :refer [open-db query! close-db!]]
    [clojure.core.async :refer [<!!]]))

(defrecord ClojureGameGeekDb [conn]

  component/Lifecycle

  (start [this]
    (assoc this
           :conn (open-db {:hostname "localhost"
                         :database "cggdb"
                         :username "cgg_role"
                         :password "lacinia"
                         ;; Host port mapped to 5432 in the container
                         :port 25432})))

  (stop [this]
    (close-db! conn)
    (assoc this :conn nil)))

(defn new-db
  []
  {:db (map->ClojureGameGeekDb {})})

(defn ^:private take!
  "Takes a value from a channel and rethrows an exception if that is the
  conveyed value."
  [ch]
  (let [v (<!! ch)]
    (if (instance? Throwable v)
      (throw v)
      v)))

(defn find-game-by-id
  [component game-id]
  (-> (query! (:conn component)
              ["select game_id, name, summary, min_players, max_players, created_at, updated_at
               from board_game where game_id = $1" game-id])
      take!
      first))

(defn find-member-by-id
  [component member-id]
  (-> (query! (:conn component)
              ["select member_id, name, created_at, updated_at
              from member
              where member_id = $1" member-id])
      take!
      first))

(defn list-designers-for-game
  [component game-id]
  (take!
    (query! (:conn component)
            ["select d.designer_id, d.name, d.uri, d.created_at, d.updated_at
              from designer d
              inner join designer_to_game j on (d.designer_id = j.designer_id)
              where j.game_id = $1
              order by d.name" game-id])))

(defn list-games-for-designer
  [component designer-id]
  (take!
    (query! (:conn component)
            ["select g.game_id, g.name, g.summary, g.min_players, g.max_players, g.created_at, g.updated_at
              from board_game g
              inner join designer_to_game j on (g.game_id = j.game_id)
              where j.designer_id = $1
              order by g.name" designer-id])))

(defn list-ratings-for-game
  [component game-id]
  (take!
    (query! (:conn component)
            ["select game_id, member_id, rating, created_at, updated_at
              from game_rating
              where game_id = $1" game-id])))

(defn list-ratings-for-member
  [component member-id]
  (take!
    (query! (:conn component)
            ["select game_id, member_id, rating, created_at, updated_at
              from game_rating
              where member_id = $1" member-id])))

(defn upsert-game-rating
  "Adds a new game rating, or changes the value of an existing game rating."
  [component game-id member-id rating]
  (take!
    (query! (:conn component)
            ["insert into game_rating (game_id, member_id, rating)
              values ($1, $2, $3)
              on conflict (game_id, member_id) do update set rating = $3"
             game-id member-id rating])))
