(ns chemtrack.frontend
  (:require [reagent.core :as reagent]
            [cljs.core.async :as async :refer [<! >!]]
            [chord.client :as chord]
            [reagent-forms.core :as reagent-forms]
            [chemtrack.frontend.render :as render]
            [chemtrack.frontend.util :as util])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(defonce items
  (let [key-fn (juxt :timestamp :city)]
    (reagent/atom
     (sorted-set-by #(compare (key-fn %1) (key-fn %2))))))

(defn form [config]
  (let [sighting (reagent/atom {})]
    [reagent-forms/bind-fields
     (render/form sighting config)
     sighting]))

(defn ws-loop! [sightings-out &
               [{:keys [max-items] :or {max-items 10}}]]
  (go
    (let [{sightings-in :ws-channel}
          (<! (chord/ws-ch
               (util/relative-ws-url "sightings")
               {:write-ch sightings-out}))]
      (loop []
        (when-let [{item :message} (<! sightings-in)]
          (swap! items util/conj+evict item max-items)
          (recur))))))

(defn mount-root []
  (let [sightings-out (async/chan)]
    (ws-loop! sightings-out)
    (reagent/render
     [render/app
      form
      {:sightings-out sightings-out
       :elements {:ag "Aluminum"
                  :ba "Barium"
                  :th "Thorium"
                  :si "Silicon Carbide"
                  :sr "Strontium"}
       :items items}]
     (.getElementById js/document "app"))))

(mount-root)
