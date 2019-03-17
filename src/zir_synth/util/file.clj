(ns zir-synth.util.file
  (:gen-class)
  (:import (javax.sound.midi MidiSystem))
  (:require [clojure.tools.logging :as log]))

(defn play-midi-resource [resource]
  (log/info "Playing MIDI resource: " resource)
  (let [sequencer (MidiSystem/getSequencer)
        midi-resource (clojure.java.io/resource resource)
        sequence (MidiSystem/getSequence midi-resource)]
    (.open sequencer)
    (.setSequence sequencer sequence)
    (.start sequencer)))
