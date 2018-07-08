(ns zir-synth.midi.chord
  (:gen-class))

;https://en.wikipedia.org/wiki/List_of_chords
(def chords {:major      [:I :IV :V]
             :minor      [:ii :iii :vi]
             :diminished [:vii°]})

(defn- filter-scale [scale notes] (vec (map (fn [i] (nth scale (- i 1))) notes)))
(defn major-triad [scale] (filter-scale scale [1 3 5]))
(defn major-seventh [scale] (filter-scale scale [1 3 5 7]))

(defn- random-triad-impl [selection accu]
  (if (= (count accu) 3)
    (vec accu)
    (recur selection (conj accu (rand-nth selection)))))
(defn random-triad [selection] (random-triad-impl selection (list)))
