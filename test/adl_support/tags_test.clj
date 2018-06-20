(ns adl-support.tags-test
  (:require [clojure.test :refer :all]
            [adl-support.tags :refer :all]
            [selmer.parser :as parser]))

(add-tags)

(deftest if-member-of-tests
  (testing "testing the if-member-of tag"
    (let [expected "boo"
          actual (if-member-of-permitted nil nil "caramba" "boo")]
      (is (= expected actual) "Nil args, nil "))
    (let [expected "boo"
          actual (if-member-of-permitted nil {:user-roles #{"admin"}} "caramba" "boo")]
      (is (= expected actual) "Nil args, one user-group"))
    (let [expected "boo"
          actual (if-member-of-permitted '("public") {:user-roles #{"admin"}} "caramba" "boo")]
      (is (= expected actual) "One arg, one non-matching user-group"))
    (let [expected "caramba"
          actual (if-member-of-permitted '("admin") {:user-roles #{"admin"}} "caramba" "boo")]
      (is (= expected actual) "One arg, one matching user-group"))
    (let [expected "boo"
          actual (if-member-of-permitted '("public") {:user-roles #{"admin" "canvassers"}} "caramba" "boo")]
      (is (= expected actual) "One arg, two non-matching user-roles"))
    (let [expected "caramba"
          actual (if-member-of-permitted '("admin") {:user-roles #{"admin" "canvassers"}} "caramba" "boo")]
      (is (= expected actual) "One arg, two user-roles, first one matching"))
    (let [expected "caramba"
          actual (if-member-of-permitted '("admin") {:user-roles #{"canvassers" "admin"}} "caramba" "boo")]
      (is (= expected actual) "One arg, two user-roles, second one matching"))
    (let [expected "caramba"
          actual (if-member-of-permitted '("admin" "public") {:user-roles #{"admin"}} "caramba" "boo")]
      (is (= expected actual) "Two args, one user-group, first arg matches"))
    (let [expected "caramba"
          actual (if-member-of-permitted '("public" "admin") {:user-roles #{"admin"}} "caramba" "boo")]
      (is (= expected actual) "Two args, one user-group, second arg matches"))
    (let [expected "not-permitted"
          actual (parser/render
                   "{% ifmemberof public canvassers %}permitted{% else %}not-permitted{% endifmemberof %}"
                   {:user-roles #{"admin"}})]
      (is (= expected actual) "Two args, one non-matching user-group"))
    (let [expected "permitted"
          actual (parser/render
                   "{% ifmemberof public canvassers %}permitted{% else %}not-permitted{% endifmemberof %}"
                   {:user-roles #{"canvassers"}})]
      (is (= expected actual) "Two args, one matching user-group"))
    ))



