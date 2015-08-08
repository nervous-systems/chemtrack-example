(defproject io.nervous/chemtrack "0.1.0-SNAPSHOT"
  :description "Clojurescript/Node/Reagent/Lambda Example Application"
  :url "https://github.com/nervous-systems/chemtrack-example"
  :license {:name "Unlicense" :url "http://unlicense.org/UNLICENSE"}

  :dependencies [[org.clojure/clojure       "1.7.0"]
                 [org.clojure/core.async    "0.1.346.0-17112a-alpha"]
                 [org.clojure/clojurescript "0.0-3308"]
                 [cljsjs/react  "0.13.3-1"]
                 [reagent       "0.5.0"]
                 [reagent-forms "0.5.4"]
                 [io.nervous/fink-nottle "1.0.0-SNAPSHOT"]
                 [io.nervous/cljs-lambda "0.1.1"]
                 [io.nervous/cljs-nodejs-externs "0.2.0-SNAPSHOT"]
                 [jarohen/chord "0.6.0"]]
  :exclusions [[org.clojure/clojure]]

  :npm {:dependencies [[source-map-support "0.2.8"]
                       [express "4.13.1"]
                       [express-ws "0.2.6"]]}
  :resource-paths ["resources"]

  :clean-targets ^{:protect false}
  ["resources/public/js/out"
   "resources/public/js/chemtrack.js"
   "target"]

  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-npm "0.6.0"]
            [lein-figwheel "0.3.7"]
            [io.nervous/lein-cljs-lambda "0.2.1"]]

  :figwheel {:open-file-command "emacsclient"}

  :cljs-lambda
  {:cljs-build-id "lambda"
   :defaults {:role "arn:aws:iam::510355070671:role/permissive-lambda"}
   :aws-profile "nervous.io"
   :functions
   [{:name    "topic-to-queue"
     :invoke  chemtrack.lambda/topic-to-queue
     :timeout 20}]}

  :cljsbuild {:builds
              [{:id "backend"
                :source-paths ["backend"]
                :compiler
                {:output-to "target/backend/chemtrack.js"
                 :output-dir "target/backend"
                 :optimizations :none
                 :main "chemtrack.core"
                 :target :nodejs}}
               {:id "frontend"
                :source-paths ["frontend"]
                :figwheel true
                :compiler
                {:asset-path "js/out"
                 :output-to "resources/public/js/chemtrack.js"
                 :output-dir "resources/public/js/out"
                 :main "chemtrack.core"
                 :source-map true
                 :optimizations :none}}
               {:id "lambda"
                :source-paths ["lambda"]
                :compiler
                {:output-to "target/lambda/chemtrack.js"
                 :output-dir "target/lambda"
                 :optimizations :none
                 :source-map true
                 :target :nodejs}}]}

  :profiles {:dev
             {:repl-options
              {:nrepl-middleware
               [cemerick.piggieback/wrap-cljs-repl]}
              :dependencies
              [[com.cemerick/piggieback "0.2.1"]
               [org.clojure/tools.nrepl "0.2.10"]]}})
