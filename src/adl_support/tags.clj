(ns ^{:doc "Application Description Language support - custom Selmer tags used
      in generated templates."
      :author "Simon Brooke"}
  adl-support.tags
  (:require [clojure.string :refer [split]]
            [selmer.parser :as p]
            [selmer.tags :as t]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;
;;;; adl-support.tags: selmer tags required by ADL selmer views.
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

(defn if-member-of-permitted
  "If at least one of these `args` matches some group name in the `:user-roles`
  of this `context`, return this `success`, else this `failure`."
  [args context success failure]
  (if
    (and
      (seq? args)
      (set? (:user-roles context))
      (some (:user-roles context) args))
    success
    failure))


(defn parse-arg
  [arg context]
  (cond
    (number? arg)
    arg
    (number? (read-string arg))
    (read-string arg)
    (= \" (first arg))
    (.substring arg 1 (dec (.length arg)))
    (and (= \: (first arg)) (> (count arg) 1))
    (keyword (subs arg 1))
    :else
    (get-in context (map keyword (split arg #"\.")))))


(defn add-tags []
  "Add custom tags required by ADL-generated code to the parser's tags."
  (p/add-tag! :ifmemberof
              (fn [args context content]
                (if-member-of-permitted
                  args context
                  (get-in content [:ifmemberof :content])
                  (get-in content [:else :content])))
              :else
              (fn [args context content]
                "")
              :endifmemberof)
  (p/add-tag! :ifcontains
              (fn [[c v] context content]
                (let [value (parse-arg v context)
                      collection (parse-arg c context)]
                  (if
                    (some
                      #(= % value)
                      collection)
                    (get-in content [:ifcontains :content])
                    (get-in content [:else :content]))))
              :else
              (fn [args context content]
                "")
              :endifcontains))


(add-tags)



