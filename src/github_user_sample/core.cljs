(ns github-user-sample.core
  (:require [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]
            [github-user-sample.api :as api]
            [clojure.string :as string]
            [goog.i18n.DateTimeFormat :as dtf]))

(enable-console-print!)

(defonce app-state (atom {:search-text "" :user {}}))

;; Define ENTER key code
(def ENTER_KEY 13)

;; Keydown handler from input field
;; It looks if key pressed is ENTER, if yes then make API call to fetch user data
(defn handle-keydown [e data owner]
  (when (== (.-which e) ENTER_KEY)
    (let [search-text (.. e -target -value)]
      (when-not (string/blank? search-text)
          (om/update! data :search-text search-text)
          (api/make-remote-call
            (str "https://api.github.com/users/" search-text)
            (fn [res]
              ;; If request returned status code 200 we set user data
              (if (== (:status res) 200)
                (om/update! data :user (:body res))
                ;; If not, clear user data
                (om/update! data :user {})))))
    false)))

;; Search input component
(defcomponent search-input [data owner]
  (init-state [_]
    {:text ""})
  (render-state [_ {:keys [text]}]
    (dom/div {:class "row"}
      (dom/label {:for "search-input"} "Github username")
      (dom/input
        {:class "u-full-width"
         :type "text"
         :id "search-input"
         :on-key-down #(handle-keydown % data owner)}
      ))))

;; User data component, it display simple data or information that user wasn't found in the API.
(defcomponent user-data [data owner]
  (render [_]
    (let [user (:user data)]
      (if (empty? user)
        (dom/div {:class "row"}
          (dom/h1 "No user found"))
        (dom/div {:class "row"}
          (dom/div {:class "three columns"}
            (dom/img {:src (:avatar_url user)}))
          (dom/div {:class "nine columns"}
            (dom/h1 (:name user)
              (dom/small (str " (@" (:login user) ")")))
            (dom/p
              (dom/div
                (dom/strong "Location: ")
                (:location user))
              (dom/div
                (dom/strong "Repositories: ")
                (:public_repos user))
              (dom/div
                (dom/strong "Member since: ")
                (.format (goog.i18n.DateTimeFormat. "dd MMMM, yyyy") (js/Date. (:created_at user)))))
            (dom/a {:class "button" :href (:html_url user)} "Visit github profile")))))))

;; Main app component
(defcomponent app [data owner]
  (render (_)
    (dom/div {:class "container"}
      (om/build search-input data)
      (om/build user-data data))))

(om/root app
  app-state
  {:target (. js/document (getElementById "app"))})

