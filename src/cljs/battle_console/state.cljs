(ns battle-console.state
  "State utilities")

(defonce app-state (atom {:page :home}))

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
