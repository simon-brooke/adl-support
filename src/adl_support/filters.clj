(ns ^{:doc "Application Description Language support - custom Selmer filters
      used in generated templates."
      :author "Simon Brooke"}
  adl-support.filters
    (:require [clojure.string :as s]
              [selmer.filters :as f]
              [selmer.parser :as p]
              [selmer.tags :as t]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;
;;;; adl-support.filters: selmer filters required by ADL selmer views.
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


(def ^:dynamic *default-international-dialing-prefix*
  "The international dialing prefix to use, if none is specified."
  "44")

(defn telephone
  "If `arg` is, or appears to be, a valid telephone number, convert it into
  a `tel:` link, else leave it be."
  [^String arg]
  (let [number
        (s/replace
         (s/replace
          arg
          #"^0"
          (str "+" *default-international-dialing-prefix* "-"))
         #"\s+" "-")]
    (if (re-matches #"[0-9 +-]*" arg)
      [:safe (str "<a href='tel:" number "'>" arg "</a>")]
      arg)))


;; (telephone "07768 130255")
;; (telephone "Freddy")

(defn email
  "If `arg` is, or appears to be, a valid email address, convert it into
  a `mailto:` link, else leave it be."
  [^String arg]
  (if (re-matches #"^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,6}$" arg)
    [:safe (str "<a href='mailto:" arg "'>" arg "</a>")]
    arg))


;; (email "simon@journeyman.cc")
;; (email "simon@journeyman")
;; (email "simon@journeyman.cc.")

(f/add-filter! :telephone telephone)

(f/add-filter! :email email)

;; (p/render "{{p|telephone}}" {:p "07768 130255"})

(defn contains
  [collection value]
  (first
    (filter
      #(= % value)
      collection)))

;; (contains '(:a :b :c) :a)

(f/add-filter! :contains contains)

;; (p/render "{{l|contains:\"foo\"}}" {:l ["froboz" "bar"]})

;; (p/render "{% if l|contains:\"foo\" %}I see ya!{% else %}I don't{% endif %}"  {:l ["foo" "bar"]})
;; (p/render "{% if l|contains:\"foo\" %}I see ya!{% else %}I don't{% endif %}"  {:l ["froboz" "bar"]})

;; (p/render
;;    "<option value='{{option.id}}' {% if record.roles|contains:option.id %}selected='selected'{% endif %}>{{option.name}}</option>"
;;    {:option {:id 2 :name "Fred"} :record {:roles [2 6]}})

