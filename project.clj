(defproject adl-support "0.1.2-SNAPSHOT"
  :description "A small library of functions called by generated ADL code."
  :url "https://github.com/simon-brooke/adl-support"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.clojure/tools.logging "0.3.1"]
                 [selmer "1.10.6"]]

  :plugins [[lein-codox "0.10.3"]
            [lein-release "1.0.5"]]

  :release-tasks [["vcs" "assert-committed"]
                  ["clean"]
                  ["test"]
                  ["codox"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ;; ["vcs" "tag"] -- not working, problems with secret key
                  ["uberjar"]
                  ["install"]
                  ["deploy" "clojars"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]])
