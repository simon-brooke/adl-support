(ns ^{:doc "Application Description Language support - utility functions."
      :author "Simon Brooke"}
  adl-support.utils
  (:require [adl-support.core :refer [*warn*]]
            [clojure.math.numeric-tower :refer [expt]]
            [clojure.pprint :as p]
            [clojure.string :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;
;;;; adl-support.utils: utility functions.
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

;; TODO: this really ought to be split into several namespaces

(def ^:dynamic  *locale*
  "The locale for which files will be generated."
  "en_GB.UTF-8")

(def ^:dynamic *output-path*
  "The path to which generated files will be written."
  "generated/")

(def ^:dynamic *verbosity*
  "The verbosity of output from the generator."
  0)


(defn element?
  "True if `o` is a Clojure representation of an XML element."
  [o]
  (and (map? o) (:tag o) (:attrs o)))


(defmacro entity?
  "True if `o` is a Clojure representation of an ADL entity."
  [o]
  `(= (:tag ~o) :entity))


(defn wrap-lines
  "Wrap lines in this `text` to this `width`; return a list of lines."
  ;; Shamelessly adapted from https://www.rosettacode.org/wiki/Word_wrap#Clojure
  ([width text]
   (s/split-lines
     (p/cl-format
       nil
       (str "~{~<~%~1," width ":;~A~> ~}")
       (clojure.string/split text #" "))))
  ([text]
   (wrap-lines 76 text)))


(defn emit-header
  "Emit this `content` as a sequence of wrapped lines each prefixed with
  `prefix`, and the whole delimited by rules."
  [prefix & content]
  (let [comment-rule (apply str (repeat 70 (last prefix)))
        p (str "\n" prefix "\t") ]
    (str
     prefix
     comment-rule
     p
     (s/join
      p
      (flatten
       (interpose
        ""
         (map
          #(wrap-lines 70 (str %))
          (flatten content)))))
     "\n"
     prefix
     comment-rule)))


(defn sort-by-name
  "Sort these `elements` by their `:name` attribute."
  [elements]
  (sort #(compare (:name (:attrs %1)) (:name (:attrs %2))) elements))


(defn children
  "Return the children of this `element`; if `predicate` is passed, return only those
  children satisfying the predicate."
  ([element]
   (if
     (keyword? (:tag element)) ;; it has a tag; it seems to be an XML element
     (:content element)))
  ([element predicate]
   (filter
     predicate
     (children element))))


(defn child
  "Return the first child of this `element` satisfying this `predicate`."
  [element predicate]
  (first (children element predicate)))


(defn attributes
  "Return the attributes of this `element`; if `predicate` is passed, return only those
  attributes satisfying the predicate."
  ([element]
   (if
     (keyword? (:tag element)) ;; it has a tag; it seems to be an XML element
     (:attrs element)))
  ([element predicate]
   (filter
     predicate
     (attributes element))))


(defn children-with-tag
  "Return all children of this `element` which have this `tag`;
  if `element` is `nil`, return `nil`. If `predicate` is supplied,
  return only those children with the specified `tag` which satisfy
  the `predicate`."
  ([element tag]
  (if
    element
    (children element #(= (:tag %) tag))))
  ([element tag predicate]
   (filter
     predicate
     (children-with-tag element tag))))


(defn child-with-tag
  "Return the first child of this `element` which has this `tag`;
  if `element` is `nil`, return `nil`. If `predicate` is supplied,
  return only the first child with the specified `tag` which satisfies
  the `predicate`."
  ([element tag]
   (first (children-with-tag element tag)))
  ([element tag predicate]
   (first (children-with-tag element tag predicate))))


(defn typedef
  "If this `property` is of type `defined`, return its type definition from
  this `application`, else nil."
  [property application]
  (if
    (= (:type (:attrs property)) "defined")
    (child
     application
     #(and
       (= (:tag %) :typedef)
       (= (:name (:attrs %)) (:typedef (:attrs property)))))))


(defn permission-groups
  "Return a list of names of groups to which this `predicate` is true of
  some permission taken from these `permissions`, else nil."
  [permissions predicate]
  (let [groups (remove
                 nil?
                 (map
                   #(if
                      (apply predicate (list %))
                      (:group (:attrs %)))
                   permissions))]
    (if groups groups)))


(defn formal-primary-key?
  "Does this `prop-or-name` appear to be a property (or the name of a property)
  which is a formal primary key of this entity?"
  [prop-or-name entity]
  (if
    (map? prop-or-name)
    (formal-primary-key? (:name (:attrs prop-or-name)) entity)
    (let [primary-key (first (children entity #(= (:tag %) :key)))
          property (first
                     (children
                       primary-key
                       #(and
                          (= (:tag %) :property)
                          (= (:name (:attrs %)) prop-or-name))))]
      (= (:distinct (:attrs property)) "system"))))


(defn entity?
  "Return true if `x` is an ADL entity."
  [x]
  (= (:tag x) :entity))


(defn property?
  "True if `o` is a property."
  [o]
  (= (:tag o) :property))


(defn entity-for-property
  "If this `property` references an entity, return that entity from this `application`"
  [property application]
  (if
    (and (property? property) (:entity (:attrs property)))
    (child
      application
      #(and
         (entity? %)
         (= (:name (:attrs %))(:entity (:attrs property)))))))


(defn visible-to
  "Return a list of names of groups to which are granted read access,
  given these `permissions`, else nil."
  [permissions]
  (permission-groups permissions #(#{"read" "insert" "noedit" "edit" "all"} (:permission (:attrs %)))))


(defn writeable-by
  "Return a list of names of groups to which are granted write access,
  given these `permissions`, else nil.
  TODO: TOTHINKABOUT: properties are also writeable by `insert` and `noedit`, but only if the
  current value is nil."
  ([permissions]
   (writeable-by permissions true))
  ([permissions has-value?]
  (let
    [privileges (if has-value? #{"edit" "all"} #{"edit" "all" "insert" "noedit"})]
  (permission-groups permissions #(privileges (:permission (:attrs %)))))))


(defn singularise
  "Attempt to construct an idiomatic English-language singular of this string."
  [string]
  (cond
    (.endsWith string "ss") string
    (.endsWith string "ise") string
    true
    (s/replace
      (s/replace
        (s/replace
          (s/replace string #"_" "-")
          #"s$" "")
        #"se$" "s")
      #"ie$" "y")))


(defn capitalise
  "Return a string like `s` but with each token capitalised."
  [s]
  (if
    (string? s)
    (s/join
      " "
      (map
        s/capitalize
        (s/split s #"[^a-zA-Z0-9]+")))
    s))


(defn pretty-name
  "Return a version of the name of this `element` (entity, field,
  form, list, page, property) suitable for use in text visible to the user."
  [element]
  (capitalise (singularise (:name (:attrs element)))))


(defn safe-name
  "Return a safe name for the object `o`, given the specified `convention`.
  `o` is expected to be either a string or an element. Recognised values for
  `convention` are: #{:c :c-sharp :clojure :java :sql}"
  ([o]
   (cond
     (element? o)
     (safe-name (:name (:attrs o)))
     true
     (s/replace (str o) #"[^a-zA-Z0-9-]" "")))
  ([o convention]
   (cond
     (and (entity? o) (= convention :sql))
     ;; if it's an entity, it's permitted to have a different table name
     ;; from its entity name. This isn't actually likely, but...
     (safe-name (or (-> o :attrs :table) (-> o :attrs :name)) :sql)
     (and (property? o) (= convention :sql))
     ;; if it's a property, it's entitle to have a different column name
     ;; from its property name.
     (safe-name (or (-> o :attrs :column) (-> o :attrs :name)) :sql)
     (element? o)
     (safe-name (:name (:attrs o)) convention)
     true
     (let [string (str o)
           capitalised (capitalise string)]
       (case convention
         (:sql :c) (s/replace string #"[^a-zA-Z0-9_]" "_")
         :clojure (s/replace string #"[^a-zA-Z0-9-]" "-")
         :c-sharp (s/replace capitalised #"[^a-zA-Z0-9]" "")
         :java (let
                 [camel (s/replace capitalised #"[^a-zA-Z0-9]" "")]
                 (apply str (cons (Character/toLowerCase (first camel)) (rest camel))))
         (safe-name string))))))

;; (safe-name "address-id" :sql)
;; (safe-name {:tag :property :attrs {:name "address-id"}} :sql)


(defn unique-link?
  "True if there is exactly one link between entities `e1` and `e2`."
  [e1 e2]
  (let [n1 (count (children-with-tag e1 :property
                                     #(and (= (-> % :attrs :type) "link")
                                           (= (-> % :attrs :entity)(-> e2 :attrs :name)))))
        n2 (count (children-with-tag e2 :property
                                     #(and (= (-> % :attrs :type) "link")
                                           (= (-> % :attrs :entity)(-> e1 :attrs :name)))))]
    (= (max n1 n2) 1)))


(defn link-table-name
  "Canonical name of a link table between entity `e1` and entity `e2`. However, there
  may be different links between the same two tables with different semantics; if
  `property` is specified, and if more than one property in `e1` links to `e2`, generate
  a more specific link name."
  ([e1 e2]
   (s/join
     "_"
     (cons
       "ln"
       (sort
         (list
           (:name (:attrs e1)) (:name (:attrs e2)))))))
  ([property e1 e2]
   (if (unique-link? e1 e2)
     (link-table-name e1 e2)
     (s/join
       "_" (cons "ln" (map #(:name (:attrs %)) (list property e1)))))))


(defn property-for-field
  "Return the property within this `entity` which matches this `field`."
  [field entity]
  (child-with-tag
    entity
    :property
    #(=
       (-> field :attrs :property)
       (-> % :attrs :name))))


(defn prompt
  "Return an appropriate prompt for the given `field-or-property` taken from this
  `form` of this `entity` of this `application`, in the context of the current
  binding of `*locale*`. TODO: something more sophisticated about i18n"
  [field-or-property form entity application]
  (let [property (case (:tag field-or-property)
                   :property field-or-property
                   :field (property-for-field field-or-property entity)
                   nil)]
    (capitalise
      (or
        (:prompt
          (:attrs
            (child-with-tag
              field-or-property
              :prompt
              #(= (:locale (:attrs %)) *locale*))))
        (:prompt
          (:attrs
            (child-with-tag
              property
              :prompt
              #(= (:locale (:attrs %)) *locale*))))
        (:name (:attrs property))
        (:property (:attrs field-or-property))
        "Missing prompt"))))


(defmacro properties
  "Return all the properties of this `entity`."
  [entity]
  `(children-with-tag ~entity :property))


(defn descendants-with-tag
  "Return all descendants of this `element`, recursively, which have this `tag`.
  If `predicate` is specified, return only those also satisfying this `predicate`."
  ([element tag]
   (flatten
     (remove
       empty?
       (cons
         (children element #(= (:tag %) tag))
         (map
           #(descendants-with-tag % tag)
           (children element))))))
  ([element tag predicate]
   (filter
     predicate
     (descendants-with-tag element tag))))


(defn descendant-with-tag
  "Return the first descendant of this `element`, recursively, which has this `tag`.
  If `predicate` is specified, return the first also satisfying this `predicate`."
  ([element tag]
   (first (descendants-with-tag element tag)))
  ([element tag predicate]
   (first (descendants-with-tag element tag predicate))))


(defn find-permissions
  "Return appropriate the permissions of the first of these `elements` which
  has permissions."
  [& elements]
  (first
    (remove
      empty?
      (map
        #(children-with-tag % :permission)
        elements))))


(defn system-generated?
  "True if the value of the `property` is system generated, and
  should not be set by the user."
  [property]
  (child-with-tag
          property
          :generator
          #(#{"native" "guid"} (-> % :attrs :action))))


(defn insertable?
  "Return `true` it the value of this `property` may be set from user-supplied data."
  [property]
  (and
    (= (:tag property) :property)
    (not (#{"link" "list"} (:type (:attrs property))))
    (not (system-generated? property))))


(defmacro all-properties
  "Return all properties of this `entity` (including key properties)."
  [entity]
  `(descendants-with-tag ~entity :property))


(defn user-distinct-properties
  "Return the properties of this `entity` which are user distinct"
  [entity]
  (filter #(#{"user" "all"} (:distinct (:attrs %))) (all-properties entity)))


(defn user-distinct-property-names
  "Return, as a set, the names of properties which are user distinct"
  [entity]
  (set
   (map
    (fn [x] (-> x :attrs :name))
    (user-distinct-properties entity))))


(defn column-name
  "Return, as a string, the name for the column which represents this `property`."
  [property]
  (safe-name
    (or (-> property :attrs :column) (-> property :attrs :name))
    :sql))


(defmacro insertable-properties
  "Return all the properties of this `entity` (including key properties) into
  which user-supplied data can be inserted"
  [entity]
  `(filter
     insertable?
     (all-properties ~entity)))


(defn required-properties
  "Return the properties of this `entity` which are required and are not
  system generated."
  [entity]
  (filter
    #(and
       (= (:required (:attrs %)) "true")
       (not (system-generated? %)))
    (descendants-with-tag entity :property)))


(defmacro key-properties
  [entity]
  `(children-with-tag (first (children-with-tag ~entity :key)) :property))


(defmacro insertable-key-properties
  [entity]
  `(filter insertable? (key-properties entity)))


(defn link-table?
  "Return true if this `entity` represents a link table."
  [entity]
  (let [properties (all-properties entity)
        links (filter #(-> % :attrs :entity) properties)]
    (= (count properties) (count links))))


(defn key-names
  ([entity]
  (set
    (remove
      nil?
      (map
        #(:name (:attrs %))
        (key-properties entity)))))
  ([entity as-keywords?]
   (let [names (key-names entity)]
     (if
       as-keywords?
       (set (map keyword names))
       names))))


(defn base-type
  [property application]
  (cond
    (:typedef (:attrs property))
    (:type
      (:attrs
        (child
          application
          #(and
             (= (:tag %) :typedef)
             (= (:name (:attrs %)) (:typedef (:attrs property)))))))
    (:entity (:attrs property))
    (:type
      (:attrs
        (first
          (key-properties
            (child
              application
              #(and
                 (= (:tag %) :entity)
                 (= (:name (:attrs %)) (:entity (:attrs property)))))))))
    true
    (:type (:attrs property))))


(defn is-quotable-type?
  "True if the value for this field should be quoted."
  [property application]
  (#{"date" "image" "string" "text" "time" "timestamp" "uploadable"} (base-type property application)))


(defn has-primary-key? [entity]
  (> (count (key-names entity)) 0))


(defn has-non-key-properties? [entity]
  (>
    (count (all-properties entity))
    (count (key-properties entity))))


(defn distinct-properties
  [entity]
  (filter
    #(#{"system" "all"} (:distinct (:attrs %)))
    (properties entity)))


(defn list-related-query-name
  "Return the canonical name of the HugSQL query to return all records on
  `farside` which match a given record on `nearside`, where `nearide` and
  `farside` are both entities; and `property` is the nearside property on
  which to join."
  ([property nearside farside as-symbol?]
   (let [unique? (=
                   (count
                     (filter
                       #(= (-> % :attrs :entity)(-> property :attrs :entity))
                       (descendants-with-tag nearside :property)))
                   1)
         farname (if unique? (safe-name farside :sql) (safe-name property :sql))
         nearname (singularise (safe-name nearside :sql))
         n (case (-> property :attrs :type)
             "list" (str "list-" farname "-by-" nearname)
             "link" (s/join "-"
                            (list
                              "list"
                              (safe-name property :sql) "by" nearname))
             "entity" (str "list-" (safe-name nearside :sql) "-by-" (safe-name property :sql))
             ;; default
             (str "ERROR-bad-property-type-"
                  (-> ~property :attrs :type) "-of-"
                  (-> ~property :attrs :name)))]
     (if
       (and
         (property? property)
         (entity? nearside)
         (entity? farside))
       (if
         as-symbol?
         (symbol (str "db/" n))
         n)
       (do
         (*warn*
           (str "Argument "
                (cond
                  (not (entity? nearside)) (or (-> nearside :attrs :name) nearside "nearside")
                  (not (entity? farside)) (or (-> farside :attrs :name) farside "farside"))
                " passed to `list-related-query-name` was a non-entity"))
         nil))))
  ([property nearside farside]
   (list-related-query-name property nearside farside false)))


(defn path-part
  "Return the URL path part for this `form` of this `entity` within this `application`.
  Note that `form` may be a Clojure XML representation of a `form`, `list` or `page`
  ADL element, or may be one of the keywords `:form`, `:list`, `:page` in which case the
  first child of the `entity` of the specified type will be used."
  [form entity application]
  (cond
   (and (map? form) (#{:list :form :page} (:tag form)))
   (s/join
    "-"
    (flatten
     (list
      (name (:tag form)) (:name (:attrs entity)) (s/split (:name (:attrs form)) #"[ \n\r\t]+"))))
   (keyword? form)
   (path-part (first (children-with-tag entity form)) entity application)))


(defn editor-name
  "Return the path-part of the editor form for this `entity`. Note:
  assumes the editor form is the first form listed for the entity."
  [entity application]
  (path-part :form entity application))


(defn type-for-defined
  [property application]
  (:type (:attrs (typedef property application))))


(defn volatility
  "Return the cache ttl in seconds for records of this `entity`."
  [entity]
  (try
    (let
      [v (read-string (:volatility (:attrs entity)))]
      (if
        (zero? v)
        0
        (expt 10 v)))
    (catch Exception _ 0)))


(defn order-preserving-set
  "The Clojure `set` function does not preserve the order in which elements are
  passed to it. This function is like `set`, except
  1. It returns a list, not a hashset, and
  2. It is order-preserving."
  [collection]
  (loop [lhs (list (first collection))
         rhs (rest collection)]
    (cond
      (empty? rhs) (reverse lhs)
      (some #(= (first rhs) %) lhs) (recur lhs (rest rhs))
      true (recur (cons (first rhs) lhs) (rest rhs)))))


(defmacro entity-by-name
  "Return the entity with this `entity-name` in this `application`.
  TODO: Candidate for move to adl-support.utils."
  [entity-name application]
  `(child-with-tag
     ~application
     :entity
     #(= (:name (:attrs %)) ~entity-name)))

