(ns zir-synth.synth.oscillator
  (:gen-class)
  (:require [zir-synth.util.synth :as zir-synth]
            [zir-synth.util.math :as zir-math]
            [zir-synth.midi.note :as note]
            ))

(defn- angle [t frequency-Hz]
  (let [ticks-per-cycle (/ zir-synth/sample-rate-Hz frequency-Hz)
        cycles (/ t ticks-per-cycle)]
    (* cycles zir-math/tau)))

(defn calculate [type t note]
  (let [frequency-Hz (note/frequency note)
        angle (angle t frequency-Hz)]
    (cond
      (= type :sine) (Math/sin angle)
      (= type :square) (if (> 0 (Math/sin angle)) 1.0 0.0)
      )))

(defn wave-bytes [wave amplitude]
  (let [data (if wave
               (unchecked-byte (* wave amplitude))
               (byte 0))]
    (byte-array (concat [data] [data]))))
