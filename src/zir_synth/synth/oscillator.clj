(ns zir-synth.synth.oscillator
  (:gen-class)
  (:require
    [zir-synth.util.synth :as zir-synth]
    [zir-synth.util.math :as zir-math]
    ))

(defn sine [t frequency-Hz amplitude]
  (let [ticks-per-cycle (/ zir-synth/sample-rate-Hz frequency-Hz)
        cycles (/ t ticks-per-cycle)
        angle (* zir-math/tau cycles)]
    (byte (* amplitude (Math/sin angle)))))

(defn square [t frequency-Hz amplitude]
  (let [ticks-per-cycle (/ zir-synth/sample-rate-Hz frequency-Hz)
        cycles (/ t ticks-per-cycle)
        angle (* zir-math/tau cycles)
        positive? (> 0 (Math/sin angle))]
    (byte (* amplitude (if positive? 1.0 0.0)))))
