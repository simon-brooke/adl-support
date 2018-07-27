(ns adl-support.rest-support
  (:require [clojure.core.memoize :as memo]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :refer [split]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;
;;;; adl-support.core: functions used by ADL-generated code: REST support.
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


(defmacro if-valid-user
  "Evaluate this `form` only if there is a valid user in the session of
  this `request`; otherwise return the `error-return` value."
  ;; TODO: candidate for moving to adl-support.core
  ([form request error-return]
   `(log/debug "if-valid-user: " (-> ~request :session :user))
   `(if
      (-> ~request :session :user)
      ~form
      ~error-return))
  ([form request]
   (if-valid-user form request nil)))


(defmacro valid-user-or-forbid
  "Evaluate this `form` only if there is a valid user in the session of
  this `request`; otherwise return an HTTP forbidden response."
  ;; TODO: candidate for moving to adl-support.core
  [form request]
  `(if-valid-user
    ~form
    ~request
    {:status 403
     :body (json/write-str "You must be logged in to do that")}))


(defmacro with-params-or-error
  "Evaluate this `form` only if these `params` contain all these `required` keys;
  otherwise return an HTTP 400 response."
  ;; TODO: candidate for moving to adl-support.core
  [form params required]
  `(if-not
     (some #(not (% ~params)) ~required)
     ~form
     {:status 400
      :body (json/write-str (str "The following params are required: " ~required))}))


;; (with-params-or-error (/ 1 0) {:a 1 :b 2} #{:a :b :c})
;; (with-params-or-error "hello" {:a 1 :b 2} #{:a :b })

(defmacro do-or-server-fail
  "Evaluate this `form`; if it succeeds, return an HTTP response with this
  status code and the JSON-formatted result as body; if it fails, return an
  HTTP 500 response."
  [form status]
  `(let [r# (do-or-return-reason ~form)]
     (if
       (some #(= :result %) (keys r#)) ;; :result might legitimately be bound to nil
       {:status ~status
        :body (:result r#)}
       {:status 500
             :body r#})))


