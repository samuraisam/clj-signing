(ns steamboat.signing
  "Simple library for signing strings, objects, etc. Warning: While the security functions in use seem
  basic, this library has not been audited for sound security. As always, follow good security practices
  with your private keys."
  {:author "Samuel Sutch"}
  (import
    (java.io StringReader)
    (java.security Security Signature)
    (org.apache.commons.codec.binary Base64)
    (org.bouncycastle.jce.provider BouncyCastleProvider)
    (org.bouncycastle.openssl PEMReader))
  (:use [cheshire.core :as json])
  (:require [clojure.string :as string]))

;; hook up the BouncyCastleProvider which makes PEM things easier for us
(Security/addProvider (BouncyCastleProvider.))

(defn get-keypair
  "Get a KeyPair from a PEM in a string"
  [pem]
  (let [sr (StringReader. pem) ;; have to create a StringReader
        pemreader (PEMReader. sr)] ;; and then create a PEMReader
    (.readObject pemreader))) ;; and then read that. point. java

(defn get-keypair-filename
  "Return a keypair from the file named `fn`"
  [filename]
  (get-keypair (slurp filename)))

(defn sign
  "Sign bytes `b` given a KeyPair `keypair` and a String"
  [keypair b]
  (let [sig (doto
              (Signature/getInstance "SHA1withRSA")
              (.initSign (.getPrivate keypair))  ;; initSign w/ the private key from the keypair
              (.update b))] ;; and add the  bytes to it
    (.sign sig)))

(defn verify
  "Verify that a base64 encoded byte array (v) is a valid signed string"
  [keypair b v]
  (let [sig (doto
              (Signature/getInstance "SHA1withRSA")
              (.initVerify (.getPublic keypair)) ;; initSign w/ the private key from the keypair
              (.update b))] ;; and add the bytes to it
    (.verify sig v))) ;; now verify the bytes passed in `v`

;; the current keypair for with-keypair
(def ^:dynamic keypair)

(defmacro with-keypair
  "Run functions that expect a pre-defined keypair"
  [kp & body]
  `(binding [keypair ~kp]
     (do ~@body)))

(defmacro with-keypair-filename
  "Run functions that expect a pre-defined keypair"
  [filename & body]
  `(binding [keypair (get-keypair-filename ~filename)]
     (do ~@body)))

(defmacro with-keypair-resource
  "Run functions that expect a pre-defined keypair"
  [resourcename & body]
  `(binding [keypair (get-keypair-filename (clojure.java.io/resource ~resourcename))]
     (do ~@body)))

(defn sign-obj
  "Signs a map as a url safe base64 encoded string"
  [ob]
  (Base64/encodeBase64URLSafeString (sign keypair (json/generate-smile ob))))

(defn dumps
  "Dump an object into a signed string. Serializes the object into the
  string so it can later be retrieved."
  [ob]
  (string/join ":"
    [(Base64/encodeBase64URLSafeString (json/generate-smile ob))
     (sign-obj ob)]))

(defn loads
  "Load an object from a signed string and verify it.
  Returns nil if not valid, otherwise returns the deserialized object."
  [s]
  (let [val (string/split s #":" 2)
        b (Base64/decodeBase64 (nth val 0))]
    (cond
      (verify keypair b (Base64/decodeBase64 (nth val 1))) (json/decode-smile b)
      :else nil)))