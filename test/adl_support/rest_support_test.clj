(ns adl-support.core-test
  (:require [clojure.test :refer :all]
            [adl-support.rest_support :refer :all]))


(deftest if-valid-user-tests
  (testing "correct handling of if-valid-user"
    (let [expected "hello"
          actual (if-valid-user "hello" {:session {:user {:id 4}}} "goodbye")]
      (is (= expected actual) "User in session"))
    (let [expected "goodbye"
          actual (if-valid-user "hello" {:session {}} "goodbye")]
      (is (= expected actual) "No user in session"))))


(deftest valid-user-or-forbid-tests
  (testing "valid-user-or-forbid"
    (let [expected "hello"
          actual (valid-user-or-forbid "hello" {:session {:user {:id 4}}})]
      (is (= expected actual) "User in session"))
    (let [expected 403
          actual (:status (valid-user-or-forbid "hello" {:session {:user {:id 4}}}))]
      (is (= expected actual) "No user in session"))))


(deftest with-params-or-error-tests
  (let [expected "hello"
        actual (with-params-or-error "hello" {:a 1 :b 2} #{:a :b})]
    (is (= expected actual) "All requirements satisfied"))
  (let [expected "hello"
        actual (with-params-or-error "hello" {:a 1 :b 2 :c 3} #{:a :b})]
    (is (= expected actual) "Unrequired parameter present"))
  (let [expected 400
        actual (:status (with-params-or-error "hello" {:a 1 :b 2} #{:a :b :c}))]
    (is (= expected actual) "Some requirements unsatisfied"))
  (let [expected 400
        actual (:status (with-params-or-error (/ 1 0) {:a 1 :b 2} #{:a :b :c}))]
    (is (= expected actual) "Exception should not be throwen")))
