(ns chemtrack.frontend.render
  (:require [chemtrack.frontend.template :as t]
            [cljs.core.async :as async]
            [clojure.string :as str]
            [cljsjs.moment]))

(defn elt [template [sym readable]]
  (t/substitute
   {:chem/value sym :chem/name readable}
   template))

(defn form [sighting {:keys [elements sightings-out]}]
  (t/substitute
   {:chem/elements (map (partial elt t/form-element) elements)
    :chem/handler  (fn [& _]
                     (async/put! sightings-out @sighting))}
   t/form))

(defn sighting [{:keys [timestamp city elements]}]
  (t/substitute
   {:chem/city city
    :chem/timestamp timestamp
    :chem.timestamp/formatted
    (-> timestamp
        js/moment
        (.format "YYYY-MM-DD HH:mm"))
    :chem/elements (map (fn [sym]
                          (elt t/sighting-element
                               [sym (-> sym name str/capitalize)]))
                        elements)}
   t/sighting))

(defn app [form-renderer {:keys [recent] :as deps}]
  (t/substitute
   {:chem/sightings (map sighting (reverse @recent))
    :chem/form      (form-renderer deps)}
   t/app))
