(ns zir-synth.instrument.default
  (:gen-class
    :extends
    :name zir-synth.instrument.DefaultInstrument))

(proxy [javax.sound.midi.Instrument] []
  method-redefinition-1
  ...
  method-redefinition-N)
