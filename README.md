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
component-based Clojure systems.

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

A minimal component is defined like this:

  (def component {:binds [:component-a]
                  :start [identity 1]})

To start a system:

  (use 'sv.system.core)

  (start-system [component])

This will return:

{:component-a 1}

For sure this is pretty useless and we will look at a real world
example in a minute, but it shows the basic building blocks. The
`start-system` will always return a map, which contains the complete
started system. Here the component states that it will bind the path
`[:component-a]` in the system map. The value of :start is a vector,
which first element is a function (here
[clojure.core/identity](https://clojuredocs.org/clojure.core/identity))
. This function will be invoked with the given arguments (the rest of
the vector, here `1`). The return value (here: `1`) will be the value
of the path (here: `[:component-a]`
cf. [assoc-in](https://clojuredocs.org/clojure.core/assoc-in)).

Ok let's build something more interesting for the real world. One big
issue that is handled by the component library is the management of
stateful objects, which have a lifecylce. An example is a HTTP server
that binds a port of your operation system and that has to be stopped
appropriately to free the port and other resources. For the code
sample we will use [httpkit](http://www.http-kit.org/) a minimal HTTP
server for Clojure:

       (use 'org.httpkit.server)

       (defn start [ring-handler opts]
         (run-server
            ring-handler
            opts))

       (defn stop [stop-httpkit]
         (stop-httpkit))

       (defn httpkit-server []
         {:binds [:httpkit :server]
          :start [start [:ring :handler] {:port 8080}]
          :stop stop})

Here you see the first dependency `[:ring :handler]`, which is
declared under :start. The libary will fetch the value under this path
in the system map and will substitute the path in the :start
arguments with the value
(cf. [get-in](https://clojuredocs.org/clojure.core/get-in)).

The library uses these dependency declarations (here `[:ring
:handler]`) to figure out an apporpriate start order of the
components. Therefore we need a component, which binds `[:ring
:handler]`:

        (defn hello-handler-fn []
          (fn [request]
            {:status 200
             :body "Hello"
             :content-type "text/plain"}))

        (defn hello-handler []
          {:binds [:ring :handler]
           :start [hello-handler-fn]})


The `hello-handler` component just binds a Ring handler to the `[:ring
:handler]` path in the system map:

        {:ring {:handler (hello-handler-fn)}}

Now we can start the system:

    (def system
      (start-system #{(hello-handler) (httpkit-server)}))

Open `http://localhost:8080/` in your browser to get your Hello
message. Notice that the order of the components in the start-system
call does not matter, since an appropriate order is automatically
calculated by the library.

For sure we also like to stop the system:

    (stop-system system)

This works since the `start-system` functions adds the calculated
start order as meta data to the system map. This order is reversed and
used by `stop-system` to stop the components in the correct order.

To stop the `httpkit-server` component the function under :stop from
above is used:

       (defn stop [stop-httpkit]
         (stop-httpkit))

       (defn httpkit-server []
         {:binds [:httpkit :server]
          :start [start [:ring :handler] {:port 8080}]
          :stop stop})

Httpkit's `run-server` function returns a no-arg function per default
to shutdown the httpkit server (here: `(stop-httpkit)`). If the value
of :stop in the component declaration is a function, then it will be
invoked with the value under the path (see `:binds`) of the component
(here `[:httpkit :server]`). It would also be possible to define the
`:stop` section like this:

       (defn httpkit-server []
         {:binds [:httpkit :server]
          :start [start [:ring :handler] {:port 8080}]
          :stop [stop [:httpkit :server]]})

Which is the same syntax that is used for the `:start` section. This
allows the stop function to use further elements from the system map
to appropriately stop the component.

As you already noticed, we have hard coded the options for httpkit
(here ``{:port 8080}`), which is for sure not desirable. Like in the
case of the system map we will also put the complete configuration
into a single map. The component get this map to populate the
component declaration with the configuration parameters. The
configuration map for our example looks like this:

       (def config
         {:httpkit {:opts {:port 8080}}})

We change the `httpkit-server` component like this:

       (defn httpkit-server [config]
         {:binds [:httpkit :server]
          :start [start [:ring :handler] (-> config :httpkit :opts)]
          :stop stop})

After also adding one argument to the `hello-handler` component:

       (defn hello-handler [config]
         {:binds [:ring :handler]
          :start [hello-handler-fn]})

We can use the `config-components` helper function to start the system:

       (def system
         (start-system
           (config-components
            config
            [httpkit-server
             hello-handler])))

Stopping the system still looks the same:

       (stop-system system)


## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
