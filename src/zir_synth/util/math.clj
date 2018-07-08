(ns zir-synth.util.math
  (:gen-class)
  (:require [clojure.algo.generic.math-functions :as algo]))

(def phi 1.618033988749895)

(defn interval-coefficient [offset] (algo/pow 2 (/ offset 12)))

;https://en.wikipedia.org/wiki/Twelfth_root_of_two#The_equal-tempered_chromatic_scale
(def twelfth-root-of-two (interval-coefficient 1))

(defn next-octave [frequency] (* frequency 2))
(defn previous-octave [frequency] (/ frequency 2))

(defn next-pitch [frequency] (* frequency twelfth-root-of-two))
(defn previous-pitch [frequency] (/ frequency twelfth-root-of-two))
