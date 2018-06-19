(ns adl-support.core
  (:require [clojure.string :refer [split]]))


(defn query-string-to-map
  [query-string]
  (reduce
   merge
   (map
    #(let [pair (split % #"=")
           value (try
                   (read-string (nth pair 1))
                   (catch Exception _
                     (nth pair 1)))]
       (hash-map (keyword (first pair) (nth pair 1))))
    (split query-string #"\&"))))
