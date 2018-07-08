(defproject zir-synth "0.1.0-SNAPSHOT"
  :description "Playground for sonic creativity and audio technology research."
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/algo.generic "0.1.2"]
                 [org.clojure/tools.logging "0.4.0"]]
  :main ^:skip-aot zir-synth.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
