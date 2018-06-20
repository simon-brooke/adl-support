(ns adl-support.core
  (:require [clojure.java.io :as io]
            [clojure.string :refer [split]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;
;;;; adl-support.core: functions used by ADL-generated code.
;;;;
;;;; This program is free software; you can redistribute it and/or
;;;; modify it under the terms of the MIT-style licence provided; see LICENSE.
;;;;
;;;; This program is distributed in the hope that it will be useful,
;;;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;;; License for more details.
;;;;
;;;; Copyright (C) 2018 Simon Brooke
;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn query-string-to-map
  "A `query-string` - the query-part of a URL - comprises generally
  `<name>=<value>&<name>=<value>...`; reduce such a string to a map.
  If `query-string` is nil or empty return an empty map."
  [query-string]
  (if
    (empty? query-string)
    {}
    (reduce
      merge
      (map
        #(let [pair (split % #"=")
               v (try
                   (read-string (nth pair 1))
                   (catch Exception _
                     (nth pair 1)))
               value (if (number? v) v (str v))]
           (hash-map (keyword (first pair)) value))
        (split query-string #"\&")))))

(defn
  raw-resolve-template
  [n]
  (if
    (.exists (io/as-file (str "resources/templates/" n)))
    n
    (str "auto/" n)))

(def resolve-template (memoize raw-resolve-template))

