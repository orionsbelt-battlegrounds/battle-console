(ns battle-console.state
  "State utilities")

(defonce app-state (atom {:page :home}))

(defn clear-error
  "Clears the current error"
  [error-type]
  (swap! app-state dissoc error-type))

(defn set-error
  "Sets an app error"
  [error-type error-msg]
  (swap! app-state assoc error-type error-msg))

(defn get-error
  "Gets an app error"
  [error-type]
  (get @app-state error-type))

(defn set-state
  "Sets custom state"
  [k v]
  (swap! app-state assoc k v))

(defn get-state
  "Gets custom state"
  [k]
  (get @app-state k))

(defn set-page
  "Sets the current page to be displayed"
  [page-name]
  (swap! app-state assoc :page page-name))

(defn current-page
  "Gets the current page"
  []
  (get @app-state :page))

(defn current
  "The current state bag"
  []
  app-state)
