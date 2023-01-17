(ns my.clojure-game-geek.schema
  "Contains custom resolvers and a function to provide the full schema."
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.resolve :refer [resolve-as]]
            [my.clojure-game-geek.db :as db]
            [clojure.edn :as edn]))

(defn game-by-id
  [db]
  (fn [_ args _]
    (db/find-game-by-id db (:id args))))

(defn member-by-id
  [db]
  (fn [_ args _]
    (db/find-member-by-id db (:id args))))

(defn board-game-designers
  [db]
  (fn [_ _ board-game]
    (db/list-designers-for-game db (:id board-game))))

(defn designer-games
  [db]
  (fn [_ _ designer]
    (db/list-games-for-designer db (:id designer))))

(defn rating-summary
  [db]
  (fn [_ _ board-game]
    (let [ratings (map :rating (db/list-ratings-for-game db (:id board-game)))
          n (count ratings)]
      {:count   n
       :average (if (zero? n)
                  0
                  (/ (apply + ratings)
                     (float n)))})))

(defn member-ratings
  [db]
  (fn [_ _ member]
    (db/list-ratings-for-member db (:id member))))

(defn game-rating->game
  [db]
  (fn [_ _ game-rating]
    (db/find-game-by-id db (:game-id game-rating))))

(defn rate-game
  [db]
  (fn [_ args _]
    (let [{game-id   :gameId
           member-id :memberId
           rating    :rating} args
          game (db/find-game-by-id db game-id)
          member (db/find-member-by-id db member-id)]
      (cond
        (nil? game)
        (resolve-as nil {:message "Game not found"
                         :status  404})

        (nil? member)
        (resolve-as nil {:message "Member not found"
                         :status  404})

        (not (<= 1 rating 5))
        (resolve-as nil {:message "Rating must be between 1 and 5"
                         :status  400})

        :else
        (do
          (db/upsert-game-rating db game-id member-id rating)
          game)))))

(defn resolver-map
  [component]
  (let [{:keys [db]} component]
    {:Query/gameById          (game-by-id db)
     :Query/memberById        (member-by-id db)
     :Mutation/rateGame       (rate-game db)
     :BoardGame/designers     (board-game-designers db)
     :BoardGame/ratingSummary (rating-summary db)
     :Designer/games          (designer-games db)
     :Member/ratings          (member-ratings db)
     :GameRating/game         (game-rating->game db)}))

(defn load-schema
  [component]
  (-> (io/resource "cgg-schema.edn")
      slurp
      edn/read-string
      (util/inject-resolvers (resolver-map component))
      schema/compile))

(defrecord SchemaProvider [db schema]

  component/Lifecycle

  (start [this]
    (assoc this :schema (load-schema this)))

  (stop [this]
    (assoc this :schema nil)))
