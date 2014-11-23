(ns battle-console.index
  (:require [battle-console.state :as state]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn handler
  "Handles the root page and asks for the token"
  []
  (state/set-page :home))

(defn by-id [id]
  (.getElementById js/document (name id)))

(defn- check-token
  "Processes the inserted token"
  []
  (js/alert (-> (by-id "token") (.-value))))

(defn- render-index
  "Renders the index page"
  [app owner]
    (om/component (dom/div nil
                    (dom/h1 nil "Enter your API token")
                    (dom/input #js {:type "text" :id "token"})
                    (dom/button #js {:onClick check-token :className "btn btn-default"} "Next"))))

(defn- register-renderer
  "Register an Om renderer for this page"
  []
  (om/root
    render-index
    (state/current)
    {:target (. js/document (getElementById "app"))}))

(register-renderer)
