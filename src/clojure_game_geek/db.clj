(ns clojure-game-geek.db
  (:require
    [clojure.java.io :as io]
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
  (->> component
       :db
       deref
       :members
       (filter #(= member-id (:id %)))
       first))

(defn list-designers-for-game
  [component game-id]
  (let [designers (:designers (find-game-by-id component game-id))]
    (->> component
         :db
         deref
         :designers
         (filter #(contains? designers (:id %))))))

(defn list-games-for-designer
  [component designer-id]
  (->> component
       :db
       deref
       :games
       (filter #(-> % :designers (contains? designer-id)))))

(defn list-ratings-for-game
  [component game-id]
  (->> component
       :db
       deref
       :ratings
       (filter #(= game-id (:game_id %)))))

(defn list-ratings-for-member
  [component member-id]
  (->> component
       :db
       deref
       :ratings
       (filter #(= member-id (:member_id %)))))

(defn ^:private apply-game-rating
  [game-ratings game-id member-id rating]
  (->> game-ratings
       (remove #(and (= game-id (:game_id %))
                     (= member-id (:member_id %))))
       (cons {:game_id game-id
              :member_id member-id
              :rating rating})))

(defn upsert-game-rating
  "Adds a new game rating, or changes the value of an existing game rating."
  [db game-id member-id rating]
  (-> db
      :db
      (swap! update :ratings apply-game-rating game-id member-id rating)))
