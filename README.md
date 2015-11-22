# sv.system

A library to develop component-based systems in Clojure.

## Why

A short feedback loop is essential for the productivity of a
developer. Tools should interrupt the development flow as little as
possible. The ideal is an interactive development experience, which is
for example provided by the great Clojure tool
[figwheel](https://github.com/bhauman/lein-figwheel):

https://www.youtube.com/watch?v=KZjFVdU8VLI

A very advanced form of interactive development is demonstrated by
Bret Victor here:

https://www.youtube.com/watch?v=-QJytzcd7Wo

A lot of development environments (I'm looking at you Java) burden the
developer with long startup times. The Clojure REPL eliminates this
problem and provides you with an interactive development experience on
top of the JVM, which is pretty awesome. But even with this mighty
tool you accumulate more and more state / changes over time, until you
reach a point, where you like to start with a fresh state. Normally
you stop and restart your application. But this could take from
anywhere between ten seconds and a minute, depending on your hardware
and your application.

One of the most compelling reasons to use a component-based system is
that it provides you essentially with a reset button for your
application, which takes you almost instantly back to a defined state
of your application and it doesn't need to restart the JVM.

Stuart Sierra pioneered this approach for the Clojure ecosystem and
created the [component
library](https://github.com/stuartsierra/component/) as an
implementation of the approach. In the meanwhile the Clojure ecosystem
yielded serveral other implementations (like
[leaven](https://github.com/palletops/leaven/) or
[yoyo](https://github.com/jarohen/yoyo)) and libraries that are based
on component ([modular](https://github.com/juxt/modular/) or
[system](https://github.com/danielsz/system) for example).

sv.system is only my two cents for this problem space of
component-based Clojure system.

## Design goals

I had the following goals in mind, while designing the library:

- Create an experience with a low complexity overhead, so that even
  Clojure beginners might consider to build a component-based system.

- Be pure, use plain functions, wherever it is possible (instead of
  records and protocols)

- Make dependencies a central and declarative aspect of the approach.

- Favor conventions over configuration to provide common application
  components, which are convenient to use.

## Usage

FIXME

## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
