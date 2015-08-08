(ns chemtrack.lambda
  (:require [cljs-lambda.util :refer [async-lambda-fn]]
            [cljs.core.async :as async :refer [<!]]
            [fink-nottle.sns :as sns]
            [fink-nottle.sqs :as sqs]
            [glossop.util]
            [eulalie.creds])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(set! *main-cli-fn* identity)
(enable-console-print!)

(defn sns-bridge-policy [topic-arn queue-arn]
  {:statement
   [{:sid (str topic-arn "->" queue-arn)
     :effect :allow
     :principal {:AWS "*"}
     :resource queue-arn
     :action [:sqs/send-message]
     :condition {:arn-equals {:aws/source-arn topic-arn}}}]})

(defn create-queue! [creds queue-name]
  (go
    (let [queue-url (<! (sqs/create-queue! creds queue-name))
          queue-arn (<! (sqs/queue-arn! creds queue-url))]
      (<! (sqs/purge-queue! creds queue-url))
      {:queue-url queue-url
       :queue-arn queue-arn})))

(defn subscribe-queue! [creds {:keys [queue-url queue-arn]} topic-arn]
  (go
    (<! (sqs/set-queue-attribute!
         creds queue-url :policy
         (sns-bridge-policy topic-arn queue-arn)))
    (let [subs-arn
          (<! (sns/subscribe! creds topic-arn :sqs queue-arn))]
      (<! (sns/set-subscription-attribute!
           creds subs-arn :raw-message-delivery true)))))

(def ^:export topic-to-queue
  (async-lambda-fn
   (fn [{:keys [topic-name queue-name]} context]
     (go
       (let [creds (eulalie.creds/env)
             topic-arn (<! (sns/create-topic! creds topic-name))
             {:keys [queue-url queue-arn] :as queue}
             (<! (create-queue! creds queue-name))]
         (<! (subscribe-queue! creds queue topic-arn))
         {:topic-id topic-arn :queue-id queue-url})))))
