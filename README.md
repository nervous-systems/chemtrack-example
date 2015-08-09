# chemtrack-example
Clojurescript/Node/Reagent/Lambda Example Application

Example code for [Chasing Chemtrails with Clojurescript](https://nervous.io/clojure/clojurescript/node/aws/2015/08/09/chemtrails/)

## Dependencies
  - NPM
  - [AWS CLI](https://aws.amazon.com/cli/)

Steps:
 - `lein deps`
 - [Insert valid IAM role name in `project.clj`](https://github.com/nervous-systems/cljs-lambda) (`lein cljs-lambda default-iam-role` will allow deployment, but SNS & SQS permissions must be added for execution)
 - `lein cljs-lambda deploy`
 - `lein cljsbuild once`
 - `node target/backend/chemtrack.js`
