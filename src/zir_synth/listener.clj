(ns zir-synth.listener
  (:gen-class)
  (:require [clojure.string :as s]
            [zir-synth.receiver.stream.approx :as approx])
  (:import (javax.sound.midi MidiDevice)
           (javax.sound.midi MidiSystem)
           (javax.sound.midi MidiUnavailableException)
           (javax.sound.midi Sequencer)
           (javax.sound.midi Synthesizer)
           ))

(defn- midi-port? [device] (and (not (instance? Sequencer device)) (not (instance? Synthesizer device))))
(defn- in-port? [midi-port] (s/includes? (type (.getDeviceInfo midi-port)) "MidiInDevice"))

(defn- infos [] (MidiSystem/getMidiDeviceInfo))
(defn- devices [] (map #(MidiSystem/getMidiDevice %) (infos)))
(defn- midi-ports [] (filter midi-port? (devices)))
(defn- find-in-port [] (first (filter in-port? (midi-ports))))

(defn -main
  "Opens the first active MIDI input device and connects it with receivers."
  [& args]
  (let [port (find-in-port)
        synth-transmitter (.getTransmitter ^MidiDevice port)
        zir-transmitter (.getTransmitter ^MidiDevice port)
        synth (MidiSystem/getSynthesizer)
        default-rec (.getReceiver synth)
        approx-rec (approx/receiver)]
    (.setReceiver synth-transmitter default-rec)
    (.setReceiver zir-transmitter approx-rec)
    (approx/start-up)
    (println "Trying to open device" (.toString (.getDeviceInfo port)))
    (try
      (.open synth)
      (.open port)
      (println "Listening..")
      (catch MidiUnavailableException e (str "Device not available" (.getMessage e)))
      )))
