(ns zir-synth.test-suite
  (:require [clojure.test :refer :all]
            [zir-synth.util.math-test :as math-test]
            [zir-synth.midi.note-test :as note-test]
            [zir-synth.midi.chord-test :as chord-test]
            [zir-synth.midi.scale.scale-test :as scale-test]
            [zir-synth.midi.scale.major-test :as major-test]
            [zir-synth.midi.scale.minor-test :as minor-test]))

(run-all-tests #"zir-synth.*")
