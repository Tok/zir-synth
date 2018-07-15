(ns zir-synth.listener
  (:gen-class)
  (:require [clojure.string :as s]
            [clojure.core.async :refer [go]]
            [zir-synth.synth.oscillator :as osc]
            [zir-synth.util.synth :as zir-synth]
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

(defn audio-format []
  (let [sample-size-in-bits 8 channels 2 signed true big-endian false]
    (AudioFormat. zir-synth/sample-rate-Hz sample-size-in-bits channels signed big-endian)))

(defn note-off [velocities timestamp note]
  (println timestamp "OFF" note (note/note-name note))
  (reset! (get velocities note) 0))

(defn note-on [velocities timestamp note velocity]
  (println timestamp "ON" note velocity (note/note-name note))
  (if (= velocity 0)
    (note-off velocities timestamp note)
    (reset! (get velocities note) velocity)))

(defn zir-receiver []
  (let [audio-format (audio-format)
        sdl (AudioSystem/getSourceDataLine audio-format)
        velocities (into (sorted-map) (map (fn [i] [i (atom (int 0))]) (range 0 127)))]
    (defn- synth-loop [t]
      (doseq [active (filter (fn [[k v]] (> @v 0)) velocities)]
        (let [note (key active)
              velocity @(val active)
              frequency-Hz (note/frequency note)
              volume (* velocity 0.2)
              data (osc/wave-bytes (osc/square t frequency-Hz) volume)
              bytes (byte-array (concat [data] [data]))]
          (.write ^SourceDataLine sdl bytes 0 2)))
      (recur (+ t 1)))
    (defn start-up []
      (println "Starting receiver...")
      (.open sdl audio-format)
      (.start sdl)
      (go (synth-loop 0)))
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
      (close [this]
        (println "Closing...")
        (.drain sdl)
        (.stop sdl)
        (.close sdl)))))

(defn midi-port? [device] (and (not (instance? Sequencer device)) (not (instance? Synthesizer device))))
(defn in-port? [midi-port] (s/includes? (type (.getDeviceInfo midi-port)) "MidiInDevice"))

(defn -main
  "Opens the first active MIDI input device and connects it with a new receiver."
  [& args]
  (let [infos (MidiSystem/getMidiDeviceInfo)
        devices (map #(MidiSystem/getMidiDevice %) infos)
        midi-ports (filter midi-port? devices)
        in-port (first (filter in-port? midi-ports))
        synth-transmitter (.getTransmitter ^MidiDevice in-port)
        zir-transmitter (.getTransmitter ^MidiDevice in-port)
        synth (MidiSystem/getSynthesizer)
        rec (zir-receiver)]
    (.setReceiver synth-transmitter (.getReceiver synth))
    (.setReceiver zir-transmitter rec)
    (start-up)
    (println "Trying to open device" (.toString (.getDeviceInfo in-port)))
    (try
      (.open synth)
      (.open in-port)
      (println "Listening..")
      (catch MidiUnavailableException e (str "Device not available" (.getMessage e)))
      )))
