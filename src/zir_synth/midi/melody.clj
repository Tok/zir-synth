(ns zir-synth.midi.melody
  (:gen-class)
  (:require [zir-synth.midi.note :as note]
            [zir-synth.midi.chord :as chord]))

(defn- switch-first [segment new-first] (assoc segment 0 new-first))
(defn- switch-last [segment new-last] (assoc segment (- (count segment) 1) new-last))

(defn- last-note-up [segment] (switch-last segment (note/note-up (last segment))))
(defn- last-note-down [segment] (switch-last segment (note/note-down (last segment))))

(defn random-segment [scale] (vec (map (fn [i] (rand-nth scale)) (range 4))))

(defn- map-vec [n] (if (vector? n) n [n]))

(defn basic-melody [seg] (map map-vec (flatten [seg (last-note-down seg) seg (shuffle seg)])))

(defn triad-melody [scale]
  (let [seg (random-segment scale)
        triad (chord/major-triad scale)
        first-seg (switch-first seg triad)
        second-seg (last-note-down first-seg)
        third-seg first-seg
        fourth-seg (switch-first (shuffle seg) triad)]
    (map map-vec (apply concat [first-seg second-seg third-seg fourth-seg]))))
