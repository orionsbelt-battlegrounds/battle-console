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
  (state/set-state :player-code (get-in data ["viewed-by" "player-code"]))
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
  (or
    (get-in game ["battle" "stash" "p1"])
    (get-in game ["battle" "stash" "p2"])))

(defn- game-stash
  "Shows the current stash if available"
  [game]
  (let [stash (get-stash game)]
    (dom/div #js {:className "row"}
      (dom/div #js {:className "col-lg-3"}
        (dom/div #js {:className "bs-component"}
          (apply dom/table #js {:className "table table-striped table-hover"}
            (dom/caption nil "Stash")
            (dom/tr nil
              (dom/th nil "Unit")
              (dom/th nil "Quantity"))
            (for [[unit quantity] stash]
              (dom/tr nil
                (dom/td nil unit)
                (dom/td nil quantity)))))))))

(defn- render-board-cell
  "Renders a board's cell"
  [game x y]
  (dom/p #js {:className "boardCoords"} (str "[" x " " y "]")))

(defn- render-board
  "Renders the board"
  [game]
  (dom/div #js {:className "col-lg-8"}
    (dom/div #js {:className "bs-component"}
      (apply dom/table #js {:className "table table-striped table-hover"}
        (for [y (range 1 9)]
          (apply dom/tr nil
            (for [x (range 1 9)]
              (dom/td nil (render-board-cell game x y)))))))))

(defn- render-player-roaster
  "Renders the player's roaster"
  [game]
  "")

(defn- render-game
  "Renders the index page"
  [state owner]
  (om/component
    (if (= :game (state/current-page))
      (if-let [msg (state/get-state :loading-game)]
        (dom/div nil msg)
        (dom/div nil
                 (game-header (state :game-data))
                 (dom/div #js {:className "row"}
                   (dom/div #js {:className "col-lg-2"} (render-player-roaster state))
                   (render-board (state :game-data))
                   (dom/div #js {:className "col-lg-2"} ""))
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
