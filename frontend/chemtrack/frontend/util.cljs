(ns chemtrack.frontend.util)

(defn relative-ws-url [& [segment]]
  (let [loc (comp not-empty
                  (partial aget (aget js/window "location"))
                  name)
        scheme (str "ws" (when (-> :protocol loc (= "https:")) "s"))
        port   (some->> :port loc (str ":"))]
    (str scheme "://" (loc :hostname) port (loc :pathname) segment)))

(defn conj+evict [coll item limit]
  (-> coll
      (cond-> (= limit (count coll)) (disj (first coll)))
      (conj item)))
