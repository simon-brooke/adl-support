
(ns adl-support.forms-support-test
  (:require [clojure.test :refer :all]
            [adl-support.forms-support :refer :all]))


(deftest auxlist-data-name-test
  (testing "auxlist-data-name"
    (let [auxlist {:tag :auxlist,
                   :attrs {:property "dwellings"},
                   :content [{:tag :field,
                              :attrs {:name "sub-address"},
                              :content nil}]}
          expected "auxlist-dwellings"
          actual (auxlist-data-name auxlist)]
      (is (= expected actual) "Just checking..."))))
