(ns zir-synth.util.math-test
  (:require [clojure.test :refer :all]
            [zir-synth.test-util :refer :all]
            [zir-synth.util.math :refer :all]
            [clojure.algo.generic.math-functions :as algo]))

(deftest twelfth-root-of-two-test
  (let [epsilon 1e-20]
    (test-approx= twelfth-root-of-two 1.0594630943592952646 epsilon)))

(deftest interval-coefficient-test
  (let [epsilon 1e-6]
    (doseq [v [[0 1.000000] [1 1.059463] [2 1.122462] [3 1.189207]
               [4 1.259921] [5 1.334839] [6 1.414213] [7 1.498307]
               [8 1.587401] [9 1.681792] [10 1.781797] [11 1.887748]
               [12 2.000000] [120 1024.000000] [240 1048576.000000]
               [-12 0.500000] [-120 9.765625e-4] [-240 9.5367431640625e-7]]]
      (test-approx= (interval-coefficient (first v)) (second v) epsilon))))

(deftest octave-up-down-test
  (test= (next-octave 440) 880)
  (test= (previous-octave 440) 220)
  (test= (-> 440 next-octave next-octave) 1760)
  (test= (-> 440 previous-octave previous-octave) 110))

(deftest pitch-up-down-test
  (let [epsilon 1e-10]
    (test-approx= (next-pitch 440) 466.1637615181 epsilon)
    (test-approx= (previous-pitch 440) 415.3046975799 epsilon)
    (test-approx= (-> 440 next-pitch next-pitch) 493.8833012561 epsilon)
    (test-approx= (-> 440 previous-pitch previous-pitch) 391.9954359817 epsilon)))
