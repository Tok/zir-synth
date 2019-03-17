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
            [zir-synth.util.math :as math-util]
            [zir-synth.midi.note :as note]
            [zir-synth.midi.chord :as chord]
            [zir-synth.midi.melody :as melody]
            [zir-synth.midi.scale.major :as major]
            [zir-synth.midi.scale.minor :as minor]))

(defn play-note! [chan note-number note-velocity duration-ms]
  (.noteOn ^SoftChannelProxy chan note-number note-velocity)
  (future
    (<!! (timeout duration-ms))
    (.noteOff ^SoftChannelProxy chan note-number)))

(defn- play-seq! [midi-seq]
  (log/info "Playing sequence:" midi-seq)
  (let [synth (MidiSystem/getSynthesizer)
        chan (nth (.getChannels synth) 0)
        piano-volume 128/2
        buzz-volume 128/4
        bpm (* 120)
        steps (* bpm 4)
        duration-ms (/ 60000 steps)
        octave 0]
    (.open synth)
    (doseq [notes midi-seq]
      (println notes)
      (doseq [n notes]
        (play-note! chan (note/midi-note octave n) piano-volume duration-ms))
      (buzz/play! notes buzz-volume duration-ms))))

(defn- send-notes [receiver chan-id i midi-note-vec]
  (doseq [midi-note midi-note-vec]
    (let [volume 100
          on-msg (ShortMessage. (ShortMessage/NOTE_ON) chan-id midi-note volume)
          off-msg (ShortMessage. (ShortMessage/NOTE_OFF) chan-id midi-note volume)]
      (.send receiver on-msg i)
      (.send receiver off-msg (+ i 4)))))

(defn- queue-notes [track chan-id i midi-note-vec]
  (doseq [midi-note midi-note-vec]
    (let [volume 100
          on-msg (ShortMessage. (ShortMessage/NOTE_ON) chan-id midi-note volume)
          off-msg (ShortMessage. (ShortMessage/NOTE_OFF) chan-id midi-note volume)]
      ;(synth-chan)
      (.add track (MidiEvent. on-msg i))
      (.add track (MidiEvent. off-msg (+ i 4))))))

(defn- init [sequencer synth]
  (let [transmitter (.getTransmitter sequencer)
        receiver (.getReceiver synth)
        soundbank (.getDefaultSoundbank synth)]
    (log/info "Loading soundbank..")
    (.loadAllInstruments synth soundbank)
    (log/info "Connecting synth..")
    (.setReceiver transmitter receiver)
    receiver))

(defn- play-sequence [sq bpm]
  (let [sequencer (MidiSystem/getSequencer)
        synth (MidiSystem/getSynthesizer)
        receiver (init sequencer synth)
        indexed-notes (map-indexed (fn [i n] [i (note/midi-notes note/default-octave n)]) sq)
        midi-seq (Sequence. (Sequence/PPQ) 4)
        track (.createTrack midi-seq)
        synth-chan-id 0]
    (log/info "Opening Synthesizer..")
    (.open synth)
    (log/info "Obtaining channels..")
    (def synth-chan (nth (.getChannels synth) synth-chan-id))
    (log/info "Preparing track..")
    (.add track (MidiEvent.
                  (ShortMessage.
                    (ShortMessage/PROGRAM_CHANGE) synth-chan-id 1 0) -1))
    (log/info "Opening Sequencer..")
    (.open sequencer)
    (log/info "Setting tempo to" bpm "BPM.")
    (.setTempoInBPM sequencer bpm)
    (log/info "Queueing notes..")

    ;(doseq [[i n] indexed-notes] (send-notes receiver synth-chan-id (* i 4) n))
    (doseq [[i n] indexed-notes] (queue-notes track synth-chan-id (* i 4) n))

    (log/info "Setting sequence:" sq)
    (.setSequence sequencer midi-seq)
    ;(.setLoopCount sequencer Sequencer/LOOP_CONTINUOUSLY)
    (.setLoopCount sequencer 1)
    (log/info "Starting..")
    (.start sequencer)))

(defn -main "Zir Synth" [& args]
  (let [scale (major/c-major)
        melody (melody/triad-melody scale)
        bpm 180.0
        steps (count melody)
        duration-s (* (math-util/to-bps bpm) steps)]
    (play-sequence melody bpm)

    ;(play-seq! melody)

    ;(file/play-midi-resource "wikipedia/Drum_sample.mid")
    ;(play-times! 16)
    (future
      (<!! (timeout (* duration-s 1000)))
      (shutdown-agents)
      ;(.stop sequencer)
      ;(.close sequencer)
      (log/info "Shutting down...")
      (System/exit 0))))
