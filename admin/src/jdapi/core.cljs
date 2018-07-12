(ns jdapi.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ajax.core :refer [GET POST]]
            [jdapi.views :as views]
            [jdapi.styles :refer [styles]]
            [jdapi.events]
            [re-frisk.core :refer [enable-re-frisk! enable-frisk!]]
            ))

(def pages
  {:home  #'views/home-page})

(defn page []
  [:div
   [styles]
   [(pages @(rf/subscribe [:page]))]])


;; -------------------------
;; Initialize app
(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn ^:export init! []
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch [:set-active-page :home])
  #_(routes/hook-browser-navigation!)
  (mount-components)
  (enable-re-frisk! {:x 100 :y 500})
  (enable-frisk! {:x 100 :y 500}))
