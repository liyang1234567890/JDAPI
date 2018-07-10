(ns jdapi.views
  (:require ["semantic-ui-react" :as se]
            [reagent.core :as r]
            [re-frame.core :as rf]))

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



(defn text-area-form []
  (let [val (r/atom "")]
    (fn []
     [:> se/Form
      [:> se/TextArea
       {:placeholder "REPL:"
            :auto-height true
        :value @val
        :on-change (fn [e]
                     (reset! val (-> e .-target .-value)))
        }]
      [:> se/Button
       {:style {:margin-top "10px"}
        :primary true
        :size "mini"
        :on-click (fn [e] (rf/dispatch [:repl-input @val]))}
       "Submit"]
      ])))

(defn feedback-message []
  (let [repl-input (rf/subscribe [:on-repl-input])]
    (fn []
     [:> se/Message
      {:info true}
      [:> se/Message.Header "feedback"]
      [:> se/Message.Content @repl-input]])))

(defn a-div []
  [:> se/Container
   {:class-name "four column doubling stackable grid"}
   [:div#some-id.column
    [:p ""]
    [:p ""]]
   [menu]
   [text-area-form]
   [a-button]
   [colored-div]
   [:span "safsdkf"]
   [:div "dafd"]
   [:span "a div"]])


(defn side-bar []
  (let [visible (r/atom true)]
    (fn []
      [:> se/Sidebar.Pushable
       {:as se/Segment}
       [:> se/Sidebar
        {:visible   @visible
         :as        se/Menu
         :animation "overlay"
         :icon      "labeled"
         :inverted  true
         :vertical  true
         :width     "thin"}
        [:> se/Menu.Item
         [:> se/Header
          {:style {:color "#dcdcdc"}}
          "API管理后台"]]
        [:> se/Menu.Item
         [:> se/Header
          {:style {:font-size   "16px"
                   :font-weight "normal"
                   :color       "#999"}}
          "基础API"]
         [:> se/Menu
          {:style    {:background "rgba(0,0,0,0)"}
           :vertical true
           :compact  true
           :fluid    true}
          [:> se/Menu.Item
           "ddd"]
          [:> se/Menu.Item
           "ddd"]
          [:> se/Menu.Item
           "ddd"]
          [:> se/Menu.Item
           "ddd"]]]
        

        [:> se/Menu.Item
         [:> se/Header
          {:style {:font-size   "16px"
                   :font-weight "normal"
                   :color       "#999"}}
          "衍生API"]
         [:> se/Menu
          {:style    {:background "rgba(0,0,0,0)"}
           :vertical true
           :compact  true
           :fluid    true}
          [:> se/Menu.Item "ddd"]
          [:> se/Menu.Item "ddd"]
          [:> se/Menu.Item "ddd"]
          [:> se/Menu.Item "ddd"]]]

        [:> se/Menu.Item
         [:> se/Header
          {:style {:font-size   "16px"
                   :font-weight "normal"
                   :color       "#999"}}
          "高级API"]
         [:> se/Menu
          {:style    {:background "rgba(0,0,0,0)"}
           :vertical true
           :compact  true
           :fluid    true}
          [:> se/Menu.Item "ddd"]
          [:> se/Menu.Item "ddd"]
          [:> se/Menu.Item "ddd"]
          [:> se/Menu.Item "ddd"]]]
        #_[:> se/Button
         {:on-click (fn [e] (swap! visible not))}
         "Hide"]
        ]
       [:> se/Sidebar.Pusher
        [:> se/Segment
         {:basic true
          :style {:margin-left "150px"}}
         [text-area-form]
         [feedback-message]
         ]]
       ])))

(defn container []
  [:> se/Container
   {:style {:height "700px"
            :width "100%"
            :margin 0
            :padding 0}}
   [side-bar]
])


(defn home-page []
  #_[:div.home "HOME"]
  [container])
