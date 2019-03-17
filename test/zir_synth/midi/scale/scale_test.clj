(ns zir-synth.midi.scale.scale-test
  (:require [clojure.test :refer :all]
            [zir-synth.test-util :refer :all]
            [zir-synth.midi.scale.major :as major]
            [zir-synth.midi.scale.minor :as minor]
            [zir-synth.midi.scale.scale :refer :all]))

(deftest major-triad-test
  (test= "c-major triad" (triad (major/scale :C)) [:C :E :G])
  (test= "f-major triad" (triad (major/scale :F)) [:F :A :C])
  (test= "g-major triad" (triad (major/scale :G)) [:G :B :D]))

(deftest minor-triad-test
  (test= "d-minor triad" (triad (minor/scale :D)) [:D :F :A])
  (test= "e-minor triad" (triad (minor/scale :E)) [:E :G :B])
  (test= "a-minor triad" (triad (minor/scale :A)) [:A :C :E]))

;https://en.wikipedia.org/wiki/Relative_key
(deftest relative-keys-test
  (doseq
    [v [[(major/c-major) (minor/a-minor)]
        [(major/g-major) (minor/e-minor)]
        [(major/d-major) (minor/b-minor)]
        [(major/a-major) (minor/f-sharp-minor)]
        [(major/e-major) (minor/c-sharp-minor)]
        [(major/b-major) (minor/g-sharp-minor)]
        [(major/f-sharp-major) (minor/d-sharp-minor)]
        [(major/g-flat-major) (minor/e-flat-minor)]
        [(major/d-flat-major) (minor/b-flat-minor)]
        [(major/a-flat-major) (minor/f-minor)]
        [(major/e-flat-major) (minor/c-minor)]
        [(major/b-flat-major) (minor/g-minor)]
        [(major/f-major) (minor/d-minor)]]]
    (do-test (scales-shifted? (first v) (second v)))))
