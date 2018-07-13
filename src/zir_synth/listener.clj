(ns zir-synth.listener
  (:gen-class)
  (:require [clojure.string :as s])
  (:import (javax.sound.midi MidiDevice)
           (javax.sound.midi MidiSystem)
           (javax.sound.midi MidiUnavailableException)
           (javax.sound.midi Receiver)
           (javax.sound.midi Sequencer)
           (javax.sound.midi Synthesizer)
           (javax.sound.sampled AudioFormat)
           (javax.sound.sampled AudioSystem)
           (javax.sound.sampled SourceDataLine)
           ))

(defn zir-receiver []
  (reify Receiver
    (send [this message timestamp]
      (println "sending..." message timestamp)              ;TODO
      )
    (close [this]
      (println "closing...")                                ;TODO
      )
    ))

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
