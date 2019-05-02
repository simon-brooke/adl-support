(defproject adl-support "0.1.6"
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
  :codox {:metadata {:doc "FIXME: write docs"}
          :output-path "doc"}

  ;; `lein release` doesn't play nice with `git flow release`. Run `lein release` in the
  ;; `develop` branch, then merge the release tag into the `master` branch.

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
