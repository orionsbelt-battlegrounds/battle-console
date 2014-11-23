(ns battle-console.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [secretary.core :as secretary :refer-macros [defroute]])
  (:import goog.History
             goog.history.EventType)
  )

(defonce app-state (atom {:text "Hello Chestnut!"}))

(secretary/set-config! :prefix "#")

(defroute "/" {:as params}
  (js/console.log "GET /")
  )

(defroute "/help" {:as params}
  (js/console.log "GET /help")
  )

;; Catch all
(defroute "*" []
  (js/console.log "ALL"))


;; Quick and dirty history configuration.
(let [h (History.)]
    (goog.events/listen h EventType.NAVIGATE #(secretary/dispatch! (.-token %)))
      (doto h
            (.setEnabled true)))

(defn h1 [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/h1 nil (:text app)))))

(defn h2 [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/h2 nil (:text app)))))

(defn main
  []
  (secretary/dispatch! "/")
  (om/root
    h1
    app-state
    {:target (. js/document (getElementById "app"))}))
