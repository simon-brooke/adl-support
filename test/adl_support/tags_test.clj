(ns adl-support.tags-test
  (:require [clojure.test :refer :all]
            [adl-support.tags :refer :all]
            [selmer.parser :as parser]))

(add-tags)

(deftest if-member-of-tests
  (testing "the `ifmemberof` tag"
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



(deftest if-contains-tests
  (testing "the `ifcontains` tag"
    (let [expected "Hello!"
          actual (parser/render "{% ifcontains record.roles option.id %}Hello!{% else %}Goodbye!{% endifcontains %}"
          {:option {:id 2 :name "Fred"} :record {:roles [2 6]}})]
      (is (= expected actual)
          "Both args are paths which exist in the context;
          the value of the first contains the value of the second;
          values are numbers"))
    (let [expected "Goodbye!"
          actual (parser/render "{% ifcontains record.roles option.id %}Hello!{% else %}Goodbye!{% endifcontains %}"
          {:option {:id 3 :name "Ginny"} :record {:roles [2 6]}})]
      (is (= expected actual)
          "Both args are paths which exist in the context;
          the value of the first does not contain the value of the second;
          values are numbers"))
     (let [expected "Hello!"
          actual (parser/render "{% ifcontains record.roles option.id %}Hello!{% else %}Goodbye!{% endifcontains %}"
          {:option {:id :two :name "Fred"} :record {:roles [:two :six]}})]
      (is (= expected actual)
          "Both args are paths which exist in the context;
          the value of the first contains the value of the second;
          values are keywords"))
    (let [expected "Goodbye!"
          actual (parser/render "{% ifcontains record.roles option.id %}Hello!{% else %}Goodbye!{% endifcontains %}"
          {:option {:id :three :name "Ginny"} :record {:roles [:two :six]}})]
      (is (= expected actual)
          "Both args are paths which exist in the context;
          the value of the first does not contain the value of the second;
          values are keywords"))
     (let [expected "Hello!"
          actual (parser/render "{% ifcontains record.roles option.id %}Hello!{% else %}Goodbye!{% endifcontains %}"
          {:option {:id "two" :name "Fred"} :record {:roles ["two" "six"]}})]
      (is (= expected actual)
          "Both args are paths which exist in the context;
          the value of the first contains the value of the second;
          values are strings"))
    (let [expected "Goodbye!"
          actual (parser/render "{% ifcontains record.roles option.id %}Hello!{% else %}Goodbye!{% endifcontains %}"
          {:option {:id "three" :name "Ginny"} :record {:roles ["two" "six"]}})]
      (is (= expected actual)
          "Both args are paths which exist in the context;
          the value of the first does not contain the value of the second;
          values are strings"))
    (let [expected "Hello!"
          actual (parser/render "{% ifcontains record.roles 2 %}Hello!{% else %}Goodbye!{% endifcontains %}"
          {:option {:id 4 :name "Henry"} :record {:roles [2 6]}})]
      (is (= expected actual)
          "First arg is a path which exists in the context, second is a literal number;
          the value of the first contains the value of the second;
          values are numbers"))
    (let [expected "Goodbye!"
          actual (parser/render "{% ifcontains record.roles 3 %}Hello!{% else %}Goodbye!{% endifcontains %}"
          {:option {:id 3 :name "Ginny"} :record {:roles [2 6]}})]
      (is (= expected actual)
          "First arg is a path which exists in the context, second is a literal number;
          the value of the first does not contain the value of the second;
          values are numbers"))
    (let [expected "Hello!"
          actual (parser/render "{% ifcontains record.roles :two %}Hello!{% else %}Goodbye!{% endifcontains %}"
          {:option {:id 4 :name "Henry"} :record {:roles [:two :six]}})]
      (is (= expected actual)
          "First arg is a path which exists in the context, second is a literal keyword;
          the value of the first contains the value of the second;
          values are numbers"))
    (let [expected "Goodbye!"
          actual (parser/render "{% ifcontains record.roles :three %}Hello!{% else %}Goodbye!{% endifcontains %}"
          {:option {:id 3 :name "Ginny"} :record {:roles [:two :six]}})]
      (is (= expected actual)
          "First arg is a path which exists in the context, second is a literal keyword;
          the value of the first does not contain the value of the second;
          values are numbers"))))
