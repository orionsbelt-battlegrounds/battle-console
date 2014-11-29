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

(defn game-header
  "Shows the game header"
  [game]
  (dom/div #js {:className "row"}
    (dom/div #js {:className "col-lg-12"}
      (dom/div #js {:className "bs-component"}
        (dom/table #js {:className "table table-striped table-hover"}
          (dom/tr nil
            (dom/th nil "Turn")
            (dom/th nil "State")
            (dom/th nil "Match")
            (dom/th nil "Id"))
          (dom/tr nil
            (dom/td nil "1")
            (dom/td nil (get-in game ["board" "state"]))
            (dom/td nil (str (get-in game ["p1" "name"]) " vs " (get-in game ["p2" "name"])))
            (dom/td nil (get game "_id"))))))))

(defn- get-stash
  "Gets the current stash"
  [game]
  (get-in game ["battle" "stash" "p1"]))

(defn- game-stash
  "Shows the current stash if available"
  [game]
  (dom/div #js {:className "row"}
    (dom/div #js {:className "col-lg-4"}
      (dom/div #js {:className "bs-component"}
        (apply dom/table #js {:className "table table-striped table-hover"}
          (dom/caption nil "Stash")
          (dom/tr nil
            (dom/th nil "Unit")
            (dom/th nil "Quantity"))
          (for [[unit quantity] (get-stash game)]
            (dom/tr nil
              (dom/td nil unit)
              (dom/td nil quantity))))))))

(defn- render-game
  "Renders the index page"
  [state owner]
  (om/component
    (if (= :game (state/current-page))
      (if-let [msg (state/get-state :loading-game)]
        (dom/div nil msg)
        (dom/div nil
                 (game-header (state :game-data))
                 (game-stash (state :game-data))))
      (dom/div #js {:className "hide"}))))

(defn- register-renderer
  "Register an Om renderer for this page"
  []
  (om/root
    render-game
    (state/current)
    {:target (. js/document (getElementById "game"))}))

(register-renderer)
