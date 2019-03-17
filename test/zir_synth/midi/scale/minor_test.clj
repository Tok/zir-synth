(ns zir-synth.midi.scale.minor-test
  (:require [clojure.test :refer :all]
            [zir-synth.test-util :refer :all]
            [zir-synth.midi.scale.minor :refer :all]
            [zir-synth.midi.scale.scale :refer :all]))

(deftest minor-scales-equality-test
  (doseq
    [v [[(a-sharp-minor) (b-flat-minor)]
        [(c-sharp-minor) (d-flat-minor)]
        [(d-sharp-minor) (e-flat-minor)]
        [(f-sharp-minor) (g-flat-minor)]
        [(g-sharp-minor) (a-flat-minor)]]]
    (do-test (scales-equal? (first v) (second v)))))

(deftest minor-key-note-test
  (doseq
    [v [[(scale :A) (a-minor)] [(scale :A#) (a-sharp-minor)] [(scale :Bb) (b-flat-minor)]
        [(scale :B) (b-minor)]
        [(scale :C) (c-minor)] [(scale :C#) (c-sharp-minor)] [(scale :Db) (d-flat-minor)]
        [(scale :D) (d-minor)] [(scale :D#) (d-sharp-minor)] [(scale :Eb) (e-flat-minor)]
        [(scale :E) (e-minor)]
        [(scale :F) (f-minor)] [(scale :F#) (f-sharp-minor)] [(scale :Gb) (g-flat-minor)]
        [(scale :G) (g-minor)] [(scale :G#) (g-sharp-minor)] [(scale :Ab) (a-flat-minor)]]]
    (do-test (scales-equal? (first v) (second v)))))
