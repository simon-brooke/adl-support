(ns ^{:doc "Application Description Language support - functions useful in
      generating forms."
      :author "Simon Brooke"}
  adl-support.forms-support
  (:require [adl-support.core :refer :all]
            [adl-support.utils :refer [capitalise descendants-with-tag safe-name singularise]]
            [clojure.core.memoize :as memo]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :refer [join lower-case]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;
;;;; adl-support.forms-support: functions used by ADL-generated code:
;;;; support functions for HTML forms.
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


(defn query-name
  "Generate a query name for the query of type `q-type` (expected to be one
  of `:create`, `:delete`, `:get`, `:list`, `:search-strings`, `:update`) of
  the entity `entity-or-name` NOTE: if `entity-or-name` is passed as a string,
  it should be the full, unaltered name of the entity."
  [entity-or-name q-type]
  (symbol
   (str
    "db/"
    (lower-case (name q-type))
    "-"
    (let [n (safe-name
             (if
               (string? entity-or-name)
               entity-or-name
               (:name (:attrs entity-or-name))) :sql)]
      (case q-type
        (:list :search-strings) n
        (singularise n)))
    (case q-type
      (:create :delete :update) "!"
      nil))))


(defmacro get-current-value
  "Use the function `f` and these `params` to fetch an `entity` record from the database."
  [f params entity-name]
  `(let
     [message# (str "Error while fetching " ~entity-name " record " ~params)]
     (support/do-or-log-error
      (~f  db/*db* ~params)
      :message message#
      :error-return {:warnings [message#]})))


(defmacro get-menu-options
  "Fetch options for a menu of `entity-name` from the database, using this
  `get-q` query and this `list-q` query, using the key `fk`, where the current
  value is this `value`."
  [entity-name get-q list-q fk value]
  `(remove
    nil?
    (flatten
     (list
      (if
        ~value
        (do-or-log-error
         (apply
          ~get-q
          (list db/*db* {~fk ~value}))
         :message
         (str "Error while fetching " ~entity-name " record '" ~value "'")))
      (do-or-log-error
       (apply
        ~list-q
        (list db/*db*)
        {})
       :message
       (str "Error while fetching " ~entity-name " list"))))))


(defmacro auxlist-data-name
  "The name to which data for this `auxlist` will be bound in the
  Selmer params."
 [auxlist]
 `(safe-name (-> ~auxlist :attrs :property) :clojure))


(defmacro all-keys-present?
  "Return true if all the keys in `keys` are present in the map `m`."
  [m keys]
  `(clojure.set/subset? (set ~keys) (set (keys ~m))))


(defmacro prepare-insertion-params
  "Params for insertion into the database must have keys for all fields in the
  insert query, even if the value of some of those keys is nil. Massage these
  `params` to have a value for each field in these `fields`."
  ;; TODO: should intelligently handle dates and times, but that might imply
  ;; access to ADL at runtime!
  [params fields]
  `(merge
    (reduce merge {} (map #(hash-map (keyword %) nil) ~fields))
    ~params))


(defn property-defaults
  "Get a map of property names and default values for all properties of this
  `entity` which have explicit defaults."
  [entity]
  (reduce
    merge {}
    (map
      #(hash-map (keyword (-> % :attrs :name)) (-> % :attrs :default))
      (descendants-with-tag entity :property #(-> % :attrs :default)))))


(defn form-title
  "Construct an appropriate title for a form having this `form-name`, for an
  entity having these `user-distinct-property-keys`, given this `record`."
  [record form-name user-distinct-property-keys]
  (str
    form-name
    ": "
    (join
      ", "
      (remove
        nil?
        (map
          record
          user-distinct-property-keys)))))


