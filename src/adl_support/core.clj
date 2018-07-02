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
        #(let [pair (split % #"=")]
           (if (= (count pair) 2)
             (let
               [v (try
                    (read-string (nth pair 1))
                    (catch Exception _
                      (nth pair 1)))
                value (if (number? v) v (str v))]
               (hash-map (keyword (first pair)) value))
             {}))
        (split query-string #"\&")))))


(defn massage-value
  [k m]
  (let [v (m k)
        vr (if
             (string? v)
             (try
               (read-string v)
               (catch Exception _ nil)))]
    (cond
     (nil? v) {}
     (= v "") {}
     (number? vr) {k vr}
     true
     {k v})))


(defn massage-params
  "Sending empty strings, or numbers as strings, to the database often isn't
  helpful. Massage these `params` and `form-params` to eliminate these problems.
  We must take key field values out of just params, but we should take all other
  values out of form-params - because we need the key to load the form in
  the first place, but just accepting values of other params would allow spoofing."
  [params form-params key-fields]
  (reduce
   merge
   ;; do the keyfields first, from params
   (reduce
    merge
    {}
    (map
     #(massage-value % params)
     (filter
      #(key-fields (str (name %)))
      (keys params))))
   ;; then merge in everything from form-params, potentially overriding what
   ;; we got from params.
   (map
    #(massage-value % form-params)
    (keys form-params))))


(defn
  raw-resolve-template
  [n]
  (if
    (.exists (io/as-file (str "resources/templates/" n)))
    n
    (str "auto/" n)))


(def resolve-template (memoize raw-resolve-template))


(defmacro do-or-log-error
  "Evaluate the supplied `form` in a try/catch block. If the
  keyword param `:message` is supplied, the value will be used
  as the log message; if the keyword param `:error-return` is
  supplied, the value will be returned if an exception is caught."
  [form & {:keys [message error-return]
           :or {message `(str "A failure occurred in "
                              ~(list 'quote form))}}]
  `(try
     ~form
     (catch Exception any#
       (clojure.tools.logging/error
        (str ~message
             (with-out-str
               (-> any# .printStackTrace))))
       ~error-return)))


