(ns clojured-monster.core
  (:require [clojure.core.async :refer [<!!]]
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

(def token (apply str (drop-last (slurp "../clojured_monster.settings"))))

(def settings (read-string (slurp "settings.clj")))

(def state (atom {}))

(def game-states (atom {}))

(h/defhandler handler

  (h/command-fn "start"
    (fn [{{id :id :as chat} :chat}]
      (println "Bot joined new chat: " chat)
      (t/send-text token id "Welcome to clojured-monster!")))

  (h/command-fn "help"
    (fn [{{id :id :as chat} :chat}]
      (println "Help was requested in " chat)
      (t/send-text token id "Help is on the way")))

  (h/message-fn
    (fn [{{id :id} :chat :as message}]
      (println "Intercepted message: " message)
      (case (lower-case (:text message))
        "play" (do 
                   (swap! state assoc :queue (conj (or (:queue @state) #{}) id))
                   (if (>= (count (:queue @state)) (:players-number settings))
                     (let [old-state @state 
                           new-state (assoc 
                                       @state 
                                       :queue 
                                       (drop (:players-number settings) (:queue @state)))
                           diff (difference (:queue old-state) (:queue new-state))] 
                       (if (compare-and-set! state old-state new-state)
                         (let [text (create-game diff game-states)]
                           (doall (for [player (seq diff)] (t/send-text token player text))))))
                     (t/send-text token id "Waiting for other players...")))
        (t/send-text token id "It's not a command. Please see help for the list of available commands."))
      (println @game-states)
      )))


(defn -main
  [& args]
  (when (str/blank? token)
    (println "Please provide token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (println "Starting the clojured-monster")
  (<!! (p/start token handler)))
