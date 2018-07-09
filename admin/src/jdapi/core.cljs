(ns jdapi.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ajax.core :refer [GET POST]]
            [jdapi.views :as views]
            [jdapi.styles :refer [styles]]
            ))

#_(def pages
  {:home  #'views/home-page})

(defn page []
  [:div
   [styles]
   [views/home-page]
   #_[(pages @(rf/subscribe [:page]))]])


;; -------------------------
;; Initialize app
(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn ^:export init! []
  (rf/dispatch-sync [:initialize-db])
  #_(routes/hook-browser-navigation!)
  (mount-components))
