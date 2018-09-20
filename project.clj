(defproject adl-support "0.1.5-SNAPSHOT"
  :description "A small library of functions called by generated ADL code."
  :url "https://github.com/simon-brooke/adl-support"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.memoize "0.7.1"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.clojure/tools.logging "0.4.1"]
                 [selmer "1.11.8"]]

  :plugins [[lein-codox "0.10.4"]
            [lein-release "1.0.5"]]

  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]

  ;; `lein release` doesn't play nice with `git flow release`. Run `lein release` in the
  ;; `develop` branch, then reset the `master` branch to the release tag.

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
