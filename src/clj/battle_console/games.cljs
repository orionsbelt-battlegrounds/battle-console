(ns battle-console.games
  (:require [battle-console.state :as state]
            [ajax.core :refer [GET POST]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [secretary.core :as secretary :refer-macros [defroute]]))

(defn handler
  "Handles the root page and asks for the token"
  []
  (state/set-page :games))

(defn- render-games
  "Renders the index page"
  [app owner]
  (om/component (dom/div nil
                  (dom/h1 nil "Games"))))

(defn- register-renderer
  "Register an Om renderer for this page"
  []
  (om/root
    render-games
    (state/current)
    {:target (. js/document (getElementById "app"))}))

(register-renderer)
