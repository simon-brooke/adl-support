(ns adl-support.print-usage
  (:require [clojure.string :refer [join]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;
;;;; adl-support.print-usage: functions used by ADL-generated code.
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


(defn print-usage
  "Print a UN*X style usage message. `project-name` should be the base name of
  the executable jar file you generate, `parsed-options` should be options as
  parsed by [clojure.tools.cli](https://github.com/clojure/tools.cli). If
  `extra-args` is supplied, it should be a map of name, documentation pairs
  for each additional argument which may be supplied."
  ([project-name parsed-options]
   (print-usage project-name parsed-options {}))
  ([project-name parsed-options extra-args]
   (println
     (join
       "\n"
       (flatten
         (list
           (join " "
                 (concat
                   (list
                     "Usage: java -jar "
                     (str
                       project-name
                       "-"
                       (or (System/getProperty (str project-name ".version")) "[VERSION]")
                       "-standalone.jar")
                     "-options")
                   (map name (keys extra-args))))
           "where options include:"
           (:summary parsed-options)
           (doall
             (map
               #(str "  " (name %) "\t\t" (extra-args %))
               (keys extra-args)))))))))

