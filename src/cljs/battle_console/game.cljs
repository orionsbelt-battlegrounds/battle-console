(ns battle-console.game
  (:require [battle-console.state :as state]
            [ajax.core :refer [GET POST, PUT]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.reader :as reader]
            [secretary.core :as secretary :refer-macros [defroute]]))

(defn- normalize-board
  "Uses p2-focused-board if present"
  [data]
  (if (data :p2-focused-board)
    (-> data
        (assoc :board (data :p2-focused-board))
        (dissoc :p2-focused-board))
    data))

(defn- game-loaded
  "After game was loaded"
  [data]
  (state/set-state :loading-game nil)
  (state/set-state :player-code (get-in data ["viewed-by" "player-code"]))
  (let [final-data (normalize-board data)]
    (state/set-state :raw-game-data data)
    (state/set-state :original-game-data final-data)
    (state/set-state :game-data final-data)))

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
    (state/set-state :game-id (params :id))
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
  (get-in game ["board" "stash" (get-in game ["viewed-by" "player-code"])]))

(defn- game-stash
  "Shows the current stash if available"
  [game]
  (let [stash (get-stash (state/get-state :game-data))]
    (dom/div #js {:className "row"}
      (dom/div #js {:className "col-lg-3"}
        (dom/div #js {:className "bs-component"}
          (dom/table #js {:className "table table-striped table-hover"}
            (dom/caption nil "Stash")
            (apply dom/tbody nil
              (dom/tr nil
                (dom/th nil "Unit")
                (dom/th nil "Quantity"))
              (for [[unit quantity] stash]
                (dom/tr nil
                  (dom/td nil unit)
                  (dom/td nil quantity))))))))))

(defn- get-player-position
  "Gets the position of a player for a given game"
  [game player]
  (if (empty? player)
    nil
    (let [player-code (get-in game ["viewed-by" "player-code"])]
      (cond
        (= "p1" player-code player) "p1"
        (= "p2" player-code player) "p1"
        :else "p2"))))

(defn- get-element-css
  "Gets a CSS class for the given element"
  [game element]
  (condp = (get-player-position game (get element "player"))
    "p1" "success"
    "p2" "warning"
    nil ""))

(defn- direction-to-arrow
  "Converts a direction to an arrow"
  [element]
  (condp = (get element "direction")
    "south" "↓"
    "north" "↑"
    "east" "→"
    "west" "←"
    nil ""))

(defn- display-unit
  "Displays the unit of the given element"
  [element]
  (when element)
    (str (get element "unit") " " (direction-to-arrow element)))

(defn- render-board-cell
  "Renders a board's cell"
  [game x y]
  (let [coordCode (str "[" x " " y "]")
        element (get-in game ["board" "elements" coordCode])
        css (get-element-css game element)]
    (dom/td #js {:className css}
      (dom/p #js {:className "boardCoords"} coordCode)
      (dom/p #js {:className "boardUnit"} (or (display-unit element) "-"))
      (dom/p #js {:className "boardQuantity"} (or (get element "quantity") 0)))))

(defn- render-board
  "Renders the board"
  [game]
  (dom/div #js {:className "col-lg-8"}
    (dom/div #js {:className "bs-component"}
      (apply dom/table #js {:className "table table-striped table-hover"}
        (for [y (range 1 9)]
          (apply dom/tr #js {:className "active"}
            (for [x (range 1 9)]
              (render-board-cell game x y))))))))

(defn- player-switch
  "Switches players"
  [player]
  (if (= player "p1")
    "p2"
    "p1"))

(defn- get-name-for
  "Gets the name to be on the given position"
  [game position]
  (let [player-code (get-in game [:game-data "viewed-by" "player-code"])
        pos (name position)]
    (cond
      (= "p1" pos player-code) (get-in game [:game-data "p1" "name"])
      (= ["p2" pos player-code] ["p2" "p1" "p2"]) (get-in game [:game-data "p2" "name"])
      (= ["p2" pos player-code] ["p2" "p2" "p1"]) (get-in game [:game-data "p2" "name"])
      :else (get-in game [:game-data (player-switch pos) "name"]))))

(defn- render-player-roaster
  "Renders the player's roaster"
  [game]
  (dom/div #js {:className "roaster h100"}
           (dom/div #js {:className "p2"}
             (dom/div #js {:className "p1 panel panel-warning"}
               (dom/div #js {:className "panel-heading"}
                (dom/h3 #js {:className "panel-title"} (get-name-for game :p2)))))
           (dom/div #js {:className "p1"} 
             (dom/div #js {:className "p1 panel panel-success"}
               (dom/div #js {:className "panel-heading"}
                (dom/h3 #js {:className "panel-title"} (get-name-for game :p1)))))))

(defn by-id [id]
  (.getElementById js/document (name id)))

(defn- get-action
  "Gets the current entered action"
  []
  (-> (by-id "newAction") (.-value)))

(defn- reset-actions
  "Reset the current actions being applied"
  []
  (state/set-state :game-data (state/get-state :original-game-data))
  (state/set-state :current-actions []))

(defn- action-added
  "Added action"
  [data]
  (let [actions (or (state/get-state :current-actions) [])
        current-action (state/get-state :processing-action)
        new-actions (conj actions current-action)
        current-game (state/get-state :game-data)
        updated-game (assoc current-game "board" (or (get data "p2-focused-board") (get data "board")))]
    (state/set-state :game-data updated-game)
    (state/set-state :current-actions new-actions)
    (state/set-state :processing-action nil)))

(defn- error-loading-action
  "Error loading action"
  []
  (state/set-state :processing-action nil))

(defn- game-played
  "After the game was deployed"
  [data]
  (game-loaded data)
  (state/set-state :current-actions nil)
  (state/set-state :processing-action nil))

(defn- game-deployed
  "After the game was deployed"
  [data]
  (game-played data))

(defn- deploy-game
  "Deploys a game"
  [ev]
  (let [actions (or (state/get-state :current-actions) [])
        game (state/get-state :original-game-data)
        player-code (get-in game ["viewed-by" "player-code"])
        token (state/get-state :token)
        game-id (state/get-state :game-id)
        url (str "http://api.orionsbelt.eu/game/" game-id "/deploy?token=" token)]
    (state/set-state :processing-action actions)
    (PUT url {:params {:actions actions}
              :format :json
              :handler game-deployed
              :error-handler error-loading-action})))

(defn- play-game
  "Plays a game"
  [ev]
  (let [actions (or (state/get-state :current-actions) [])
        game (state/get-state :raw-game-data)
        player-code (get-in game ["viewed-by" "player-code"])
        token (state/get-state :token)
        game-id (state/get-state :game-id)
        url (str "http://api.orionsbelt.eu/game/" game-id "/turn?token=" token)]
    (state/set-state :processing-action actions)
    (PUT url {:params {:actions actions}
              :format :json
              :handler game-played
              :error-handler error-loading-action})))

(defn- add-action
  "Processes a new action"
  [ev]
  (let [action (reader/read-string (get-action))
        current-actions (or (state/get-state :current-actions) [])
        new-actions (conj current-actions action)
        game (state/get-state :raw-game-data)
        player-code (get-in game ["viewed-by" "player-code"])
        game (-> game
                 (assoc :actions new-actions)
                 (assoc :p2-focused-board (= player-code "p2"))
                 (assoc :action-focus player-code))
        jsgame (js/encodeURIComponent (.stringify js/JSON (clj->js game)))
        url (str "http://rules.api.orionsbelt.eu/game/turn/" player-code "?context=" jsgame)]
    (println game)
    (state/set-state :processing-action action)
    (GET url {:handler action-added
              :error-handler error-loading-action})))

(defn- add-action-disabled
  "Checks if the button should be disabled"
  [state]
  (if (state :processing-action)
    "disabled"
    ""))

(defn- deploy-disabled
  "Checks if the deploy button should be disabled"
  [state]
  (let [player-code (get-in state [:game-data "viewed-by" "player-code"])]
    (cond
      (state :processing-action) "disabled"
      (not= "deploy" (get-in state [:original-game-data "board" "state"])) "disabled"
      (empty? (get-in state [:original-game-data "board" "stash" player-code])) "disabled"
      :else "")))

(defn- play-disabled
  "Checks if the play button should be disabled"
  [state]
  (let [player-code (get-in state [:game-data "viewed-by" "player-code"])]
    (cond
      (state :processing-action) "disabled"
      (not= player-code (get-in state [:original-game-data "board" "state"])) "disabled"
      :else "")))

(defn- render-action-console
  "Renders the action management console"
  [state]
  (dom/div #js {:className (str "form-group ")}
    (apply dom/div nil (map (fn [raw] (dom/div #js {:className "label label-info action-label"} (.stringify js/JSON (clj->js raw)))) (state :current-actions)))
    (dom/label #js {:for "newAction" :className "control-label"} "Action:")
    (dom/input #js {:type "text" :id "newAction" :className "form-control"})
    (dom/button #js {:id "resetActionButton" :onClick reset-actions :className "btn btn-default"} "Reset")
    (dom/button #js {:id "addActionButton" :onClick add-action :className "btn btn-info" :disabled (add-action-disabled state)} "Add")
    (dom/button #js {:id "deployButton" :onClick deploy-game :className "btn btn-info" :disabled (deploy-disabled state)} "Deploy")
    (dom/button #js {:id "playButton" :onClick play-game :className "btn btn-info" :disabled (play-disabled state)} "Play")))

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
                   (dom/div #js {:className "col-lg-2 h100"} (render-player-roaster state))
                   (render-board (state :game-data))
                   (dom/div #js {:className "col-lg-2"} (render-action-console state)))
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
