(ns zir-synth.midi.scale.major-test
  (:require [clojure.test :refer :all]
            [zir-synth.midi.scale.major :refer :all]
            [zir-synth.midi.scale.scale :refer :all]))

(deftest major-scales-equality-test
  (doseq
    [v [[(c-sharp-major) (d-flat-major)]
        [(d-sharp-major) (e-flat-major)]
        [(f-sharp-major) (g-flat-major)]
        [(g-sharp-major) (a-flat-major)]
        [(a-sharp-major) (b-flat-major)]]]
    (testing (is (scales-equal? (first v) (second v))))))

(deftest major-key-note-test
  (doseq
    [v [[(scale :C) (c-major)] [(scale :C#) (c-sharp-major)] [(scale :Db) (d-flat-major)]
        [(scale :D) (d-major)] [(scale :D#) (d-sharp-major)] [(scale :Eb) (e-flat-major)]
        [(scale :E) (e-major)]
        [(scale :F) (f-major)] [(scale :F#) (f-sharp-major)] [(scale :Gb) (g-flat-major)]
        [(scale :G) (g-major)] [(scale :G#) (g-sharp-major)] [(scale :Ab) (a-flat-major)]
        [(scale :A) (a-major)] [(scale :A#) (a-sharp-major)] [(scale :Bb) (b-flat-major)]
        [(scale :B) (b-major)]]]
    (testing (is (scales-equal? (first v) (second v))))))
