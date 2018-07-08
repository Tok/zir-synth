(ns zir-synth.core
  (:gen-class)
  (:import (javax.sound.midi MidiSystem)
           (javax.sound.midi MidiEvent)
           (javax.sound.midi Sequence)
           (javax.sound.midi ShortMessage)
           (com.sun.media.sound SoftChannelProxy))
  (:require [clojure.core.async :refer [<!! timeout]]
            [clojure.tools.logging :as log]
            [zir-synth.instrument.buzz :as buzz]
            [zir-synth.midi.note :as note]
            [zir-synth.midi.chord :as chord]
            [zir-synth.midi.melody :as melody]
            [zir-synth.midi.scale.major :as major]
            [zir-synth.midi.scale.minor :as minor]))

(defn play-note! [chan note-number note-velocity duration-ms]
  (.noteOn ^SoftChannelProxy chan note-number note-velocity)
  (future
    (<!! (timeout duration-ms))
    (.noteOff ^SoftChannelProxy chan note-number)
    ))

(defn- play-times! [n]
  (let [synth (MidiSystem/getSynthesizer)
        chan (nth (.getChannels synth) 0)
        piano-volume 128/2
        buzz-volume 128/4
        bpm (* 120)
        steps (* bpm 4)
        duration-ms (/ 60000 steps)
        scale (minor/scale :B)
        triad (chord/random-triad scale)
        octave 0
        add-harmony? true]
    (.open synth)
    (dotimes [i n]
      (let [piano-note (rand-nth triad)
            buzz-note (rand-nth scale)
            piano-midi (note/midi-note octave piano-note)
            buzz-hz (note/frequency (note/midi-note octave buzz-note))]
        (buzz/play! buzz-hz buzz-volume duration-ms add-harmony?)
        (play-note! chan piano-midi piano-volume duration-ms)
        ))))

(defn- queue-note [track chan i midi-note]
  (let [volume 100
        on-msg (ShortMessage. (ShortMessage/NOTE_ON) chan midi-note volume)
        off-msg (ShortMessage. (ShortMessage/NOTE_OFF) chan midi-note volume)]
    (.add track (MidiEvent. on-msg i))
    (.add track (MidiEvent. off-msg (+ i 1)))
    ))

(defn- play-sequence [sq]
  (let [sequencer (MidiSystem/getSequencer)
        indexed-notes (map-indexed (fn [i n] [i (note/midi-note note/default-octave n)]) sq)
        midi-seq (Sequence. (Sequence/PPQ) 4)
        track (.createTrack midi-seq)
        chan 1]
    (log/info "Playing sequence:" sq)
    (.open sequencer)
    (doseq [note indexed-notes] (queue-note track chan (first note) (second note)))
    (.setSequence sequencer midi-seq)
    ;(.setLoopCount sequencer Sequencer/LOOP_CONTINUOUSLY)
    (.start sequencer)
    ))

(defn -main "Zir Synth" [& args]
  (play-sequence (melody/basic-melody (melody/random-segment (major/c-major))))
  ;(file/play-midi-resource "wikipedia/Drum_sample.mid")
  ;(play-times! 16)
  (future
    (<!! (timeout 4000))
    (.close (MidiSystem/getSequencer))
    (shutdown-agents)
    (log/info "Shutting down...")
    (System/exit 0)
    ))
