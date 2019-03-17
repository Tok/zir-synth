(ns zir-synth.synth.oscillator
  (:gen-class)
  (:require [zir-synth.util.synth :as zir-synth]
            [zir-synth.util.math :as zir-math]
            [zir-synth.midi.note :as note]))

(defn calc-amplitude [tick type note velocity]
  (let [frequency-Hz (note/frequency note)
        peak-amplitude (/ velocity 127)
        raw-phase (/ (* tick frequency-Hz zir-math/tau) zir-synth/sample-rate-Hz)
        phase (mod raw-phase zir-math/tau)]
    (cond
      (= type :sine) (* peak-amplitude (Math/sin phase))
      (= type :square) (* peak-amplitude (if (> 0 (Math/sin phase)) 1.0 0.0)))))

(defn wave-bytes [wave]
  (let [global-volume 1.0
        data (if wave
               (byte (* wave global-volume))
               (byte 0))]
    (byte-array (concat [data] [data]))))
