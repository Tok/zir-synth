(ns zir-synth.midi.scale.scale
  (:gen-class)
  (:require [zir-synth.midi.note :as note]))

(defn scales-equal? [first second] (= (map note/offset first) (map note/offset second)))

(defn scales-shifted? [first second]
  (let [fun (fn [n] (note/midi-note 0 n))
        f (map fun first)
        s (map fun second)]
    (= (subvec (vec (concat (drop-last f) f)) 5 13) s)))

(defn triad [scale] [(nth scale 0) (nth scale 2) (nth scale 4)])
