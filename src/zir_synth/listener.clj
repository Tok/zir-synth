(ns zir-synth.listener
  (:gen-class)
  (:require [clojure.string :as s]
            [clojure.core.async :refer [go]]
            [zir-synth.util.synth :as zir-synth]
            [zir-synth.util.math :as zir-math]
            [zir-synth.midi.note :as note])
  (:import (javax.sound.midi MidiDevice)
           (javax.sound.midi MidiSystem)
           (javax.sound.midi MidiUnavailableException)
           (javax.sound.midi Receiver)
           (javax.sound.midi Sequencer)
           (javax.sound.midi Synthesizer)
           (javax.sound.midi ShortMessage)
           (javax.sound.sampled AudioFormat)
           (javax.sound.sampled AudioSystem)
           (javax.sound.sampled SourceDataLine)
           ))

(defn sine-wave [t frequency-Hz velocity]
  (let [volume 0.05
        amplitude (* volume velocity)
        ticks-per-cycle (/ zir-synth/sample-rate-Hz frequency-Hz)
        cycles (/ t ticks-per-cycle)
        angle (* zir-math/tau cycles)]
    (byte (* amplitude (Math/sin angle)))))

(defn audio-format []
  (let [sample-size-in-bits 8 channels 2 signed true big-endian false]
    (AudioFormat. zir-synth/sample-rate-Hz sample-size-in-bits channels signed big-endian)))

(defn play-note [velocities sdl note]
  (loop [t 0]
    (let [velocity @(get velocities note)
          running? (> velocity 0)]
      (if running?
        (let [frequency-Hz (note/frequency note)
              data (sine-wave t frequency-Hz velocity)
              bytes (byte-array (concat [data] [data]))]
          (.write ^SourceDataLine sdl bytes 0 2)
          (recur (+ t 1)))
        (do
          (.drain sdl)
          (.stop sdl)
          (.close sdl))))))

(defn note-off [velocities timestamp note]
  (println timestamp "OFF" note)
  (reset! (get velocities note) 0))

(defn note-on [velocities timestamp note velocity]
  (println timestamp "ON" note velocity)
  (if (= velocity 0)
    (note-off velocities timestamp note)
    (let [audio-format (audio-format)
          sdl (AudioSystem/getSourceDataLine audio-format)]
      (.open sdl audio-format)
      (.start sdl)
      (reset! (get velocities note) velocity)
      (go (play-note velocities sdl note))
      )))

(defn zir-receiver []
  (def velocities (into (sorted-map) (map (fn [i] [i (atom (int 0))]) (range 0 127))))
  (reify Receiver
    (send [_ message timestamp]
      (let [command (int (.getCommand message))
            channel (int (.getChannel message))]
        (if (or (= command ShortMessage/NOTE_ON) (= command ShortMessage/NOTE_OFF))
          (let [note (.getData1 message)
                velocity (.getData2 message)]
            (cond
              (= command ShortMessage/NOTE_ON) (note-on velocities timestamp note velocity)
              (= command ShortMessage/NOTE_OFF) (note-off velocities timestamp note)))
          (println "WARNING: Unhandled command" command))))
    (close [this] (println "closing..."))))

(defn midi-port? [device] (and (not (instance? Sequencer device)) (not (instance? Synthesizer device))))
(defn in-port? [midi-port] (s/includes? (type (.getDeviceInfo midi-port)) "MidiInDevice"))

(defn -main
  "Opens the first active MIDI input device and connects it with a new receiver."
  [& args]
  (let [infos (MidiSystem/getMidiDeviceInfo)
        devices (map (fn [info] (MidiSystem/getMidiDevice info)) infos)
        midi-ports (filter midi-port? devices)
        in-port (first (filter in-port? midi-ports))
        synth-transmitter (.getTransmitter ^MidiDevice in-port)
        zir-transmitter (.getTransmitter ^MidiDevice in-port)
        synth (MidiSystem/getSynthesizer)]
    (.setReceiver synth-transmitter (.getReceiver synth))
    (.setReceiver zir-transmitter (zir-receiver))
    (println "Trying to open device" (.toString (.getDeviceInfo in-port)))
    (try
      (.open synth)
      (.open in-port)
      (println "Listening..")
      (catch MidiUnavailableException e (str "Device not available" (.getMessage e)))
      )))
