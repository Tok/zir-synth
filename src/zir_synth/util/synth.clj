(ns zir-synth.util.synth
  (:gen-class)
  (:import (javax.sound.sampled AudioFormat)))

(def sample-rate-Hz 88200)
(def ns-per-sample (/ 1000000000 sample-rate-Hz))
(defn ticks-per-cycle [frequency-Hz] (/ sample-rate-Hz frequency-Hz))

(defn audio-format []
  (let [sample-size-in-bits 8 channels 2 signed true big-endian false]
    (AudioFormat. sample-rate-Hz sample-size-in-bits channels signed big-endian)))

(defn n-channels [harmonic?] (if harmonic? 2 1))
(defn sound-byte [angle volume] (byte (* (Math/sin angle) volume)))

(defn calc-angle [frequency hz i] (* (* (/ i (/ frequency hz)) 2) Math/PI))
(defn calc-times [frequency duration-ms] (/ (* duration-ms frequency) 1000))
