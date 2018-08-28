(ns adl-support.core
  (:require [clojure.core.memoize :as memo]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :refer [split join]]
            [clojure.tools.logging]))

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


(def ^:dynamic *warn*
  "The idea here is to have a function with which to show warnings to the user,
  which can be dynamically bound. Any binding should be a function of one
  argument, which it should print, log, or otherwise display."
  (fn [s] (println s)))


(defn massage-value
  "Return a map with one key, this `k` as a keyword, whose value is the binding of
  `k` in map `m`, as read by read."
  [k m]
  (let [v (m k)
        vr (if
             (string? v)
             (try
               (json/read-str v)
               (catch Exception _ nil)))]
    (cond
     (nil? v) {}
     (= v "") {}
     (and
       (number? vr)
       ;; there's a problem that json/read-str will read "07777 888999" as 7777
       (re-matches #"^[0-9.]+$" v)) {(keyword k) vr}
     true
     {(keyword k) v})))


(defn raw-massage-params
  "Sending empty strings, or numbers as strings, to the database often isn't
  helpful. Massage these `params` and `form-params` to eliminate these problems.
  Date and time fields also need massaging."
  ([request entity]
   (let
     [params (:params request)
      form-params (:form-params request)
      p (reduce
         merge
         {}
         (map
          #(massage-value % params)
          (keys params)))]
     (if
       (empty? (keys form-params))
       p
       (reduce
        merge
        ;; do the keyfields first, from params
        p
        ;; then merge in everything from form-params, potentially overriding what
        ;; we got from params.
        (map
         #(massage-value % form-params)
         (keys form-params))))))
  ([request]
   (raw-massage-params request nil)))


(def massage-params
  "Sending empty strings, or numbers as strings, to the database often isn't
  helpful. Massage these `params` and `form-params` to eliminate these problems.
  We must take key field values out of just params, but we should take all other
  values out of form-params - because we need the key to load the form in
  the first place, but just accepting values of other params would allow spoofing."
  (memo/ttl raw-massage-params {} :ttl/threshold 5000))


(defn
  raw-resolve-template
  [n]
  (if
    (.exists (io/as-file (str "resources/templates/" n)))
    n
    (str "auto/" n)))


(def resolve-template (memoize raw-resolve-template))


(defmacro compose-exception-reason
  "Compose and return a sensible reason message for this `exception`."
  ([exception intro]
   `(str
     ~intro
     (if ~intro ": ")
     (join
      "\n\tcaused by: "
      (reverse
       (loop [ex# ~exception result# ()]
         (if-not (nil? ex#)
           (recur
            (.getCause ex#)
            (cons (str
                   (.getName (.getClass ex#))
                   ": "
                   (.getMessage ex#)) result#))
           result#))))))
  ([exception]
   `(compose-exception-reason ~exception nil)))


(defmacro compose-reason-and-log
  "Compose a reason message for this `exception`, log it (with its
  stacktrace), and return the reason message."
  ([exception intro]
   `(let [reason# (compose-exception-reason ~exception ~intro)]
      (clojure.tools.logging/error
       reason#
       "\n"
       (with-out-str
         (-> ~exception .printStackTrace)))
      reason#))
  ([exception]
   `(compose-reason-and-log ~exception nil)))


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
       (compose-reason-and-log any# ~message)
       ~error-return)))


(defmacro do-or-return-reason
  "Clojure stacktraces are unreadable. We have to do better; evaluate
  this `form` in a try-catch block; return a map. If the evaluation
  succeeds, the map will have a key `:result` whose value is the result;
  otherwise it will have a key `:error` which will be bound to the most
  sensible error message we can construct."
  ([form intro]
  `(try
     {:result ~form}
     (catch Exception any#
       {:error (compose-exception-reason any# ~intro)})))
  ([form]
   `(do-or-return-reason ~form nil)))


(defmacro do-or-log-and-return-reason
  "Clojure stacktraces are unreadable. We have to do better; evaluate
  this `form` in a try-catch block; return a map. If the evaluation
  succeeds, the map will have a key `:result` whose value is the result;
  otherwise it will have a key `:error` which will be bound to the most
  sensible error message we can construct. Additionally, log the exception"
  [form]
  `(try
     {:result ~form}
     (catch Exception any#
       {:error (compose-reason-and-log any#)})))


(defmacro do-or-warn
  "Evaluate this `form`; if any exception is thrown, show it to the user
  via the `*warn*` mechanism."
  ([form]
   `(try
      ~form
      (catch Exception any#
        (*warn* (compose-exception-reason any#))
        nil)))
  ([form intro]
   `(try
      ~form
      (catch Exception any#
        (*warn* (str ~intro ":\n\t" (compose-exception-reason any#)))
        nil))))


(defmacro do-or-warn-and-log
  "Evaluate this `form`; if any exception is thrown, log the reason and
  show it to the user via the `*warn*` mechanism."
  ([form]
   `(try
      ~form
      (catch Exception any#
        (*warn* (compose-reason-and-log any#))
        nil)))
  ([form intro]
   `(try
      ~form
      (catch Exception any#
        (*warn* (compose-reason-and-log any# ~intro ))
        nil))))

