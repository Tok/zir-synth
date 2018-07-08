(ns zir-synth.instrument.buzz
  (:gen-class)
  (:import (javax.sound.sampled AudioFormat)
           (javax.sound.sampled AudioSystem)
           (javax.sound.sampled SourceDataLine))
  (:require [zir-synth.util.math :refer :all]
            [zir-synth.util.synth :refer :all]))

(defn write! [sdl sound-byte harmonic? angle volume]
  (let [n (n-channels harmonic?)]
    (if harmonic?
      (let [harmonized (byte (* (Math/sin (* angle 2)) (/ volume phi)))]
        (.write ^SourceDataLine sdl (byte-array (concat [sound-byte] [harmonized])) 0 n))
      (.write ^SourceDataLine sdl (byte-array [sound-byte]) 0 n))))

(defn play! [hz volume duration-ms harmonic?]
  (let [n (n-channels harmonic?)
        audio-format (AudioFormat. sample-rate-Hz 8 n true false)
        sdl (AudioSystem/getSourceDataLine audio-format)]
    (.open sdl audio-format)
    (.start sdl)
    (dotimes [i (calc-times sample-rate-Hz duration-ms)]
      (let [a (calc-angle sample-rate-Hz hz i)
            b (sound-byte a volume)]
        (write! sdl b harmonic? a volume)))
    (.drain sdl)
    (.stop sdl)
    (.close sdl)))
