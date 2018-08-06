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


(deftest prepare-insertion-params-tests
  (testing "prepare-insertion-params"
    (is (= {:test1 nil :test2 nil}
           (prepare-insertion-params {} #{:test1 :test2}))
        "Empty params; set")
    (is (= {:test1 nil :test2 nil}
           (prepare-insertion-params {} '(:test1 :test2)))
        "Empty params; list")
    (is (= {:test1 nil :test2 nil :test3 6}
           (prepare-insertion-params {:test3 6} #{:test1 :test2}))
        "Unlisted param; set")
    (is (= {:test1 "foo" :test2 nil}
           (prepare-insertion-params {:test1 "foo"} '(:test1 :test2)))
        "Listed param; list")
    (is (= {:test1 "foo" :test2 6}
           (prepare-insertion-params {:test1 "foo" :test2 6} '(:test1 :test2)))
        "Listed params; list")))
