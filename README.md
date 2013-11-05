clj-signing
===========

A simple library for signing strings and objects.

Usage
=====

First, you'll need a private key. You can generate one using OpenSSL simply:

```shell
openssl genrsa -out my-private.pem 4096
```

The size of the private key will determine how large the signature strings are. It's up to you how many bits of
encryption you want, but for keys and short-lived keys, (such as cookies, access tokens, etc) I use 512 bits
(or even 256).

If you put the private key into your `.jar` resources (using `:resources-path` in your `project.clj`) then all
you need to do to sign an object is this:

```clojure
(ns my.ns
  (:require [steamboat.signing :as signing]))

(signing/with-keypair-resource "my-private.pem"
  (signing/dumps {"key" "value}))
```

`dumps` returns the serialized object and the signature. This can be placed in a cookie, or whatever.

To load it back, use `loads`:

```clojure
(ns my.ns
  (:require [steamboat.signing :as signing]))

(signing/with-keypair-resource "my-private.pem"
  (signing/loads (some-theoretical-function-which-returns-the-original-value)))
```

`loads` will return the deserialized object if it was not tampered with, otherwise it will throw an exception.

Note
====
This library is built on top of BouncyCastle and the standard java Security libraries. However, it's implementation
*MAY* be flawed (i.e. *insecure*), as I have not had anyone review the code for security holes.