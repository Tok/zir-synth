(ns zir-synth.util.synth
  (:gen-class))

(def sample-rate-Hz 44100)

(defn n-channels [harmonic?] (if harmonic? 2 1))
(defn sound-byte [angle volume] (byte (* (Math/sin angle) volume)))

(defn calc-angle [frequency hz i] (* (* (/ i (/ frequency hz)) 2) Math/PI))
(defn calc-times [frequency duration-ms] (/ (* duration-ms frequency) 1000))
