# chemtrack-example
Clojurescript/Node/Reagent/Lambda Example Application

Example code for [Chasing Chemtrails with Clojurescript](https://nervous.io/clojure/clojurescript/node/aws/2015/08/09/chemtrails/).  [Demo instance here](http://chemtrack.nervous.io).

## Dependencies
  - NPM
  - [AWS CLI](https://aws.amazon.com/cli/)
  - EC2 (The queue naming code retrieves instance metadata.  [Easy enough to change](https://github.com/nervous-systems/chemtrack-example/blob/master/backend/chemtrack/backend/util.cljs#L23))

Steps:
 - `lein deps`
 - [Insert valid IAM role name in `project.clj`](https://github.com/nervous-systems/cljs-lambda) (`lein cljs-lambda default-iam-role` will allow deployment, but SNS & SQS permissions must be added for execution)
 - `lein cljs-lambda deploy`
 - `lein cljsbuild once`
 - `node target/backend/chemtrack.js`