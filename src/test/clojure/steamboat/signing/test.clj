(ns steamboat.signing.test
  (:use clojure.test)
  (:require [steamboat.signing :as signing]))

;; yeah, these tests are pretty lame...

(def tstructure {"key" "hello"})

(deftest basic-serialization-and-deserialization
  (signing/with-keypair-resource "test-private.pem"
    (let [signed (signing/dumps tstructure)
          designed (signing/loads signed)]
      (is (= tstructure designed)))))