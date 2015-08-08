(ns chemtrack.frontend.template
  (:require [clojure.walk :as walk]))

(def substitute walk/prewalk-replace)

(def form-element
  [:li.list-group-item.periodic {:key :chem/value} :chem/name])

(def sighting-element
  [:div.col-xs-1.periodic {:key :chem/value} :chem/name])

(def form
  [:div
   [:div.form-group
    [:div.input-group
     [:span.input-group-addon "City"]
     [:input.form-control {:field :text :id :city :key "form-control"}]]]

   [:div.form-group
    [:div.input-group
     [:label.input-group-addon "Severity"]
     [:input.form-control {:field :range :min 1 :max 10 :id :severity}]]]

   [:label.control-label "Elemental Composition"]
   [:ul.list-group {:field :multi-select :id :elements}
    :chem/elements]

   [:div.form-group
    [:button.btn.btn-default.pull-right
     {:on-click :chem/handler}
     "Tell the World!"]]])

(def sighting
  [:div.list-group-item.sighting-item {:key [:chem/timestamp :chem/city]}
   [:div.row
    [:div.col-md-4 [:h4 {:style {:margin "0px"}} :chem/city]]
    :chem/elements
    [:div.col-xs-3.pull-right :chem.timestamp/formatted]]])

(def app
  [:div
   [:div.row
    [:div.text-right.col-md-12
     [:h1 "Recent Chemtrail Sightings"]]]
   [:div.row
    [:div.col-md-4 :chem/form]
    [:div#list.list-group.col-md-8
     :chem/sightings]]])
