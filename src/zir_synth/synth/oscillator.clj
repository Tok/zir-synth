(ns zir-synth.synth.oscillator
  (:gen-class)
  (:require [zir-synth.util.synth :as zir-synth]
            [zir-synth.util.math :as zir-math]
            [zir-synth.midi.note :as note]
            ))

(defn angle [tick note]
  (let [frequency-Hz (note/frequency note)
        tpc (zir-synth/ticks-per-cycle frequency-Hz)
        cycle (/ tick tpc)]
    (mod (* cycle zir-math/tau) zir-math/tau)))

(defn calculate-wave [type angle]
  (cond
    (= type :sine) (Math/sin angle)
    (= type :square) (if (> 0 (Math/sin angle)) 1.0 0.0)
    ))

(defn wave-bytes [wave amplitude]
  (let [global-volume 0.4
        data (if wave
               (byte (* wave (* amplitude global-volume)))
               (byte 0))]
    (byte-array (concat [data] [data]))))
