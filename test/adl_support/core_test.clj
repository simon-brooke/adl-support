(ns adl-support.core-test
  (:require [clojure.test :refer :all]
            [adl-support.core :refer :all]))

(deftest query-string-to-map-tests
  (testing "conversion of query strings to maps"
    (let [expected {}
          actual (query-string-to-map nil)]
      (is (= expected actual) "Nil arg"))
    (let [expected {}
          actual (query-string-to-map "")]
      (is (= expected actual) "Empty string arg"))
    (let [expected {:id 1}
          actual (query-string-to-map "id=1")]
      (is (= expected actual) "One integer value"))
    (let [expected {:name "simon"}
          actual (query-string-to-map "name=simon")]
      (is (= expected actual) "One string value."))
    (let [expected {:name "simon" :id 1}
          actual (query-string-to-map "id=1&name=simon")]
      (is (= expected actual) "One string value, one integer. Order of pairs might be reversed, and that's OK"))
    ))
