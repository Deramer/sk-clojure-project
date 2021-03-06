(defproject clojured-monster "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure      "1.8.0"]
                 [environ                  "1.1.0"]
                 [morse                    "0.2.4"]
                 [org.clojure/tools.trace  "0.7.9"]
                 [org.clojure/core.match   "0.3.0-alpha5"]]

  :plugins [[lein-environ "1.1.0"]]

  :main ^:skip-aot clojured-monster.core
  :target-path "target/%s"

  :profiles {:uberjar {:aot :all}})
