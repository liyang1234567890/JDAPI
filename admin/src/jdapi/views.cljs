(ns jdapi.views
  (:require ["semantic-ui-react" :as se]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [jdapi.util :refer [indexed]]))

(defn colored-div []
  [:div
   {:style {:background "red"
            :color      "white"}}
   "COLORED div"])

(defn a-button []
  (let []
    (fn []
      [:> se/Button
       "Click Here"])))


(defn menu []
  [:> se/Menu
   [:> se/Menu.Item
    {:name     "editorials"
     :on-click (fn [e] (js/console.log "on-click"))}
    "Editorials"]

   [:> se/Menu.Item
    {:name     "editorials"
     :on-click (fn [] (js/console.log "on-click"))}
    [:span {:style {:color "blue"}} "Menu 2"]]])



(defn text-area-form []
  (let [val (r/atom "")]
    (fn []
      [:> se/Form
       [:> se/TextArea
        {:placeholder "REPL:"
         :auto-height true
         :value       @val
         :on-change   (fn [e]
                        (reset! val (-> e .-target .-value)))
         }]
       [:> se/Button
        {:style    {:margin-top "10px"}
         :primary  true
         :size     "mini"
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

#_(defn a-div []
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

(defn basic-api-list []
  (let [basic      (rf/subscribe [:basic-api-list])
        show-input (r/atom false)
        val        (r/atom "未命名")
        active-item (rf/subscribe [:active-item])
        edit-item (rf/subscribe [:edit-item])
        edit-val (r/atom "")]
    (fn []
      [:> se/Menu.Item
       [:> se/Menu.Header
        {:style {:font-size   "16px"
                 :font-weight "normal"
                 :color       "#999"}}
        "基础API"]
       (let [[active-category active-index] @active-item
             [edit-categroy edit-index] @edit-item]
        [:> se/Menu.Menu
         {:style {:background "rgba(0,0,0,0)"}}
         (doall
          (for [[index item] (indexed @basic)
                :let [active (and (= :basic active-category)
                                  (= index active-index))]]
            (if (and (= :basic edit-categroy)
                     (= index edit-index))
              ^{:key (str "edit" index)}
              [(let [!ref (atom nil)]
                   (r/create-class
                    {:display-name        "autofocus-edit"
                     :component-did-mount (fn []
                                            (some-> @!ref .focus))
                     :reagent-render      (fn []
                                            [:> se/Input
                                             {:ref       (fn [com] (reset! !ref com))
                                              :value     @edit-val
                                              :size      "mini"
                                              :focus     true
                                              :on-change (fn [e]
                                                           (reset! edit-val (-> e .-target .-value)))
                                              :on-blur   (fn [e]
                                                           (rf/dispatch [:set-edit-item :basic nil])
                                                           (when-not (empty? @edit-val)
                                                             (rf/dispatch [:set-item-name :basic index @edit-val]))
                                                           (reset! edit-val ""))}])}))]
              ^{:key (str "item" index)}
              [:> se/Menu.Item
               {:style          {:text-align "center"}
                :on-mouse-enter (fn [e]
                                  (rf/dispatch [:set-active-item :basic index]))
                :on-mouse-leave (fn [e]
                                  (rf/dispatch [:set-active-item :basic nil]))}
               (if active
                 [:> se/Grid
                  {:columns "16"}
                  [:> se/Grid.Column
                   {:width "4"}
                   [:> se/Icon
                    {:name     "minus"
                     :on-click (fn [e]
                                 (rf/dispatch [:remove-from-list :basic index]))}]]
                  [:> se/Grid.Column {:width "8"}
                   (:name item)]
                  [:> se/Grid.Column {:width "4"}
                   [:> se/Icon
                    {:name     "edit"
                     :on-click (fn [e]
                                 (js/console.log (:name item))
                                 (reset! edit-val (:name item))
                                 (rf/dispatch [:set-edit-item :basic index]))}]]]
                 [:> se/Grid {:columns "16"}
                  [:> se/Grid.Column {:width "4"}]
                  [:> se/Grid.Column {:width "8"} (:name item)]
                  [:> se/Grid.Column {:width "4"}]])])))
         (when @show-input
           [:> se/Input
            {:value     @val
             :size      "mini"
             :focus true
             :on-change (fn [e]
                          (reset! val (-> e .-target .-value)))
             :on-blur   (fn [e]
                          (reset! show-input false)
                          (when-not (empty? @val)
                            (rf/dispatch [:append-to-list :basic @val])))}])
         [:> se/Menu.Item
          {:on-click #(reset! show-input true)}
          [:> se/Icon
           {:name "add"}]]])])))


(defn derived-api-list []
  (let [derived (rf/subscribe [:derived-api-list])]
    (fn []
      [:> se/Menu.Item
       [:> se/Menu.Header
        {:style {:font-size   "16px"
                 :font-weight "normal"
                 :color       "#999"}}
        "衍生API"]
       [:> se/Menu.Menu
        {:style {:background "rgba(0,0,0,0)"}}
        (doall
         (for [item @derived]
           ^{:key (:name item)}
           [:> se/Menu.Item
            (:name item)]))]])))

(defn side-bar []
  (let [visible (r/atom true)]
    (fn []
      [:> se/Sidebar.Pushable
       {:as se/Segment}
       [:> se/Sidebar
        {:style {:width "180px"}
         :visible   @visible
         :as        se/Menu
         :animation "overlay"
         :icon      "labeled"
         :inverted  true
         :vertical  true
         }
        [:> se/Menu
         {:vertical true
          :fluid true
          :style    {:background "#191a1c"}}
         [:> se/Menu.Header
          {:style {:font-size "16px"
                   :color     "#dcdcdc"}}
          "API管理后台"]
         [basic-api-list]
         [derived-api-list]
         ;; TODO
         #_[:> se/Menu.Item
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
             [:> se/Menu.Item "ddd"]]]]
        #_[:> se/Button
           {:on-click (fn [e] (swap! visible not))}
           "Hide"]
        ]
       [:> se/Sidebar.Pusher
        [:> se/Segment
         {:basic true
          :style {:margin-left "150px"}}
         [text-area-form]
         [feedback-message]]]])))

(defn container []
  [:> se/Container
   {:style {:height  "700px"
            :width   "100%"
            :margin  0
            :padding 0}}
   [side-bar]])

(defn mock-pool []
  [:> se/Button
   {:on-click (fn [e]
                (rf/dispatch [:mock-api-list]))}
   "api-list"])


(defn home-page []
  #_[:div.home "HOME"]
  [:div
   [mock-pool]
   [container]])
