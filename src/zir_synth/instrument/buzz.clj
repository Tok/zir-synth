(ns zir-synth.instrument.buzz
  (:gen-class)
  (:import (javax.sound.sampled AudioFormat)
           (javax.sound.sampled AudioSystem)
           (javax.sound.sampled SourceDataLine))
  (:require [zir-synth.util.math :refer :all]
            [zir-synth.util.synth :refer :all]
            [zir-synth.midi.note :as note]))

(defn write! [sdl sound-byte angle volume]
  (let [harmonized (byte (* (Math/sin (* angle 2)) (/ volume phi)))]
    (.write ^SourceDataLine sdl (byte-array (concat [sound-byte] [harmonized])) 0 2)))

(defn play! [notes volume duration-ms]
  (let [audio-format (AudioFormat. sample-rate-Hz 8 2 true false)
        sdl (AudioSystem/getSourceDataLine audio-format)
        octave 0                                            ;TODO
        hz-seq (map note/frequency (map (fn [n] (note/midi-note octave n)) notes))]
    (.open sdl audio-format)
    (.start sdl)
    (doseq [buzz-hz hz-seq]
      (dotimes [i (calc-times sample-rate-Hz duration-ms)]
        (let [an (calc-angle sample-rate-Hz buzz-hz i)
              by (sound-byte an volume)]
          (write! sdl by an volume)))
      )
    (.drain sdl)
    (.stop sdl)
    (.close sdl)))
