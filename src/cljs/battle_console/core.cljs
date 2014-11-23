(ns battle-console.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [battle-console.index :as index]
            [secretary.core :as secretary :refer-macros [defroute]])
  (:import goog.History
           goog.history.EventType))

(secretary/set-config! :prefix "#")

(defroute "/" {:as params} (index/handler))

(defroute "/help" {:as params}
  (js/console.log "GET /help"))

;; Quick and dirty history configuration.
(let [h (History.)]
  (goog.events/listen h EventType.NAVIGATE #(secretary/dispatch! (.-token %)))
  (doto h
    (.setEnabled true)))

(defn main
  []
  (secretary/dispatch! "/"))
