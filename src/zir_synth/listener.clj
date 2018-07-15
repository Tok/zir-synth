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
           (javax.sound.sampled AudioSystem)
           (javax.sound.sampled SourceDataLine)
           ))

(defn zir-receiver []
  (let [audio-format (zir-synth/audio-format)
        sdl (AudioSystem/getSourceDataLine audio-format)
        velocities (into (sorted-map) (map (fn [i] [i (atom (int 0))]) (range 0 127)))]
    (defn- calc-data [t note velocity]
      (let [frequency-Hz (note/frequency note)
            volume (* velocity 0.2)]                        ;TODO
        (osc/wave-bytes (osc/square t frequency-Hz) volume))
      )
    (defn- synth-loop [t]
      (doseq [active (filter (fn [[k v]] (> @v 0)) velocities)]
        (let [data (calc-data t (key active) @(val active))
              bytes (byte-array (concat [data] [data]))]
          (.write ^SourceDataLine sdl bytes 0 2)))
      (recur (+ t 1)))
    (defn start-up []
      (println "Starting receiver...")
      (.open sdl audio-format)
      (.start sdl)
      (go (synth-loop 0)))
    (defn note-off [timestamp note]
      (println timestamp "OFF" note (note/note-name note))
      (reset! (get velocities note) 0))
    (defn note-on [timestamp note velocity]
      (println timestamp "ON" note velocity (note/note-name note))
      (if (= velocity 0)
        (note-off timestamp note)
        (reset! (get velocities note) velocity)))
    (reify Receiver
      (send [_ message timestamp]
        (let [command (int (.getCommand message))
              channel (int (.getChannel message))]
          (if (or (= command ShortMessage/NOTE_ON) (= command ShortMessage/NOTE_OFF))
            (let [note (.getData1 message)
                  velocity (.getData2 message)]
              (cond
                (= command ShortMessage/NOTE_ON) (note-on timestamp note velocity)
                (= command ShortMessage/NOTE_OFF) (note-off timestamp note)))
            (println "WARNING: Unhandled command" command))))
      (close [this]
        (println "Closing...")
        (.drain sdl)
        (.stop sdl)
        (.close sdl)))))

(defn- midi-port? [device] (and (not (instance? Sequencer device)) (not (instance? Synthesizer device))))
(defn- in-port? [midi-port] (s/includes? (type (.getDeviceInfo midi-port)) "MidiInDevice"))

(defn- infos [] (MidiSystem/getMidiDeviceInfo))
(defn- devices [] (map #(MidiSystem/getMidiDevice %) (infos)))
(defn- midi-ports [] (filter midi-port? (devices)))
(defn- find-in-port [] (first (filter in-port? (midi-ports))))

(defn -main
  "Opens the first active MIDI input device and connects it with a new receiver."
  [& args]
  (let [port (find-in-port)
        synth-transmitter (.getTransmitter ^MidiDevice port)
        zir-transmitter (.getTransmitter ^MidiDevice port)
        synth (MidiSystem/getSynthesizer)
        rec (zir-receiver)]
    (.setReceiver synth-transmitter (.getReceiver synth))
    (.setReceiver zir-transmitter rec)
    (start-up)
    (println "Trying to open device" (.toString (.getDeviceInfo port)))
    (try
      (.open synth)
      (.open port)
      (println "Listening..")
      (catch MidiUnavailableException e (str "Device not available" (.getMessage e)))
      )))
