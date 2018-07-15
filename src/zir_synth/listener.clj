(ns zir-synth.listener
  (:gen-class)
  (:require [clojure.string :as s]
            [clojure.core.async :refer [go]]
            [zir-synth.synth.oscillator :as osc]
            [zir-synth.util.synth :as zir-synth]
            [zir-synth.util.math :as zir-math]
            [zir-synth.midi.note :as note]
            [clojure.tools.logging :as log])
  (:import (java.lang System)
           (javax.sound.midi MidiDevice)
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
  (let [start-nano (System/nanoTime)
        velocities (into (sorted-map) (map (fn [i] [i (atom (int 0))]) (range 0 127)))]
    (defn- synth-loop [sdl i]
      (let [active (filter (fn [[k v]] (> @v 0)) velocities)
            active? (> (count active) 0)]
        (if active?
          (let [cnt (count active)
                wave-form :sine
                now-nano (System/nanoTime)
                t-nano (- now-nano start-nano)
                tick (mod (/ t-nano zir-synth/ns-per-sample) zir-synth/ns-per-sample)
                angles (map (fn [[note velo]] [(osc/angle tick note) @velo]) active)
                raw-angle (reduce (fn [m [angle velo]] [(+ (first m) angle) (+ (second m) velo)]) [0 0] angles)
                angle [(mod (first raw-angle) zir-math/tau) (/ (second raw-angle) cnt)]
                wave (osc/calculate-wave wave-form (first angle))
                volume (second angle)
                wave-bytes (osc/wave-bytes wave volume)]
            (log/debug i "tick" (long tick) "count" cnt "angle" (first angle) "wave" wave "volume" volume)
            (if (false? (.isActive sdl))
              (.start sdl))
            (.write ^SourceDataLine sdl wave-bytes 0 2)
            )
          (do (.drain sdl) (.stop sdl))
          )
        (recur sdl (+ i 1))
        ))
    (defn start-up []
      (let [audio-format (zir-synth/audio-format)
            sdl (AudioSystem/getSourceDataLine audio-format)]
        (println "Starting receiver...")
        (.open sdl audio-format)
        (go
          (synth-loop sdl 0)
          (.close sdl))))
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
      (close [this] (println "Closing...")))))

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
