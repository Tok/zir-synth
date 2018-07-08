(ns zir-synth.util.math-test
  (:require [clojure.test :refer :all]
            [zir-synth.util.math :refer :all]
            [clojure.algo.generic.math-functions :as algo]))

(deftest twelfth-root-of-two-test
  (testing (is (algo/approx= twelfth-root-of-two 1.0594630943592952646 1e-20))))

(defn- coefficient-ok? [offset compare]
  (let [epsilon 1e-6]
    (algo/approx= (interval-coefficient offset) compare epsilon)))

(deftest interval-coefficient-test
  (doseq [v [[0 1.000000] [1 1.059463] [2 1.122462] [3 1.189207]
             [4 1.259921] [5 1.334839] [6 1.414213] [7 1.498307]
             [8 1.587401] [9 1.681792] [10 1.781797] [11 1.887748]
             [12 2.000000] [120 1024.000000] [240 1048576.000000]
             [-12 0.500000] [-120 9.765625e-4] [-240 9.5367431640625e-7]]]
    (testing (is (coefficient-ok? (first v) (second v))))))

(deftest octave-up-down-test
  (testing (is (= (next-octave 440) 880)))
  (testing (is (= (previous-octave 440) 220)))
  (testing (is (= (next-octave (next-octave 440)) 1760)))
  (testing (is (= (previous-octave (previous-octave 440)) 110)))
  )

(deftest pitch-up-down-test
  (let [epsilon 1e-10]
    (testing (is (algo/approx= (next-pitch 440) 466.1637615181 epsilon)))
    (testing (is (algo/approx= (previous-pitch 440) 415.3046975799 epsilon)))
    (testing (is (algo/approx= (next-pitch (next-pitch 440)) 493.8833012561 epsilon)))
    (testing (is (algo/approx= (previous-pitch (previous-pitch 440)) 391.9954359817 epsilon)))
    ))
