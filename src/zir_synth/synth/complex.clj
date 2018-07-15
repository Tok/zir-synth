(ns zir-synth.synth.complex
  (:gen-class)
  (:require [clojure.algo.generic.math-functions :as algo]))

(defn i [] [0.0 1.0])
(defn zero [] [0.0 0.0])
(defn one [] [1.0 0.0])

(defn- add-squares [[re im]] (+ (* re re) (* im im)))

(defn magnitude [[re im]] (Math/sqrt (add-squares [re im])))
(defn phase [[re im]] (algo/atan2 re im))

(defn negate [[re im]] [(- re) (- im)])
(defn conjugate [[re im]] [re (- im)])
(defn reverse [[re im]] [(- re) im])

(defn to-string [complex]
  (let [epsilon 1e-20
        [im re] complex]
    (cond
      (= complex (i)) "i"
      (algo/approx= im 0 epsilon) (str re)
      (algo/approx= re 0 epsilon) (concat (str im) "i")
      :else (concat (str re) (if (< im 0) "-" "+") (str im) "*i"))))

(defn plus [a b]
  (let [[a-re a-im] a [b-re b-im] b]
    [(+ a-re b-re) (+ a-im b-im)]))

(defn minus [a b]
  (let [[a-re a-im] a [b-re b-im] b]
    [(- a-re b-re) (- a-im b-im)]))

(defn times [a b]
  (let [[a-re a-im] a [b-re b-im] b]
    [(- (* a-re b-re) (* a-im b-im))
     (- (* a-re b-im) (* a-im b-re))]))

(defn div [a b]
  (let [[a-re a-im] a [b-re b-im] b]
    (if (algo/approx= b-re 0 1e-10) (throw (IllegalArgumentException "Real part is 0.")))
    (if (algo/approx= b-im 0 1e-10) (throw (IllegalArgumentException "Imaginary part is 0.")))
    (let [d (add-squares b)]
      [(/ ((+ (* a-re b-re) (* a-im b-im))) d)
       (/ ((- (* a-im b-re) (* a-re b-im))) d)])))
