(ns ^{:doc "Application Description Language support - custom Selmer tags used
      in generated templates."
      :author "Simon Brooke"}
  adl-support.tags
    (:require [selmer.parser :as p]))

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
              :endifmemberof))

(add-tags)
