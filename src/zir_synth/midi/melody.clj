(ns zir-synth.midi.melody
  (:gen-class)
  (:require [zir-synth.midi.note :as note]))

(defn- switch-last [segment new-last] (conj (pop (vec segment)) new-last))
(defn- last-note-up [segment] (switch-last segment (note/note-up (last segment))))
(defn- last-note-down [segment] (switch-last segment (note/note-down (last segment))))

(defn random-segment [scale] (map (fn [i] (rand-nth scale)) (range 4)))

(defn basic-melody [segment]
  (let [seg (vec segment)] (flatten [seg (last-note-down seg) seg (shuffle seg)])))
