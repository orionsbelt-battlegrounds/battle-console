(ns battle-console.index
  (:require [battle-console.state :as state]
            [ajax.core :refer [GET POST]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [secretary.core :as secretary :refer-macros [defroute]]))

(enable-console-print!)

(defn handler
  "Handles the root page and asks for the token"
  []
  (println "GET /login")
  (state/set-page :home))

(defn by-id [id]
  (.getElementById js/document (name id)))

(defn- get-token
  "Gets the current input token"
  []
  (-> (by-id "token") (.-value)))

(defn- after-load-games
  "After the load games call"
  [data]
  (state/set-state :loading false)
  (state/set-state :token (get-token))
  (secretary/dispatch! "/games"))

(defn- error-loading-games
  "When getting games fails"
  [data]
  (state/set-state :loading false)
  (state/set-error :auth-fail (str (data :status) "  " (data :message))))

(defn- check-token
  "Processes the inserted token"
  []
  (let [token (get-token)
        url (str "http://api.orionsbelt.eu/auth/enforce?token=" token)]
    (state/set-state :loading true)
    (state/clear-error :auth-fail)
    (GET url {:handler after-load-games
              :error-handler error-loading-games})))

(defn css-result-class
  "Gets the result based on css class"
  [error]
  (cond
    (not= :home (state/current-page)) "hide"
    (state/get-state :token) "has-success"
    error "has-error"
    :else ""))

(defn- button-caption
  "The next button's caption"
  []
  (if (state/get-state :loading)
    "Loading..."
    "Next"))

(defn- render-index
  "Renders the index page"
  [app owner]
  (let [error (state/get-error :auth-fail)]
    (om/component (dom/div #js {:className (str "form-group " (css-result-class error))}
                    (dom/h1 nil "Enter your API token")
                    (dom/label #js {:for "token" :className "control-label"} "Token:")
                    (dom/input #js {:type "text" :id "token" :className "form-control"})
                    (dom/button #js {:onClick check-token :className "btn btn-default"} (button-caption))))))

(defn- register-renderer
  "Register an Om renderer for this page"
  []
  (om/root
    render-index
    (state/current)
    {:target (. js/document (getElementById "login"))}))

(register-renderer)
