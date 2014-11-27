(ns battle-console.game
  (:require [battle-console.state :as state]
            [ajax.core :refer [GET POST]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [secretary.core :as secretary :refer-macros [defroute]]))

(defn- game-loaded
  "After game was loaded"
  [data]
  (state/set-state :loading-game nil)
  (state/set-state :game-data data))

(defn- error-loading
  "Error loading game"
  []
  (state/set-state :loading-game "Error!"))

(defn handler
  "Processes one game"
  [params]
  (state/set-page :game)
  (let [token (state/get-state :token)
        url (str "http://api.orionsbelt.eu/game/" (params :id) "?token=" token)]
    (state/set-state :loading-game (str "Loading game " (params :id) "..."))
    (GET url {:handler game-loaded
              :error-handler error-loading})))

(defn- render-game
  "Renders the index page"
  [app owner]
  (om/component
    (if (= :game (state/current-page))
      (if-let [msg (state/get-state :loading-game)]
        (dom/div nil msg)
        (dom/div nil "GAME HERE"))
      (dom/div #js {:className "hide"}))))

(defn- register-renderer
  "Register an Om renderer for this page"
  []
  (om/root
    render-game
    (state/current)
    {:target (. js/document (getElementById "game"))}))

(register-renderer)
