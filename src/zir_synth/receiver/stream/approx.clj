(ns zir-synth.receiver.stream.approx
  (:gen-class)
  (:require [clojure.core.async :refer [go go-loop <! timeout]]
            [zir-synth.synth.oscillator :as osc]
            [zir-synth.util.synth :as zir-synth]
            [zir-synth.util.math :as zir-math]
            [zir-synth.midi.note :as note]
            [clojure.tools.logging :as log])
  (:import (javax.sound.midi Receiver)
           (javax.sound.midi ShortMessage)
           (javax.sound.sampled AudioSystem)
           (javax.sound.sampled SourceDataLine)
           ))

(defn receiver []
  (let [use-timeout? true
        use-rep? true
        wave-form :sine
        start-nano (System/nanoTime)
        velocities (into (sorted-map) (map (fn [i] [i (atom (int 0))]) (range 0 127)))]
    (defn- synth-loop [sdl i]
      (go-loop [i 0 last-tick 0]
        (let [active (filter (fn [[k v]] (> @v 0)) velocities)
              active? (> (count active) 0)
              d-nano (- (System/nanoTime) start-nano)
              tick (int (mod (/ d-nano zir-synth/ns-per-sample) zir-synth/ns-per-sample))]
          (if active?
            (let [cnt (count active)
                  angles (map (fn [[note velo]] (osc/calc-amplitude tick wave-form note @velo)) active)
                  angle (mod (reduce (fn [accu angle] (+ accu angle)) 0 angles) zir-math/tau)]
              (log/debug i "count" cnt "tick" tick "angle" angle)
              (if (false? (.isActive sdl)) (.start sdl))
              (if use-timeout? (<! (timeout (/ 1 zir-synth/sample-rate-Hz))))
              (if use-rep?
                  (repeat (- tick last-tick) (.write ^SourceDataLine sdl (osc/wave-bytes angle) 0 2))
                  (.write ^SourceDataLine sdl (osc/wave-bytes angle) 0 2)
                  )
              )
            (do (.drain sdl) (.stop sdl))
            )
          (recur (inc i) tick))
        ))
    (defn note-off [timestamp note]
      (println timestamp "OFF" note (note/note-name note))
      (reset! (get velocities note) 0))
    (defn note-on [timestamp note velocity]
      (println timestamp "ON" note velocity (note/note-name note))
      (if (= velocity 0)
        (note-off timestamp note)
        (reset! (get velocities note) velocity)))
    )
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
          (log/warn "Unhandled command" command))))
    (close [this] (println "Closing..."))
    ))

(defn start-up []
  (let [audio-format (zir-synth/audio-format)
        sdl (AudioSystem/getSourceDataLine audio-format)]
    (println "Starting receiver...")
    (.open sdl audio-format)
    (synth-loop sdl 0)
    ;(.close sdl)
    ))
