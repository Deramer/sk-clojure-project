(ns clojured-monster.game)

(def texts (read-string (slurp "resources/english_text.clj")))

(def game-states (atom {}))

(defn create-game [ids]
    (let [game-state-key (apply min ids)
          four-random-numbers (shuffle [(rand-int 10) 
                                        (- (rand-int 20) 10) 
                                        (- (rand-int 20) 10) 
                                        (- (rand-int 20) 10)])
          monster {:mood (rand-int 100) 
                   :hit (nth four-random-numbers 0) 
                   :pat (nth four-random-numbers 1) 
                   :feed (nth four-random-numbers 2) 
                   :scold (nth four-random-numbers 3)  }
          new-ids (reduce into {} (map #(hash-map %  false) ids))
          ]
      (swap! game-states #(merge % {game-state-key {:monster-chars monster :ids new-ids :game-over false}})))
      (texts :start-of-the-game))
  
(defn compute-changes [id action game-state-key]
   (let [game-state (@game-states game-state-key)
        monster  (:monster-chars game-state)
        mood (:mood monster)
        new-mood mood
        id-game-over? ((game-state :ids) id)]
        ; user game over
        (if id-game-over? (:game-over-user-trying-action texts)
            (let [
                  game-over? (if (= action :tame) (if (> mood 100) true false) false)
                  id-game-over? (if (= action :tame) (if (<= mood 100) true false) false)
                  new-mood (+ mood (action monster) new-mood)
                  message (str "user " id " " 
                               action " the monster, "
                               (if game-over? 
                                 " the monster becomes his/her friend"
                                 (if id-game-over? 
                                   "the user was eaten" 
                                   (if (> (- new-mood mood) 0) " the monster is pleased" " the monster get angry"))))
                  new-monster (merge monster {:mood new-mood})
                  new-ids (merge (game-state :ids) {id id-game-over?})]
        (swap! game-states #(merge % {game-state-key {:monster-chars new-monster :ids new-ids :game-over  game-over?}}))
        message))))
