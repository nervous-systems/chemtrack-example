(ns chemtrack.frontend
  (:require [reagent.core :as reagent]
            [cljs.core.async :as async :refer [<! >!]]
            [chord.client :as chord]
            [reagent-forms.core :as reagent-forms]
            [chemtrack.frontend.render :as render]
            [chemtrack.frontend.util :as util])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(def recent-container
  (let [key-fn (juxt :timestamp :city :elements)]
    (sorted-set-by #(compare (key-fn %1) (key-fn %2)))))

(defn bind-form [config]
  (let [sighting (reagent/atom {})]
    [reagent-forms/bind-fields
     (render/form sighting config)
     sighting]))

(defn ws-loop! [recent sightings-out &
               [{:keys [max-items] :or {max-items 10}}]]
  (go
    (let [{sightings-in :ws-channel}
          (<! (chord/ws-ch
               (util/relative-ws-url "sightings")
               {:write-ch sightings-out}))]
      (loop []
        (when-let [{sighting :message} (<! sightings-in)]
          (swap! recent util/conj+evict sighting max-items)
          (recur))))))

(defn mount-root []
  (let [sightings-out (async/chan)
        recent        (reagent/atom recent-container)]
    (ws-loop! recent sightings-out)
    (reagent/render
     [render/app
      bind-form
      {:sightings-out sightings-out
       :elements {:ag "Aluminum"
                  :ba "Barium"
                  :th "Thorium"
                  :si "Silicon Carbide"
                  :sr "Strontium"}
       :recent recent}]
     (.getElementById js/document "app"))))

(mount-root)
