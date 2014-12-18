(defproject org.clojars.haussman/om-react-pixi "0.1.0-SNAPSHOT"
  :description "ClojureScript definitions for using om with react-pixi"
  :url "https://github.com/Izzimach/om-react-pixi"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2277"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [om "0.7.1"]
		             [org.clojars.haussman/react-pixi "0.1.0-SNAPSHOT"]
                 [prismatic/schema "0.2.4"]
                 [prismatic/om-tools "0.3.6"]]

  :profiles {:dev {:dependencies [[figwheel "0.1.5-SNAPSHOT"]
                                  [ring/ring-core "1.2.2"]
                                  [ring/ring-jetty-adapter "1.2.2"]
                                  [compojure "1.3.1"]]}}

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-figwheel "0.1.5-SNAPSHOT"]
            [lein-ring "0.8.10"]]

  :ring {:handler webserver.servefromjar/reactpixifromjar :port 8081 }

  :figwheel {
             :http-server-root "public"
             :server-port 8081
             :ring-handler webserver.servefromjar/reactpixifromjar
             }

  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src/omreactpixi"]
                        :compiler {
                                   :output-to "out/om-react-pixi.js"
                                   :output-dir "out"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "packed"
                        ;; vendor is added to the source paths so that
                        ;; :preamble can find react-pixi.js
                        :source-paths ["src/omreactpixi"]
                        :compiler {
                                   :output-to "out-min/om-react-pixi.min.js"
                                   :output-dir "out-min"
                                   :optimizations :advanced
                                   :preamble ["react_pixi/react-pixi.min.js"]
                                   :externs ["react_pixi/react-pixi.js"]
                                   :closure-warnings {:externs-validation :off
                                                      :non-standard-jsdoc :off}}}
                       {:id "hello"
                        :source-paths ["src/omreactpixi" "src/examples/hello"]
                        :compiler {
                                   :output-to "resources/public/examples/hello/out/hello.js"
                                   :output-dir "resources/public/examples/hello/out"
                                   :source-map true
                                   :optimizations :none}}
                       {:id "cupcake"
                        :source-paths ["src/omreactpixi" "src/examples/cupcake"]
                        :compiler {
                                   :output-to "resources/public/examples/cupcake/out/cupcake.js"
                                   :output-dir "resources/public/examples/cupcake/out"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "cupcake-minimized"
                        :source-paths ["src/omreactpixi" "src/examples/cupcake"]
                        :compiler {
                                   :output-to "resources/public/examples/cupcake/out-min/cupcake.js"
                                   :output-dir "resources/public/examples/cupcake/out-min"
                                   :externs ["react_pixi/react-pixi.min.js" "react_pixi/pixi.dev.js"]
                                   :optimizations :advanced
                                   :pretty-print false
                                   :closure-warnings {:externs-validation :off
                                                      :non-standard-jsdoc :off}}}
                       {:id "preloader"
                        :source-paths ["src/omreactpixi" "src/examples/preloader"]
                        :compiler {
                                   :output-to "resources/public/examples/preloader/out/preloader.js"
                                   :output-dir "resources/public/examples/preloader/out"
                                   :source-map true
                                   :optimizations :none}}
                       {:id "interactive"
                        :source-paths ["src/omreactpixi" "src/examples/interactive"]
                        :compiler {
                                   :output-to "resources/public/examples/interactive/out/interactive.js"
                                   :output-dir "resources/public/examples/interactive/out"
                                   :source-map true
                                   :optimizations :none}}]})

