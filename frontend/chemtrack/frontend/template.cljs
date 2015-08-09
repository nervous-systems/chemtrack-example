(ns chemtrack.frontend.template
  (:require [clojure.walk :as walk]))

(def substitute walk/prewalk-replace)

(def form-element
  [:li.list-group-item.periodic {:key :chem/value} :chem/name])

(def sighting-element
  [:div.col-xs-1.periodic {:key :chem/value} :chem/name])

(def form
  [:div.modal.fade.form-modal {:tabIndex -1 :role "dialog"}
   [:div.modal-dialog {:role "document"}
    [:div.modal-content
     [:div.modal-header [:h2.modal-title "Submit Sighting"]]
     [:div.modal-body
      [:div.form-group
       [:div.input-group
        [:span.input-group-addon "City"]
        [:input.form-control
         {:field :text :id :city :key "form-control"}]]]

      [:div.form-group
       [:div.input-group
        [:label.input-group-addon "Severity"]
        [:input.form-control
         {:field :range :min 1 :max 10 :id :severity}]]]

      [:label.control-label "Composition"]
      [:ul.list-group {:field :multi-select :id :elements}
       :chem/elements]]

     [:div.modal-footer
      [:button.btn.btn-default
       {:type "button" :data-dismiss "modal"}
       "Close"]
      [:button.btn.btn-primary
       {:type "button"
        :on-click :chem/handler
        :data-dismiss "modal"}
       "Tell The World"]]]]])

(def sighting
  [:div.list-group-item.sighting-item {:key [:chem/timestamp :chem/city]}
   [:div.row.sighting-row
    [:div.col-md-4 [:h2 :chem/city]]
    :chem/elements
    [:div.pull-right :chem.timestamp/formatted]]])

(def app
  [:div
   [:div.jumbotron
    [:h1 "Recent Chemtrail Sightings"]
    [:div
     [:button.btn.btn-default.btn-lg
      {:type "button"
       :data-toggle "modal"
       :data-target ".form-modal"}
      "Submit Sighting"]]]
   [:div.container-fluid
    [:div#list.list-group.sightings
     :chem/sightings]
    :chem/form]])
