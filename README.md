# chemtrack-example
Clojurescript/Node/Reagent/Lambda Example Application

Steps:
 - Install NPM
 - `lein deps`
 - Configure [AWS CLI](https://aws.amazon.com/cli/)
 - [Insert valid IAM role name in `project.clj`](https://github.com/nervous-systems/cljs-lambda) (`lein cljs-lambda default-iam-role` will allow deployment, but SNS & SQS permissions are must be added for execution)
 - `lein cljs-lambda deploy`
 - `lein cljsbuild once`
 - `node target/backend/chemtrack.js`
