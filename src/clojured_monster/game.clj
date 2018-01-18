(ns clojured-monster.game)

(defn create-game [ids game-states]
  (do 
    (let [game-state-key (apply min ids)]
      (swap! game-states assoc-in [game-state-key] {:monster-chars {} :ids ids}))
    "Put some text from Yulia here."))

(defn compute-changes [id action game-state-key game-states]
  nil)
