(ns zir-synth.synth.oscillator
  (:gen-class)
  (:require
    [zir-synth.util.synth :as zir-synth]
    [zir-synth.util.math :as zir-math]
    ))

(defn- angle [t frequency-Hz]
  (let [ticks-per-cycle (/ zir-synth/sample-rate-Hz frequency-Hz)
        cycles (/ t ticks-per-cycle)]
    (* cycles zir-math/tau)))

(defn sine [t frequency-Hz]
  (Math/sin (angle t frequency-Hz)))

(defn square [t frequency-Hz]
  (if (> 0 (Math/sin (angle t frequency-Hz))) 1.0 0.0))

(defn wave-bytes [wave amplitude]
  (byte (* wave amplitude)))
