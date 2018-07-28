(ns zir-synth.receiver.single
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
  (defn note-on [sdl timestamp note velocity]
    (if (false? (.isActive sdl)) (.start sdl))
    (println timestamp "velocity" velocity "note" note (note/note-name note))
    (if (not= velocity 0)
      (go
        (let [frequency-Hz (note/frequency note)
              duration-ms 1000]
          (dotimes [i (zir-synth/calc-times zir-synth/sample-rate-Hz duration-ms)]
            (let [angle (mod (zir-synth/calc-angle zir-synth/sample-rate-Hz frequency-Hz i) zir-math/tau)
                  sound-byte (zir-synth/sound-byte angle velocity)
                  harmonized (byte (* (Math/sin (* angle 2)) velocity))]
              (.write ^SourceDataLine sdl (byte-array (concat [sound-byte] [harmonized])) 0 2)
              ))))
      (.drain sdl)
      ))
  (reify Receiver
    (send [_ message timestamp]
      (let [command (int (.getCommand message))
            channel (int (.getChannel message))
            audio-format (zir-synth/audio-format)
            sdl (AudioSystem/getSourceDataLine audio-format)]
        (.open sdl audio-format)
        (if (or (= command ShortMessage/NOTE_ON) (= command ShortMessage/NOTE_OFF))
          (let [note (.getData1 message)
                velocity (.getData2 message)]
            (cond
              (= command ShortMessage/NOTE_ON) (note-on sdl timestamp note velocity)
              (= command ShortMessage/NOTE_OFF) (go (println timestamp note "off") (.stop sdl)))
            )
          (log/warn "Unhandled command" command))))
    (close [this]
      (println "Closing...")
      (.stop (AudioSystem/getSourceDataLine (zir-synth/audio-format)))
      )))
