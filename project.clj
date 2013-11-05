(defproject org.steamboat/signing "0.0.1"
  :description "Provides facilities for signing string and objects"
  :url "http://ssutch.org/clojure-signed-strings/"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :source-paths ["src/main/clojure"]
  :test-paths ["src/test/clojure"]
  :resource-paths ["src/test/resource"] ;; i only want this for test, not sure how
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [org.bouncycastle/bcprov-jdk16 "1.46"]
                 [commons-codec "1.8"]
                 [cheshire "5.2.0"]]
  :min-lein-version "2.0.0")
