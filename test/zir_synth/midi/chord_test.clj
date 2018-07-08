(ns zir-synth.midi.chord-test
  (:require [clojure.test :refer :all]
            [zir-synth.midi.chord :refer :all]
            [zir-synth.midi.note :as note]
            [zir-synth.midi.scale.minor :as minor]
            [zir-synth.midi.scale.major :as major]))

;https://en.wikipedia.org/wiki/List_of_chords

(deftest minor-test (testing "a-minor" (is (= (minor/scale :A) [:A :B :C :D :E :F :G :A]))))
(deftest major-test (testing "c-major" (is (= (major/scale :C) [:C :D :E :F :G :A :B :C]))))

(deftest minor-triad-random-test
  (let [name "a-minor"
        scale (minor/scale :A)
        triad (random-triad scale)]
    (testing "count" (is (= (count triad) 3)))
    (testing name (contains? scale (nth triad 0)))
    (testing name (contains? scale (nth triad 1)))
    (testing name (contains? scale (nth triad 2)))))

(deftest major-triad-random-test
  (let [name "c-major"
        scale (major/scale :C)
        triad (random-triad scale)]
    (testing "count" (is (= (count triad) 3)))
    (testing name (contains? scale (nth triad 0)))
    (testing name (contains? scale (nth triad 1)))
    (testing name (contains? scale (nth triad 2)))))

(defn- triad? [scale-fn expected]
  (let [normalized-exp (vec (map note/normalize expected))
        root (nth expected 0)
        triad (major-triad (scale-fn root))
        normalized (vec (map note/normalize triad))]
    (= normalized normalized-exp)))

;https://en.wikipedia.org/wiki/Major_chord
(deftest major-triad-test
  (doseq
    [triad
     [[:C :E :G] [:C# :E# :G#] [:Db :F :Ab]
      [:D :F# :A] [:D# :F## :A#] [:Eb :G :Bb]
      [:E :G# :B]
      [:F :A :C] [:F# :A# :C#] [:Gb :Bb :Db]
      [:G :B :D] [:G# :B# :D#] [:Ab :C :Eb]
      [:A :C# :E] [:A# :C## :E#] [:Bb :D :F]
      [:B :D# :F#]]]
    (testing (is (triad? major/scale triad)))))

;https://en.wikipedia.org/wiki/Minor_chord
(deftest minor-triad-test
  (doseq
    [triad
     [[:C :Eb :G] [:C# :E :G#] [:Db :Fb :Ab]
      [:D :F :A] [:D# :F# :A#] [:Eb :Gb :Bb]
      [:E :G :B]
      [:F :Ab :C] [:F# :A :C#] [:Gb :Bbb :Db]
      [:G :Bb :D] [:G# :B :D#] [:Ab :Cb :Eb]
      [:A :C :E] [:A# :C# :E#] [:Bb :Db :F]
      [:B :D :F#]]]
    (testing (is (triad? minor/scale triad)))))

(defn- seventh? [scale-fn expected]
  (let [normalized-exp (vec (map note/normalize expected))
        root (nth expected 0)
        triad (major-seventh (scale-fn root))
        normalized (vec (map note/normalize triad))]
    (= normalized normalized-exp)))

;https://en.wikipedia.org/wiki/Major_seventh_chord
(deftest major-seventh-test
  (doseq
    [seventh
     [[:C :E :G :B] [:C# :E# :G# :B#] [:Db :F :Ab :C]
      [:D :F# :A :C#] [:D# :F## :A# :C##] [:Eb :G :Bb :D]
      [:E :G# :B :D#]
      [:F :A :C :E] [:F# :A# :C# :E#] [:Gb :Bb :Db :F]
      [:G :B :D :F#] [:G# :B# :D# :F##] [:Ab :C :Eb :G]
      [:A :C# :E :G#] [:A# :C## :E# :G##] [:Bb :D :F :A]
      [:B :D# :F# :A#]]]
    (testing (is (seventh? major/scale seventh)))))

;https://en.wikipedia.org/wiki/Minor_seventh_chord
(deftest minor-seventh-test
  (doseq
    [seventh
     [[:C :Eb :G :Bb] [:C# :E :G# :B] [:Db :Fb :Ab :Cb]
      [:D :F :A :C] [:D# :F# :A# :C#] [:Eb :Gb :Bb :Db]
      [:E :G :B :D]
      [:F :Ab :C :Eb] [:F# :A :C# :E] [:Gb :Bbb :Db :Fb]
      [:G :Bb :D :F] [:G# :B :D# :F#] [:Ab :Cb :Eb :Gb]
      [:A :C :E :G] [:A# :C# :E# :G#] [:Bb :Db :F :Ab]
      [:B :D :F# :A]]]
    (testing (is (seventh? minor/scale seventh)))))
