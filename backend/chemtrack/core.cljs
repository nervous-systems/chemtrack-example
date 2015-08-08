(ns chemtrack.core
  (:require [cljs.core.async :as async :refer [<! >! close!]]
            [cljs.nodejs :as nodejs]
            [cljs.reader :refer [read-string]]
            [eulalie.creds]
            [fink-nottle.sns :as sns]
            [fink-nottle.sqs.channeled :as sqs]
            [chemtrack.util :as util])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(nodejs/enable-util-print!)

(defonce http    (nodejs/require "http"))
(defonce express (nodejs/require "express"))
(defonce express-ws (nodejs/require "express-ws"))

(defn sns-push-loop! [creds topic-id sightings-in]
  (go
    (loop []
      (let [item (<! sightings-in)]
        (sns/publish-topic! creds topic-id {:default (pr-str item)})
        (recur)))))

(defn sqs-incoming!
  [{:keys [max-recent recent deletes]}
   {:keys [body] :as message}
   results]
  (let [body (read-string body)]
    (swap! recent util/conj+evict body max-recent)
    (go
      (>! results body)
      (>! deletes message)
      (close! results))))

(defn connect-channels!
  [{:keys [port topic-name creds max-recent] :as config}
   {:keys [sightings-out sightings-in recent] :as channels}]
  (go
    (let [{:keys [queue-id topic-id]} (<! (util/topic-to-queue! config))]
      (sns-push-loop! creds topic-id sightings-in)
      (let [{deletes :in-chan} (sqs/batching-deletes creds queue-id)]
        (async/pipeline-async
         1
         sightings-out
         (partial sqs-incoming! {:deletes deletes
                                 :max-recent recent
                                 :recent recent})
         (sqs/receive! creds queue-id))))))

(defn make-sightings-handler [{:keys [sightings-out sightings-in recent]}]
  (let [sightings-out* (async/mult sightings-out)]
    (fn [websocket _]
      (let [from-client (async/chan 1 (map util/sighting-in))
            to-client   (async/chan 1 (map util/sighting-out))]
        (util/channel-websocket!
         websocket to-client from-client)
        (async/pipe from-client sightings-in false)
        (go
          (<! (async/onto-chan to-client @recent false))
          (async/tap sightings-out* to-client))))))

(defn register-routes [app channels]
  (doto app
    (.use (.static express "resources/public"))
    (.ws  "/sightings" (make-sightings-handler channels))))

(defn make-server [app]
  (let [server (.createServer http app)]
    (express-ws app server)
    server))

(defn -main [& [{:keys [port]
                 :or {port (or (aget js/process "env" "PORT") 8080)}}]]
  (let [channels {:sightings-out (async/chan)
                  :sightings-in  (async/chan)
                  :recent        (atom #queue [])}
        app      (express)
        server   (make-server app)]

    (register-routes app channels)
    (connect-channels!
     {:port port
      :topic-name "chemtrail-sightings"
      :creds (eulalie.creds/env)
      :max-recent 10}
     channels)
    (.listen server port)))

(set! *main-cli-fn* -main)
