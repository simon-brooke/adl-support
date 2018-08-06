(ns adl-support.core-test
  (:require [clojure.test :refer :all]
            [adl-support.core :refer :all]))

(deftest massage-params-tests
  (testing "Massaging of params"
    (let [expected {:id 67 :offset 0 :limit 50}
          actual (massage-params {:params {:id "67" :offset "0" :limit "50"} :form-params {}})]
      (is (= expected actual) "Request with no form params"))
    (let [expected {:id 67 :offset 0 :limit 50}
          actual (massage-params {:params {:id "0" :offset "1000" :limit "150"}
                                  :form-params {:id "67" :offset "0" :limit "50"}})]
      (is (= expected actual) "Request with form params, params and form params differ"))))


(deftest compose-exception-reason-tests
  (testing "Compose exception reason"
    (let [expected "java.lang.Exception: hello"
          actual (compose-exception-reason
                  (Exception. "hello"))]
      (is (= expected actual) "Exception with no cause"))
    (let [expected "java.lang.Exception: Top-level exception\n\tcaused by: java.lang.Exception: cause"
          actual (compose-exception-reason
                  (Exception.
                   "Top-level exception"
                   (Exception. "cause")))]
      (is (= expected actual) "Exception with cause"))
    (let [expected ""
          actual (compose-exception-reason nil)]
      (is (= expected actual) "Exception with no cause"))))


(deftest do-or-return-reason-tests
  (testing "do-or-return-reason"
    (let [expected {:result 1}
          actual (do-or-return-reason (/ 1 1))]
      (is (= expected actual) "No exception thrown"))
    (let [expected {:error "java.lang.ArithmeticException: Divide by zero"}
          actual (do-or-return-reason (/ 1 0))]
      (is (= expected actual) "Exception thrown"))
    (let [expected {:error "Hello: java.lang.ArithmeticException: Divide by zero"}
          actual (do-or-return-reason (/ 1 0) "Hello")]
      (is (= expected actual) "Exception thrown, with intro"))))


;; These work in REPL, but break in tests. Why?
;; (deftest "do-or-warn-tests"
;;   (testing "do-or-warn"
;;     (let [expected 1
;;           actual (do-or-warn (/ 1 1))]
;;       (is (= expected actual) "No exception thrown"))
;;     (let [expected nil
;;           actual (do-or-warn (/ 1 0))]
;;       (is (= expected actual) "Exception thrown"))
;;     (let [expected nil
;;           actual (do-or-warn (/ 1 0) "hello")]
;;       (is (= expected actual) "Exception thrown"))
;;     ))
