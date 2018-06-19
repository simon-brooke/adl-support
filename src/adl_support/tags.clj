(ns adl-support.tags
    (:require selmer.node
            [selmer.filter-parser :refer [split-filter-val
                                          safe-filter
                                          compile-filter-body
                                          fix-accessor
                                          get-accessor]]
            [selmer.filters :refer [filters]]
            [selmer.util :refer :all]
            [json-html.core :refer [edn->html]])
  (:import [selmer.node INode TextNode FunctionNode]))

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


(defn if-writable-handler [params tag-content render rdr]
  "If the current element is writable by the current user, emit the content of
  the if clause; else emit the content of the else clause."
  (let [{if-tags :ifwritable else-tags :else} (tag-content rdr :ifwritable :else :endifwritable)]
    params))
