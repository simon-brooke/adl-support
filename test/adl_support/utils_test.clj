(ns adl-support.utils-test
  (:require [clojure.test :refer :all]
            [adl-support.utils :refer :all]))

;; Yes, there's MASSES in utils which ought to be tested. I'll add more tests over time.

(deftest child-with-tag-tests
  (testing "child-with-tag"
    (let [expected {:tag :prompt
                    :attrs {:prompt "test"
                            :locale "en_GB.UTF-8"}}
          actual (child-with-tag {:tag :property
                                  :attrs {:name "not test"}
                                  :content [{:tag :prompt
                                             :attrs {:prompt "test"
                                                     :locale "en_GB.UTF-8"}}]}
                                 :prompt)]
      (is (= expected actual) "Basic search for one child which exists"))
    (let [expected nil
          actual (child-with-tag {:tag :property
                                  :attrs {:name "not test"}
                                  :content [{:tag :prompt
                                             :attrs {:prompt "test"
                                                     :locale "en_GB.UTF-8"}}]}
                                 :frobox)]
      (is (= expected actual) "Basic search for one child which doesn't exist"))
    (let [expected nil
          actual (child-with-tag nil :frobox)]
      (is (= expected actual) "Basic search with nil element"))
    (let [expected {:tag :prompt
                    :attrs {:prompt "test"
                            :locale "en_GB.UTF-8"}}
          actual (child-with-tag {:tag :property
                                  :attrs {:name "not test"}
                                  :content [{:tag :frobox}
                                            {:tag :prompt
                                             :attrs {:prompt "test"
                                                     :locale "en_GB.UTF-8"}}]}
                                 :prompt)]
      (is (= expected actual) "Basic search for one child which exists but is not first"))
    (let [expected {:tag :prompt
                    :attrs {:prompt "test"
                            :locale "en_GB.UTF-8"}}
          actual (child-with-tag {:tag :property
                                  :attrs {:name "not test"}
                                  :content [{:tag :prompt
                                             :attrs {:prompt "essai"
                                                     :locale "fr-FR"}}
                                            {:tag :prompt
                                             :attrs {:prompt "test"
                                                     :locale "en_GB.UTF-8"}}]}
                                 :prompt
                                 #(= (-> % :attrs :locale) "en_GB.UTF-8"))]
      (is (= expected actual) "Conditional search for one child which exists (1)"))
    (let [*locale* "fr-FR"
          expected {:tag :prompt
                    :attrs {:prompt "essai"
                            :locale "fr-FR"}}
          actual (child-with-tag {:tag :property
                                  :attrs {:name "not test"}
                                  :content [{:tag :prompt
                                             :attrs {:prompt "essai"
                                                     :locale "fr-FR"}}
                                            {:tag :prompt
                                             :attrs {:prompt "test"
                                                     :locale "en_GB.UTF-8"}}]}
                                 :prompt
                                 #(= (-> % :attrs :locale) "fr-FR"))]
      (is (= expected actual) "Conditional search for one child which exists (2)"))
    ))


(deftest prompt-tests
  (testing "Prompts for fields and properties"
    (let [*locale* "en_GB.UTF-8"
          expected "Test"
          actual (prompt {:tag :property
                          :attrs {:name "not test"}
                          :content [{:tag :prompt
                                     :attrs {:prompt "test"
                                             :locale "en_GB.UTF-8"}}]}
                         {}
                         {}
                         {})]
      (is (= expected actual) "Basic property with one prompt in current locale"))
    (let [*locale* "en_GB.UTF-8"
          expected "Test"
          actual (prompt {:tag :field
                          :attrs {:property "not-test"}
                          :content [{:tag :prompt
                                     :attrs {:prompt "test"
                                             :locale "en_GB.UTF-8"}}]}
                         {}
                         {}
                         {})]
      (is (= expected actual) "Basic field with one prompt in current locale"))
    (let [*locale* "en_GB.UTF-8"
          expected "Test"
          actual (prompt {:tag :field
                          :attrs {:property "not-test"}}
                         {}
                         {:tag :entity
                          :content [{:tag :property
                                     :attrs {:name "not-test"}
                                     :content [{:tag :prompt
                                                :attrs {:prompt "test"
                                                        :locale "en_GB.UTF-8"}}]}]}
                         {})]
      (is (= expected actual) "Basic field with no prompt, in context of entity
          with appropriate property with prompt in current locale"))
    (let [*locale* "en_GB.UTF-8"
          expected "Home"
          actual (prompt {:tag :field,
                          :attrs {:property "dwelling_id"}}
                         {}
                         {:tag :entity,
                          :attrs
                          {:volatility "5",
                           :magnitude "6",
                           :name "electors",
                           :table "electors"},
                          :content
                          [{:tag :documentation,
                            :attrs nil,
                            :content
                            ["All electors known to the system; electors are\n    people believed to be entitled to vote in the current\n    campaign."]}
                           {:tag :key,
                            :attrs nil,
                            :content
                            [{:tag :property,
                              :attrs
                              {:distinct "system",
                               :immutable "true",
                               :column "id",
                               :name "id",
                               :type "integer",
                               :required "true"},
                              :content
                              [{:tag :prompt,
                                :attrs {:locale "en_GB.UTF-8", :prompt "id"},
                                :content nil}]}]}
                           {:tag :property,
                            :attrs
                            {:distinct "user",
                             :column "name",
                             :name "name",
                             :type "string",
                             :required "true",
                             :size "64"},
                            :content
                            [{:tag :prompt,
                              :attrs {:locale "en_GB.UTF-8", :prompt "Name"},
                              :content nil}]}
                           {:tag :property,
                            :attrs
                            {:farkey "id",
                             :entity "dwellings",
                             :column "dwelling_id",
                             :name "dwelling_id",
                             :type "entity",
                             :required "true"},
                            :content
                            [{:tag :prompt,
                              :attrs {:locale "en_GB.UTF-8", :prompt "Home"},
                              :content nil}]}
                           {:tag :property,
                            :attrs {:column "phone", :name "phone", :type "string", :size "16"},
                            :content
                            [{:tag :prompt,
                              :attrs {:locale "en_GB.UTF-8", :prompt "Phone"},
                              :content nil}]}
                           {:tag :property,
                            :attrs
                            {:column "email", :name "email", :type "string", :size "128"},
                            :content
                            [{:tag :prompt,
                              :attrs {:locale "en_GB.UTF-8", :prompt "Email"},
                              :content nil}]}
                           {:tag :property,
                            :attrs
                            {:default "Unknown",
                             :farkey "id",
                             :entity "genders",
                             :column "gender",
                             :type "entity",
                             :name "gender"},
                            :content
                            [{:tag :prompt,
                              :attrs {:locale "en_GB.UTF-8", :prompt "Gender"},
                              :content nil}]}
                           {:tag :property,
                            :attrs {:type "text", :name "signature"},
                            :content
                            [{:tag :documentation,
                              :attrs nil,
                              :content
                              ["The signature of this elector, captured as SVG text,\n      as evidence they have consented to us holding data on them.\n      Null if they have not."]}]}
                           {:tag :list,
                            :attrs {:name "Electors", :properties "listed"},
                            :content
                            [{:tag :field,
                              :attrs {:property "id"},
                              :content
                              [{:tag :prompt,
                                :attrs {:locale "en_GB.UTF-8", :prompt "id"},
                                :content nil}]}
                             {:tag :field,
                              :attrs {:property "name"},
                              :content
                              [{:tag :prompt,
                                :attrs {:locale "en_GB.UTF-8", :prompt "Name"},
                                :content nil}]}
                             {:tag :field,
                              :attrs {:property "dwelling_id"},
                              :content
                              [{:tag :prompt,
                                :attrs {:locale "en_GB.UTF-8", :prompt "Home"},
                                :content nil}]}
                             {:tag :field,
                              :attrs {:property "phone"},
                              :content
                              [{:tag :prompt,
                                :attrs {:locale "en_GB.UTF-8", :prompt "Phone"},
                                :content nil}]}
                             {:tag :field,
                              :attrs {:property "email"},
                              :content
                              [{:tag :prompt,
                                :attrs {:locale "en_GB.UTF-8", :prompt "eMail"},
                                :content nil}]}
                             {:tag :field,
                              :attrs {:property "gender"},
                              :content
                              [{:tag :prompt,
                                :attrs {:locale "en_GB.UTF-8", :prompt "Gender"},
                                :content nil}]}]}
                           {:tag :form,
                            :attrs {:name "Elector", :properties "listed"},
                            :content
                            [{:tag :field,
                              :attrs {:property "id"},
                              :content
                              [{:tag :prompt,
                                :attrs {:locale "en_GB.UTF-8", :prompt "id"},
                                :content nil}]}
                             {:tag :field,
                              :attrs {:property "name"},
                              :content
                              [{:tag :prompt,
                                :attrs {:locale "en_GB.UTF-8", :prompt "Name"},
                                :content nil}]}
                             {:tag :field,
                              :attrs {:property "dwelling_id"},
                              :content
                              [{:tag :prompt,
                                :attrs {:locale "en_GB.UTF-8", :prompt "Home"},
                                :content nil}]}
                             {:tag :field,
                              :attrs {:property "phone"},
                              :content
                              [{:tag :prompt,
                                :attrs {:locale "en_GB.UTF-8", :prompt "Phone"},
                                :content nil}]}
                             {:tag :field,
                              :attrs {:property "email"},
                              :content
                              [{:tag :prompt,
                                :attrs {:locale "en_GB.UTF-8", :prompt "eMail"},
                                :content nil}]}
                             {:tag :field,
                              :attrs {:property "gender"},
                              :content
                              [{:tag :prompt,
                                :attrs {:locale "en_GB.UTF-8", :prompt "Gender"},
                                :content nil}]}]}
                           {:tag :permission,
                            :attrs {:permission "read", :group "canvassers"},
                            :content nil}
                           {:tag :permission,
                            :attrs {:permission "read", :group "teamorganisers"},
                            :content nil}
                           {:tag :permission,
                            :attrs {:permission "read", :group "issueexperts"},
                            :content nil}
                           {:tag :permission,
                            :attrs {:permission "read", :group "analysts"},
                            :content nil}
                           {:tag :permission,
                            :attrs {:permission "read", :group "issueeditors"},
                            :content nil}
                           {:tag :permission,
                            :attrs {:permission "all", :group "admin"},
                            :content nil}]}

                         {})]
      (is (= expected actual) "With realistic clutter: field with no prompt, in context of entity
          with appropriate property with prompt in current locale"))
    ))
