(ns zir-synth.midi.note
  (:gen-class)
  (:require [zir-synth.util.math :as math-util]))

(defn- names [] {0  :C 1 :C#/Db
                 2  :D 3 :D#/Eb
                 4  :E
                 5  :F 6 :F#/Gb
                 7  :G 8 :G#/Ab
                 9  :A 10 :A#/Bb
                 11 :B})

(defn- offsets [] {:C     0 :c 0 :B# 0 :Dbb 0
                   :C#/Db 1 :C# 1 :c# 1 :Db 1 :db 1
                   :D     2 :d 2 :C## 2 :Ebb 2
                   :D#/Eb 3 :D# 3 :d# 3 :Eb 3 :eb 3
                   :E     4 :e 4 :D## 4 :Fb 4
                   :F     5 :f 5 :E# 5 :Gbb 5
                   :F#/Gb 6 :F# 6 :f# 6 :Gb 6 :gb 6
                   :G     7 :g 7 :F## 7 :Abb 7
                   :G#/Ab 8 :G# 8 :g# 8 :Ab 8 :ab 8
                   :A     9 :a 9 :G## 9 :Bbb 9
                   :A#/Bb 10 :A# 10 :a# 10 :Bb 10 :bb 10
                   :B     11 :b 11 :A## 11 :Cb 11})

(defn normalize [note] (get names (get offsets note)))

(defn note-name [note] (let [remainder (mod note 12)] (get (names) remainder)))
(defn offset [note-name] (get (offsets) note-name))

(defn midi-note [octave note]
  (let [oct (int (* (+ octave 5) 12))
        offset (offset note)]
    (+ oct offset)))

(defn midi-progression [start-octave notes]
  (let [key-note (first notes)
        key-note-midi (midi-note start-octave key-note)
        offsets (map offset notes)
        first-offset (offset key-note)
        mod-offsets (conj (vec (take (- (count notes) 1) (map (fn [o] (mod (- o first-offset) 12)) offsets))) 12)]
    (vec (map (fn [note-offset] (+ note-offset key-note-midi)) mod-offsets))))

(defn octave [n] (- (int (/ n 12)) 5))

(defn to-names [notes] (vec (map note-name notes)))

(defn notes-equal? [first second] (= (offset first) (offset second)))

;https://en.wikipedia.org/wiki/MIDI_tuning_standard#Frequency_values
;https://en.wikipedia.org/wiki/A440_(pitch_standard)
(defn frequency [midi-note]
  "Calculates the frequency of a midi note number in Hz."
  (let [base-note 69
        offset (- midi-note base-note)
        standard-frequency 440]
    (if (= 0 offset)
      standard-frequency
      (* standard-frequency (math-util/interval-coefficient offset)))))

(defn notes-from-name [name] (sort (vec (filter (fn [n] (= (note-name n) name)) (range 0 (+ 127 1))))))
