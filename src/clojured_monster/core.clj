(ns clojured-monster.core
  (:require [clojure.core.async :refer [<!!]]
            [clojure.core.match :refer [match]]
            [clojure.tools.trace :refer [trace-forms trace]]
            [clojure.string :as str]
            [environ.core :refer [env]]
            [morse.handlers :as h]
            [morse.polling :as p]
            [morse.api :as t]
            [clojure.string :refer [lower-case]]
            [clojure.set :refer [difference]]
            [clojured-monster.game :refer [create-game compute-changes]])
  (:gen-class))

(def token (apply str (drop-last (slurp "clojured_monster.token"))))

(def settings (read-string (slurp "resources/settings.clj")))

(def texts (read-string (slurp "resources/english_text.clj")))

(def state (atom {}))

(def id-to-game-info (atom {}))


(defn compute-changes-wrap [id action]
  (if (contains? @id-to-game-info id)
    (let [response (compute-changes id action (first (get @id-to-game-info id))) 
          players (second (get @id-to-game-info id))]
      (if (:game-over response)
        (swap! id-to-game-info #(apply (partial dissoc %1) %2) players))
      (doall (for [player players]
               (t/send-text token player (:text response)))))
    (t/send-text token id (:error-out-of-game texts))))

(h/defhandler handler

  (h/command-fn "start"
    (fn [{{id :id :as chat} :chat}]
      (println "Bot joined new chat: " chat)
      (t/send-text token id (:start texts))))

  (h/command-fn "help"
    (fn [{{id :id :as chat} :chat}]
      (println "Help was requested in " chat)
      (t/send-text token id (:help texts))))

  (h/message-fn
    (fn [{{id :id} :chat :as message}]
      (println "Intercepted message: " message)
      (match (lower-case (:text message))
        "play" (if (not (contains? @id-to-game-info id))
                 (do 
                   (swap! state assoc :queue (conj (or (:queue @state) #{}) id))
                   (if (>= (count (:queue @state)) (:players-number settings))
                     (let [old-state @state 
                           new-state (assoc 
                                       old-state 
                                       :queue 
                                       (drop (:players-number settings) (:queue old-state)))
                           diff (difference (:queue old-state) (:queue new-state))
                           game-info [(apply min diff) diff]] 
                       (if (compare-and-set! state old-state new-state)
                         (let [text (create-game diff)]
                           (doall (for [player (seq diff)] 
                                    (do 
                                      (t/send-text token player text)
                                      (swap! id-to-game-info assoc player game-info)))))))
                     (t/send-text token id (:stand-by texts))))
                 (t/send-text token id (:error-in-game texts)))
        "hit" (compute-changes-wrap id :hit)
        "scold" (compute-changes-wrap id :scold)
        "pet" (compute-changes-wrap id :pat)
        "feed" (compute-changes-wrap id :feed)
        "tame" (compute-changes-wrap id :tame)
        :else (t/send-text token id (:error texts)))
      )))


(defn -main
  [& args]
  (when (str/blank? token)
    (println "Please provide token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (println "Starting the clojured-monster")
  (<!! (p/start token handler)))
