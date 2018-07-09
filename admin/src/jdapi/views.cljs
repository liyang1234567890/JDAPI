(ns jdapi.views
  (:require ["semantic-ui-react" :as se]
            [reagent.core :as r]))

(defn colored-div []
  [:div
   {:style {:background "red"
            :color "white"}}
   "COLORED div"])

(defn a-button []
  (let []
    (fn []
      [:> se/Button
       "Click Here"])))


(defn menu []
  [:> se/Menu
   [:> se/Menu.Item
    {:name "editorials"
     :on-click (fn [e] (js/console.log "on-click"))}
    "Editorials"]

   [:> se/Menu.Item
    {:name "editorials"
     :on-click (fn [] (js/console.log "on-click"))}
    [:span {:style {:color "blue"}} "Menu 2"]]])



(defn text-area []
  (let [val (r/atom "")]
    (fn []
     [:> se/Form
      [:> se/TextArea
       {:placeholder "some text"
        ;;     :auto-height true
        :value @val
        :on-change (fn [e]
                     ;; 
                     (reset! val (-> e .-target .-value)))
        }]])))

(defn a-div []
  [:> se/Container
   {:class-name "four column doubling stackable grid"}
   [:div#some-id.column
    [:p ""]
    [:p ""]]
   [menu]
   [text-area]
   [a-button]
   [colored-div]
   [:span "safsdkf"]
   [:div "dafd"]
   [:span "a div"]])





(defn home-page []
  #_[:div.home "HOME"]
  [a-div])
