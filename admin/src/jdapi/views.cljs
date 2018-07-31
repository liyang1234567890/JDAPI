(ns jdapi.views
  (:require ["semantic-ui-react" :as se]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [jdapi.util :refer [indexed]]
            [cljs.reader :refer [read-string]]
            [clojure.string :as str]))

(defn text-area-form [[category index]]
  (let [repl-val (r/atom "")
        note-val (r/atom "")
        repl     (rf/subscribe [:repl-val])
        note     (rf/subscribe [:note-val])]
    (if (or (nil? category) (nil? index))
      [:div ]
      (r/create-class
       {:component-will-receive-props (fn [el]
                                        (reset! repl-val (or @repl ""))
                                        (reset! note-val (or @note "")))
        :reagent-render
        (fn [[category index]]
          (let [[api-category api-key] [category index]]
            [:> se/Form
             [:> se/TextArea
              {:style       {:margin-bottom "20px"}
               :placeholder "REPL:"
               :auto-height true
               :value       @repl-val
               :on-change   (fn [e]
                              (reset! repl-val (-> e .-target .-value)))
               }]  
             [:> se/TextArea
              {
               :placeholder "备注："
               :auto-height true
               :value       @note-val
               :on-change   (fn [e]
                              (reset! note-val (-> e .-target .-value)))
               }]
             [:> se/Button
              {:style    {:margin-top    "15px"
                          :margin-bottom "15px"
                          :margin-right  "30px"
                          }
               :primary  true
               :size     "small"
               :on-click (fn [e] (rf/dispatch [:on-repl-save api-category api-key {:repl @repl-val
                                                                                   :note @note-val}]))}
              "保存"]
             [:> se/Button
              {:style    {:margin-top    "15px"
                          :margin-bottom "15px"
                          }
               :primary  true
               :size     "small"
               :on-click (fn [e] (rf/dispatch [:on-repl-run api-category api-key {:repl @repl-val
                                                                                  :note @note-val}]))}
              "运行"]
             ]))}))))

(defn feedback-message []
  (let [repl-input (rf/subscribe [:on-repl-input])]
    (fn []
      [:> se/Message
       {:info true}
       [:> se/Message.Header "计算结果"]
       [:> se/Message.Content @repl-input]])))

(defn basic-api-list []
  (let [basic       (rf/subscribe [:basic-api-list])
        show-input  (r/atom false)
        val         (r/atom "未命名")
        active-item (rf/subscribe [:active-item])
        edit-item   (rf/subscribe [:edit-item])
        edit-val    (r/atom "")
        panel-attr  (rf/subscribe [:active-panel-attr])]
    (fn []
      (let [[panel-category panel-key] @panel-attr]
        [:> se/Menu.Item
         [:> se/Menu.Header
          {:style {:font-size   "20px"
                   :font-weight "normal"
                   :font-family "Yuanti SC"
                   :color       "#999"}}
          "基础API"]
         (let [[active-category active-index] @active-item
               [edit-categroy edit-index]     @edit-item]
           [:> se/Menu.Menu
            {:style {:background "rgba(0,0,0,0)"}}
            (doall
             (for [[index item] (indexed @basic)
                   :let         [active (and (= :basic active-category)
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
                                               :on-change (fn [e]
                                                            (reset! edit-val (-> e .-target .-value)))
                                               :on-blur   (fn [e]
                                                            (rf/dispatch [:set-edit-item :basic nil])
                                                            (when-not (empty? @edit-val)
                                                              (rf/dispatch [:set-item-name :basic index @edit-val]))
                                                            (reset! edit-val ""))}])}))]
                 ^{:key (str "item" index)}
                 [:> se/Menu.Item
                  {:active         (and (= :basic panel-category) (= index panel-key))
                   :style          {:text-align "center"}
                   :on-click       (fn [e]
                                     (rf/dispatch [:set-active-panel :basic index]))
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
                        :on-click (fn [e] (rf/dispatch [:remove-from-list :basic index]))}]]
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
              [(let [!ref (atom nil) default-val "未命名"
                     l (rf/subscribe [:basic-api-list])
                     vals (->> @l
                               (map :name)
                               (filter (fn [s] (str/starts-with? s "未命名-")))
                               (map (fn [s] (str/replace-first s "未命名-" "")))
                               (filter (fn [s] (number? (read-string s))))
                               (map read-string))] 
                         (if (empty? vals)
                         "未命名"
                         (str "未命名-" (inc (apply max vals))))
                 (r/create-class
                  {:component-will-mount (fn [](reset! val @default-val))
                   :component-did-mount  (fn []
                                           (some-> @!ref .focus))
                   :reagent-render       (fn []
                                           [:> se/Input
                                            {:ref       (fn [com] (reset! !ref com))
                                             :value     @val
                                             :size      "mini"
                                             :on-change (fn [e]
                                                          (reset! val (-> e .-target .-value)))
                                             :on-blur   (fn [e]
                                                          (reset! show-input false)
                                                          (when-not (empty? @val)
                                                            (rf/dispatch [:append-to-list :basic @val])))}])}))])
            [:> se/Menu.Item
             {:on-click #(reset! show-input true)}
             [:> se/Icon
              {:name "add"}]]])]))))

(defn  derived-api-list []
  (let [derived       (rf/subscribe [:derived-api-list])
        show-input  (r/atom false)
        val         (r/atom "未命名")
        active-item (rf/subscribe [:active-item])
        edit-item   (rf/subscribe [:edit-item])
        edit-val    (r/atom "")
        panel-attr  (rf/subscribe [:active-panel-attr])]
    (fn []
      (let [[panel-category panel-key] @panel-attr]
        [:> se/Menu.Item
         [:> se/Menu.Header
          {:style {:font-size   "20px"
                   :font-weight "normal"
                   :font-family "Yuanti SC"
                   :color       "#999"}}
          "衍生API"]
         (let [[active-category active-index] @active-item
               [edit-categroy edit-index]     @edit-item]
           [:> se/Menu.Menu
            {:style {:background "rgba(0,0,0,0)"}}
            (doall
             (for [[index item] (indexed @derived)
                   :let         [active (and (= :derived active-category)
                                             (= index active-index))]]
               (if (and (= :derived edit-categroy)
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
                                               :on-change (fn [e]
                                                            (reset! edit-val (-> e .-target .-value)))
                                               :on-blur   (fn [e]
                                                            (rf/dispatch [:set-edit-item :derived nil])
                                                            (when-not (empty? @edit-val)
                                                              (rf/dispatch [:set-item-name :derived index @edit-val]))
                                                            (reset! edit-val ""))}])}))]
                 ^{:key (str "item" index)}
                 [:> se/Menu.Item
                  {:active         (and (= :derived panel-category) (= index panel-key))
                   :style          {:text-align "center"}
                   :on-click       (fn [e]
                                     (rf/dispatch [:set-active-panel :derived index]))
                   :on-mouse-enter (fn [e]
                                     (rf/dispatch [:set-active-item :derived index]))
                   :on-mouse-leave (fn [e]
                                     (rf/dispatch [:set-active-item :derived nil]))}
                  (if active
                    [:> se/Grid
                     {:columns "16"}
                     [:> se/Grid.Column
                      {:width "4"}
                      [:> se/Icon
                       {:name     "minus"
                        :on-click (fn [e] (rf/dispatch [:remove-from-list :derived index]))}]]
                     [:> se/Grid.Column {:width "8"}
                      (:name item)]
                     [:> se/Grid.Column {:width "4"}
                      [:> se/Icon
                       {:name     "edit"
                        :on-click (fn [e]
                                    (js/console.log (:name item))
                                    (reset! edit-val (:name item))
                                    (rf/dispatch [:set-edit-item :derived index]))}]]]
                    [:> se/Grid {:columns "16"}
                     [:> se/Grid.Column {:width "4"}]
                     [:> se/Grid.Column {:width "8"} (:name item)]
                     [:> se/Grid.Column {:width "4"}]])])))
            (when @show-input
              [(let [!ref (atom nil) default-val "未命名"
                     l (rf/subscribe [:derived-api-list])
                     vals (->> @l
                               (map :name)
                               (filter (fn [s] (str/starts-with? s "未命名-")))
                               (map (fn [s] (str/replace-first s "未命名-" "")))
                               (filter (fn [s] (number? (read-string s))))
                               (map read-string))]
                         (if (empty? vals)
                         "未命名"
                         (str "未命名-" (inc (apply max vals))))
                 (r/create-class
                  {:component-will-mount (fn [](reset! val @default-val))
                   :component-did-mount  (fn []
                                           (some-> @!ref .focus))
                   :reagent-render       (fn []
                                           [:> se/Input
                                            {:ref       (fn [com] (reset! !ref com))
                                             :value     @val
                                             :size      "mini"
                                             :on-change (fn [e]
                                                          (reset! val (-> e .-target .-value)))
                                             :on-blur   (fn [e]
                                                          (reset! show-input false)
                                                          (when-not (empty? @val)
                                                            (rf/dispatch [:append-to-list :derived @val])))}])}))])
            [:> se/Menu.Item
             {:on-click #(reset! show-input true)}
             [:> se/Icon
              {:name "add"}]]])]))))

(defn advanced-api-list []
  (let [advanced       (rf/subscribe [:advanced-api-list])
        show-input  (r/atom false)
        val         (r/atom "未命名")
        active-item (rf/subscribe [:active-item])
        edit-item   (rf/subscribe [:edit-item])
        edit-val    (r/atom "")
        panel-attr  (rf/subscribe [:active-panel-attr])]
    (fn []
      (let [[panel-category panel-key] @panel-attr]
        [:> se/Menu.Item
         [:> se/Menu.Header
          {:style {:font-size   "20px"
                   :font-weight "normal"
                   :font-family "Yuanti SC"
                   :color       "#999"}}
          "高级API"]
         (let [[active-category active-index] @active-item
               [edit-categroy edit-index]     @edit-item]
           [:> se/Menu.Menu
            {:style {:background "rgba(0,0,0,0)"}}
            (doall
             (for [[index item] (indexed @advanced)
                   :let         [active (and (= :advanced active-category)
                                             (= index active-index))]]
               (if (and (= :advanced edit-categroy)
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
                                               :on-change (fn [e]
                                                            (reset! edit-val (-> e .-target .-value)))
                                               :on-blur   (fn [e]
                                                            (rf/dispatch [:set-edit-item :advanced nil])
                                                            (when-not (empty? @edit-val)
                                                              (rf/dispatch [:set-item-name :advanced index @edit-val]))
                                                            (reset! edit-val ""))}])}))]
                 ^{:key (str "item" index)}
                 [:> se/Menu.Item
                  {:active         (and (= :advanced panel-category) (= index panel-key))
                   :style          {:text-align "center"}
                   :on-click       (fn [e]
                                     (rf/dispatch [:set-active-panel :advanced index]))
                   :on-mouse-enter (fn [e]
                                     (rf/dispatch [:set-active-item :advanced index]))
                   :on-mouse-leave (fn [e]
                                     (rf/dispatch [:set-active-item :advanced nil]))}
                  (if active
                    [:> se/Grid
                     {:columns "16"}
                     [:> se/Grid.Column
                      {:width "4"}
                      [:> se/Icon
                       {:name     "minus"
                        :on-click (fn [e] (rf/dispatch [:remove-from-list :advanced index]))}]]
                     [:> se/Grid.Column {:width "8"}
                      (:name item)]
                     [:> se/Grid.Column {:width "4"}
                      [:> se/Icon
                       {:name     "edit"
                        :on-click (fn [e]
                                    (js/console.log (:name item))
                                    (reset! edit-val (:name item))
                                    (rf/dispatch [:set-edit-item :advanced index]))}]]]
                    [:> se/Grid {:columns "16"}
                     [:> se/Grid.Column {:width "4"}]
                     [:> se/Grid.Column {:width "8"} (:name item)]
                     [:> se/Grid.Column {:width "4"}]])])))
             (when @show-input
              [(let [!ref (atom nil) default-val "未命名"
                     l (rf/subscribe [:advanced-api-list])
                     vals (->> @l
                               (map :name)
                               (filter (fn [s] (str/starts-with? s "未命名-")))
                               (map (fn [s] (str/replace-first s "未命名-" "")))
                               (filter (fn [s] (number? (read-string s))))
                               (map read-string))]
                         (if (empty? vals)
                         "未命名"
                         (str "未命名-" (inc (apply max vals))))
                 (r/create-class
                  {:component-will-mount (fn [](reset! val @default-val))
                   :component-did-mount  (fn []
                                           (some-> @!ref .focus))
                   :reagent-render       (fn []
                                           [:> se/Input
                                            {:ref       (fn [com] (reset! !ref com))
                                             :value     @val
                                             :size      "mini"
                                             :on-change (fn [e]
                                                          (reset! val (-> e .-target .-value)))
                                             :on-blur   (fn [e]
                                                          (reset! show-input false)
                                                          (when-not (empty? @val)
                                                            (rf/dispatch [:append-to-list :advanced @val])))}])}))])
            [:> se/Menu.Item
             {:on-click #(reset! show-input true)}
             [:> se/Icon
              {:name "add"}]]])]))))

(defn side-bar []
  (let [visible    (r/atom true)
        panel-attr (rf/subscribe [:active-panel-attr])]
    (fn []
      (let [attr @panel-attr]
        [:> se/Sidebar.Pushable
         {:as se/Segment}
         [:> se/Sidebar
          {:style     {:width "220px"}
           :visible   @visible
           :as        se/Menu
           :animation "overlay"
           :icon      "labeled"
           :inverted  true
           :vertical  true
           }
          [:> se/Menu
           {:vertical true
            :fluid    true
            :style    {:background "#191a1c"}}
           [:> se/Menu.Header
            
            {:style {:font-size     "25px"
                     :font-family   "Yuanti SC"
                     :color         "white"
                     :margin-top    "20px"
                     :margin-bottom "20px"}}
            "API管理后台"]
           [basic-api-list]
           [derived-api-list]
           [advanced-api-list]
           
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
            :style {:margin-left "240px"
                    }}
           [text-area-form attr]
           [feedback-message]]]]))))

(defn container []
  [:> se/Container
   {:style {:height  "1000px"
            :width   "100%"
            :margin  0
            :padding 0}}
   [side-bar]])

(defn mock-pool []
  [:> se/Button
   {:on-click (fn [e]
                (rf/dispatch [:mock-api-list]))}
   "api-list"])

(defn a-div []
  [:> se/Container
   [text-area-form]
   [feedback-message]
   [side-bar]
   #_[side-push]
   [container]
   #_[table-message]
   ])


(defn home-page []
  #_[:div.home "HOME"]
  [:div
   [mock-pool]
   [container]])

