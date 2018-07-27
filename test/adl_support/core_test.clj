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
    (let [expected {:address_id_expanded "AIRDS"}
          actual (query-string-to-map "id=&address_id_expanded=AIRDS&sub-address=")]
      (is (= expected actual) "Yeys with no values should not be included in the map"))
    ))

(deftest massage-params-tests
  (testing "Massaging of params"
    (let [expected {:id 67}
          actual (massage-params {:id 67} {} #{:id})]
      (is (= expected actual) "numeric param"))
    (let [expected {:id 67}
          actual (massage-params {:id "67"} {} #{:id})]
      (is (= expected actual) "string param"))
    (let [expected {:id 67}
          actual (massage-params {"id" "67"} {} #{:id})]
      (is (= expected actual) "string keyword"))
    (let [expected {:id 67}
          actual (massage-params {:id 60} {:id 67} #{:id})]
      (is (= expected actual) "params and form-params differ"))
    (let [expected {:id 67 :offset 0 :limit 50}
          actual (massage-params {:id 60} {:id "67" :offset "0" :limit "50"} #{:id})]
      (is (= expected actual) "prefer values from form-params"))
    (let [expected {:id 67 :offset 0 :limit 50}
          actual (massage-params {:params {:id "67" :offset "0" :limit "50"} :form-params {}})]
      (is (= expected actual) "Request with no form params"))
    (let [expected {:id 67 :offset 0 :limit 50}
          actual (massage-params {:params {:id "0" :offset "1000" :limit "150"}
                                  :form-params {:id "67" :offset "0" :limit "50"}})]
      (is (= expected actual) "Request with form params, params and form params differ"))
      ))
