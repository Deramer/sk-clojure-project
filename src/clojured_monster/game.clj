(ns clojured-monster.game)

(def game-states (atom {}))

(defn create-game [ids]
  (do 
    (let [game-state-key (apply min ids)]
      (swap! game-states assoc-in [game-state-key] {:monster-chars {} :ids ids}))
    "Put some text from Yulia here."))

(defn compute-changes [id action game-state-key]
  {:game-over false :text "Get some other text from Yulia here."})
