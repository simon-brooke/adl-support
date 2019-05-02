(ns adl-support.utils-test
  (:require [clojure.test :refer :all]
            [adl-support.core :refer [*warn*]]
            [adl-support.utils :refer :all]))

;; Yes, there's MASSES in utils which ought to be tested. I'll add more tests over time.

(deftest singularise-tests
  (testing "Singularise"
    (is (= "address" (singularise "addresses")))
    (is (= "address" (singularise "address")))
    (is (= "expertise" (singularise "expertise")))))


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


(deftest list-related-query-name-tests
  (testing "list-related-query-name"
    (let [e1 {:tag :entity,
              :attrs {:volatility "6", :magnitude "1", :name "genders", :table "genders"},
              :content [{:tag :documentation,
                         :content ["All genders which may be assigned to\n    electors."]}
                        {:tag :key, :attrs nil,
                         :content [{:tag :property,
                                    :attrs {:distinct "all", :size "32", :type "string", :name "id"},
                                    :content [{:tag :prompt,
                                               :attrs {:locale "en_GB.UTF-8",
                                                       :prompt "Gender"},
                                               :content nil}]}]}
                        {:tag :list, :attrs {:name "Genders", :properties "all"}}
                        {:tag :form, :attrs {:name "Gender", :properties "all"}}]}
          e2 {:tag :entity,
              :attrs {:volatility "6", :magnitude "1", :name "electors", :table "electors"},
              :content [{:tag :documentation,
                         :attrs nil,
                         :content
                         ["All electors known to the system; electors are
                          people believed to be entitled to vote in the current
                          campaign."]}
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
                           [{:tag :generator, :attrs {:action "native"}, :content nil}]}]}
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
                         {:default "Unknown",
                          :farkey "id",
                          :entity "genders",
                          :column "gender",
                          :type "entity",
                          :name "gender"},
                         :content
                         [{:tag :prompt,
                           :attrs {:locale "en_GB.UTF-8", :prompt "Gender"},
                           :content nil}]}]}
          property (child e2 #(= (-> % :attrs :name) "gender"))
          expected "list-electors-by-gender"
          actual (list-related-query-name property e2 e1)]
      (is (= expected actual) "just checking..."))
    (let [e1 {:tag :entity
              :attrs {:name "dwellings"}
              :content [{:tag :key
                         :content [{:tag :property
                                    :attrs {:name "id" :type "integer" :distinct "system"}}]}
                        {:tag :property
                         :attrs {:name "address" :type "entity" :entity "addresses"}}]}
          e2 {:tag :entity
              :attrs {:name "addresses"}
              :content [{:tag :key
                         :content [{:tag :property
                                    :attrs {:name "id" :type "integer" :distinct "system"}}]}
                        {:tag :property
                         :attrs {:name "dwellings" :type "list" :entity "dwellings"}}]}]
      (let [property {:tag :property
                      :attrs {:name "address" :type "entity" :entity "addresses"}}
            expected "list-dwellings-by-address"
            actual (list-related-query-name property e1 e2)]
        (is (= expected actual) "Entity property"))
      (let [property {:tag :property
                      :attrs {:name "dwellings" :type "list" :entity "dwellings"}}
            expected "list-dwellings-by-address"
            actual (list-related-query-name property e2 e1)]
        (is (= expected actual) "List property")))
    (let [e1 {:tag :entity
              :attrs {:name "teams"}
              :content [{:tag :key
                         :content [{:tag :property
                                    :attrs {:name "id" :type "integer" :distinct "system"}}]}
                        {:tag :property
                         :attrs {:name "members" :type "link" :entity "canvassers"}}
                        {:tag :property
                         :attrs {:name "organisers" :type "link" :entity "canvassers"}}]}
          e2 {:tag :entity
              :attrs {:name "canvassers"}
              :content [{:tag :key
                         :content [{:tag :property
                                    :attrs {:name "id" :type "integer" :distinct "system"}}]}
                        {:tag :property
                         :attrs {:name "memberships" :type "link" :entity "teams"}}]}]
      (let [property {:tag :property
                      :attrs {:name "members" :type "link" :entity "canvassers"}}
            expected "list-members-by-team"
            actual (list-related-query-name property e1 e2)]
        (is (= actual expected) "Link property - members"))
      (let [property {:tag :property
                      :attrs {:name "organisers" :type "link" :entity "canvassers"}}
            expected "list-organisers-by-team"
            actual (list-related-query-name property e1 e2)]
        (is (= actual expected) "Link property - organisers"))
      (let [property {:tag :property
                         :attrs {:name "memberships" :type "link" :entity "teams"}}
            expected "list-memberships-by-canvasser"
            actual (list-related-query-name property e2 e1)]
        (is (= actual expected) "Link property - membersips")))))


(deftest link-table-name-tests
  (testing "link-table-name"
    (let [e1 {:tag :entity
              :attrs {:name "teams"}
              :content [{:tag :key
                         :content [{:tag :property
                                    :attrs {:name "id" :type "integer" :distinct "system"}}]}
                        {:tag :property
                         :attrs {:name "members" :type "link" :entity "canvassers"}}
                        {:tag :property
                         :attrs {:name "organisers" :type "link" :entity "canvassers"}}]}
          e2 {:tag :entity
              :attrs {:name "canvassers"}
              :content [{:tag :key
                         :content [{:tag :property
                                    :attrs {:name "id" :type "integer" :distinct "system"}}]}
                        {:tag :property
                         :attrs {:name "memberships" :type "link" :entity "teams"}}
                        {:tag :property
                         :attrs {:name "roles" :type "link" :entity "roles"}}]}
          e3 {:tag :entity
              :attrs {:name "roles"}
              :content [{:tag :key
                         :content [{:tag :property
                                    :type "string"
                                    :distinct "all"
                                    :name "id"}]}]}]
      (let [property {:tag :property
                      :attrs {:name "members" :type "link" :entity "canvassers"}}
            expected "ln_members_teams"
            actual (link-table-name property e1 e2)]
        (is (= actual expected) "Link property - members"))
      (let [property {:tag :property
                      :attrs {:name "organisers" :type "link" :entity "canvassers"}}
            expected "ln_organisers_teams"
            actual (link-table-name property e1 e2)]
        (is (= actual expected) "Link property - organisers"))
      (let [property {:tag :property
                         :attrs {:name "memberships" :type "link" :entity "teams"}}
            expected "ln_memberships_canvassers"
            actual (link-table-name property e2 e1)]
        (is (= actual expected) "Link property - membersips"))
      (let [property {:tag :property
                         :attrs {:name "roles" :type "link" :entity "roles"}}
            expected "ln_canvassers_roles"
            actual (link-table-name property e2 e3)]
        (is (= actual expected) "Link property - roles")))))


(deftest unique-link-tests
  (testing "unique-link?"
    (let [e1 {:tag :entity
              :attrs {:name "teams"}
              :content [{:tag :key
                         :content [{:tag :property
                                    :attrs {:name "id" :type "integer" :distinct "system"}}]}
                        {:tag :property
                         :attrs {:name "members" :type "link" :entity "canvassers"}}
                        {:tag :property
                         :attrs {:name "organisers" :type "link" :entity "canvassers"}}]}
          e2 {:tag :entity
              :attrs {:name "canvassers"}
              :content [{:tag :key
                         :content [{:tag :property
                                    :attrs {:name "id" :type "integer" :distinct "system"}}]}
                        {:tag :property
                         :attrs {:name "memberships" :type "link" :entity "teams"}}
                        {:tag :property
                         :attrs {:name "roles" :type "link" :entity "roles"}}]}
          e3 {:tag :entity
              :attrs {:name "roles"}
              :content [{:tag :key
                         :content [{:tag :property
                                    :type "string"
                                    :distinct "all"
                                    :name "id"}]}]}]
      (is (= false (unique-link? e1 e2)) "There are two logical links, three link properties, between e1 and e2")
      (is (= true (unique-link? e2 e3)) "There is only one link between e2 and e3"))))

(deftest capitalise-tests
  (testing "capitalise"
    (is (= (capitalise "the quick brown fox jumped over the lazy dog") "The Quick Brown Fox Jumped Over The Lazy Dog"))))

(deftest safe-name-tests
  (testing "safe-name"
     (let [e1 {:tag :entity
              :attrs {:name "canvass-teams" :table "team"}
              :content [{:tag :key
                         :content [{:tag :property
                                    :attrs {:name "id" :type "integer" :distinct "system"}}]}
                        {:tag :property
                         :attrs {:name "members" :type "link" :entity "canvassers"}}
                        {:tag :property
                         :attrs {:name "organisers" :type "link" :entity "canvassers"}}]}
           p1 {:tag :property
                                    :attrs {:name "id" :type "integer" :distinct "system"}}
           p2 {:tag :property
               :attrs {:name "with_underscore" :column "with-hyphen" :type "integer"}}]
       (is
         (= (safe-name "the quick brown fox jumped over the lazy dog")
            "thequickbrownfoxjumpedoverthelazydog")
         "absent a convention, spaces are suppressed")
       (is
         (= (safe-name "the quick brown fox jumped over the lazy dog" :c)
            "the_quick_brown_fox_jumped_over_the_lazy_dog")
         "in :c convention, spaces are replaced with underscores")
       (is
         (= (safe-name "the quick brown fox jumped over the lazy dog" :c-sharp)
            "TheQuickBrownFoxJumpedOverTheLazyDog")
         "in :c-sharp convention spaces are suppressed and all words camel cased")
       (is
         (= (safe-name "the quick brown fox jumped over the lazy dog" :java)
            "theQuickBrownFoxJumpedOverTheLazyDog")
         "in :java convention spaces are suppressed and embedded words camel cased")
       (is
         (= (safe-name "the quick brown fox jumped over the lazy dog" :sql)
            "the_quick_brown_fox_jumped_over_the_lazy_dog")
         "in :sql convention, spaces are replaced with underscores")
       (is (= (safe-name e1) "canvass-teams"))
       (is (= (safe-name e1 :c) "canvass_teams")
           "In :c convention, hyphen is replaced by underscore")
       (is (= (safe-name e1 :c-sharp) "CanvassTeams")
           "In :c-sharp convention, hyphen is suppressed and words capitalised")
       (is (= (safe-name e1 :java) "canvassTeams")
           "In :java convention, hyphen is suppressed and embedded words capitalised")
       (is (= (safe-name e1 :sql) "team")
           "In :sql convention, the :table attribute is preferred")
       (is (= (safe-name p1) "id"))
       (is (= (safe-name p1 :c) "id"))
       (is (= (safe-name p1 :c-sharp) "Id"))
       (is (= (safe-name p1 :java) "id"))
       (is (= (safe-name p1 :sql) "id"))
       (is (= (safe-name p2) "withunderscore")
           "Absent a convention, underscore is not considered safe")
       (is (= (safe-name p2 :c) "with_underscore")
           "In :c convention, underscore is considered safe")
       (is (= (safe-name p2 :c-sharp) "WithUnderscore")
           "In :c-sharp convention, initial letters are capialised and underscore is suppressed")
       (is (= (safe-name p2 :java) "withUnderscore")
           "In :java convention, underscore is suppressed and embedded words capitalised")
       (is (= (safe-name p2 :sql) "with_hyphen")
           "In :sql convention, the column-name variant is preferred, and hyphens replaced with underscores"))))


(deftest key-names-tests
  (testing "key-names"
    (let [e1 {:tag :entity
              :attrs {:name "canvass-teams" :table "team"}
              :content [{:tag :key
                         :content [{:tag :property
                                    :attrs {:name "id" :type "integer" :distinct "system"}}]}
                        {:tag :property
                         :attrs {:name "members" :type "link" :entity "canvassers"}}
                        {:tag :property
                         :attrs {:name "organisers" :type "link" :entity "canvassers"}}]}
          e2 {:tag :entity
              :attrs {:name "canvass-teams" :table "team"}
              :content [{:tag :key
                         :content [{:tag :property
                                    :attrs {:name "id" :type "integer" :distinct "system"}}
                                   {:tag :property
                                    :attrs {:name "shard" :type "string" :default "SW"}}]}
                        {:tag :property
                         :attrs {:name "members" :type "link" :entity "canvassers"}}
                        {:tag :property
                         :attrs {:name "organisers" :type "link" :entity "canvassers"}}]}]
      (is (= (key-names e1) #{"id"}))
      (is (= (key-names e1 true) #{:id}))
      (is (= (key-names e2) #{"id" "shard"}))
      (is (= (key-names e2 true) #{:id :shard})))))


(deftest order-preserving-set-tests
  (testing "order-preserving-set"
    (is (= '(:a) (order-preserving-set '(:a :a :a :a))))
    (is (= '(:a) (order-preserving-set [:a :a :a :a])))
    (is (= '(:a :b :c :d :e) (order-preserving-set '(:a :a :b :c :a :b :d :c :e))))))
