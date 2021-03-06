(ns battle-console.games
  (:require [battle-console.state :as state]
            [ajax.core :refer [GET POST]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [secretary.core :as secretary :refer-macros [defroute]]))

(defn- games-loaded
  "After games were loaded"
  [data]
  (state/set-state :loading-games nil)
  (state/set-state :games-data data))

(defn- error-loading
  "Error loading games"
  []
  (state/set-state :loading-games "Error!"))

(defn handler
  "Handles the root page and asks for the token"
  []
  (state/set-page :games)
  (state/set-state :loading-games "Loading games...")
  (let [token (state/get-state :token)
        url (str "http://api.orionsbelt.eu/player/latest-games?token=" token)]
    (GET url {:handler games-loaded
              :error-handler error-loading})))

(defn- get-master-css
  "Getst the master CSS"
  []
  (cond
    (not= :games (state/current-page)) "hide"
    :else ""))

(defn- game-row
  "Creates the games rows"
  [data]
  (dom/tr nil
          (dom/td nil
                  (dom/a #js {:href (str "#/game/" (data "_id"))} (data "_id")))
          (dom/td nil (data "state"))
          (dom/td nil (get-in data ["p1" "name"]))
          (dom/td nil (get-in data ["p2" "name"]))))

(defn- render-games
  "Renders the index page"
  [app owner]
  (let [data (state/get-state :games-data)]
    (om/component
      (if-let [msg (state/get-state :loading-games)]
        (dom/p nil msg)
        (dom/div #js {:className (get-master-css)}
                      (dom/h1 nil (str (count (state/get-state :games-data)) " games available"))
                      (apply dom/table #js {:className "table table-striped table-hover "}
                             (dom/tr nil
                                     (dom/th nil "Game ID")
                                     (dom/th nil "State")
                                     (dom/th nil "P1")
                                     (dom/th nil "P2"))
                            (map game-row data)))))))

(defn- register-renderer
  "Register an Om renderer for this page"
  []
  (om/root
    render-games
    (state/current)
    {:target (. js/document (getElementById "games"))}))

(register-renderer)
