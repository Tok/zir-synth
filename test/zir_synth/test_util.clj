(ns zir-synth.test-util
  (:require [clojure.test :refer :all]
            [clojure.algo.generic.math-functions :as algo]))

(defn do-test
  ([passed?] (testing (is passed?)))
  ([string passed?] (testing string (is passed?))))

(defn test=
  ([value expected] (do-test (= value expected)))
  ([string value expected] (do-test string (= value expected))))

(defn test-approx=
  ([value expected epsilon] (do-test (algo/approx= value expected epsilon)))
  ([string value expected epsilon] (do-test string (algo/approx= value expected epsilon))))

(defn contained? [value vector] (not (nil? (some #{value} vector))))
