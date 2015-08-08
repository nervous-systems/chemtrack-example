(ns chemtrack.util
  (:require [cljs.core.async :as async :refer [<!]]
            [eulalie.instance-data :as instance-data]
            [eulalie.lambda.util :as lambda]
            [cljs.reader]
            [clojure.string :as str])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn sighting-in [msg]
  (-> msg
      cljs.reader/read-string
      (assoc :timestamp (.getTime (js/Date.)))))

(def sighting-out pr-str)

(defn queue-name! [port]
  (go
    (let [{:keys [instance-id region]}
          (<! (instance-data/instance-identity!
               :document {:parse-json true}))]
      (str/join "_" [region instance-id port]))))

(defn topic-to-queue! [{:keys [topic-name creds] :as config}]
  (go
    (let [queue-name (<! (queue-name! config))]
      (-> (lambda/request!
           creds :topic-to-queue
           {:topic-name topic-name
            :queue-name queue-name})
          <!
          second))))

(defn channel-websocket! [ws to-client from-client]
  (.on ws "message" (fn [m _]
                      (async/put! from-client m)))
  (.on ws "close"   (fn []
                      (async/close! from-client)
                      (async/close! to-client)))
  (go
    (loop []
      (when-let [value (<! to-client)]
        (.send ws value)
        (recur))))
  [from-client to-client])

(defn conj+evict [q item limit]
  (-> q
      (cond-> (= limit (count q)) pop)
      (conj item)))
