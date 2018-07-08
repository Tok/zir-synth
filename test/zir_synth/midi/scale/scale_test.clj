(ns zir-synth.midi.scale.scale-test
  (:require [clojure.test :refer :all]
            [zir-synth.midi.scale.major :as major]
            [zir-synth.midi.scale.minor :as minor]
            [zir-synth.midi.scale.scale :refer :all]))

(deftest major-triad-test
  (testing "c-major triad" (is (= (triad (major/scale :C)) [:C :E :G])))
  (testing "f-major triad" (is (= (triad (major/scale :F)) [:F :A :C])))
  (testing "g-major triad" (is (= (triad (major/scale :G)) [:G :B :D]))))

(deftest minor-triad-test
  (testing "d-minor triad" (is (= (triad (minor/scale :D)) [:D :F :A])))
  (testing "e-minor triad" (is (= (triad (minor/scale :E)) [:E :G :B])))
  (testing "a-minor triad" (is (= (triad (minor/scale :A)) [:A :C :E]))))

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
    (testing (is (scales-shifted? (first v) (second v))))))
