(ns jdapi.views
  (:require ["semantic-ui-react" :as se]
            [reagent.core :as r]
            [re-frame.core :as rf]))

(defn side-bar []
  (let [visible (r/atom true)]
    (fn []
       [:> se/Sidebar.Pushable
        {:as se/Segment}
        [:> se/Sidebar
         
         {
          :as        se/Menu
          :animation "overlay"
          :icon      "labeled"
          :inverted  true
          :vertical  true
          :visible   @visible
          :width     "thin"
          }
         [:> se/Menu.Item
          [:> se/Header
           {:style {:color "gray"}}
           "API管理后台"]
          ]
         [:> se/Menu.Item
          [:> se/Header
           {:style {:font-size "20px"
                    :color     "white"}}
           "基础API"]
          [:> se/Menu
           {:vertical true
            :compact   true
            :fluid     true
            }
           [:> se/Menu.Item "第一行"]
           [:> se/Menu.Item "第二行"]
           [:> se/Menu.Item "第三行"]
           ]
          ]
         [:> se/Menu.Item
          [:> se/Header
           {:style {:font-size "20px"
                    :color     "white"}}
           "衍生API"]
          [:> se/Menu
           { :vertical true
            :compact   true
            :fluid     true
            }
           [:> se/Menu.Item "第一行"]
           [:> se/Menu.Item "第二行"]
           [:> se/Menu.Item "第三行"]
           ]
          ]
         [:> se/Menu.Item
          [:> se/Header
           {:style {:font-size "20px"
                    :color     "white"}}
           "高级API"]
          [:> se/Menu
           { :vertical true
            :compact   true
            :fluid     true
            }
           [:> se/Menu.Item "第一行"]
           [:> se/Menu.Item "第二行"]
           [:> se/Menu.Item "第三行"]
           ]
          ]]
   ])))

(defn text-area-form []
  (let [val (r/atom "")]
    (fn []
     [:> se/Form
      [:> se/TextArea
       {:placeholder "Tell me more"
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
      [:> se/Message.Content @repl-input]
      ]
      )
    )
  )

(defn table-message []
  (fn []
    [:> se/Table
     {:fixed true}
     [:> se/Table.Header
      [:> se/Table.Row
       [:> se/Table.HeaderCell "Name"]
       [:> se/Table.HeaderCell "Status"]
       [:> se/Table.HeaderCell "Description"]
       ]
      ]
     [:> se/Table.Body
      [:> se/Table.Row
       [:> se/Table.Cell "john"]
       [:> se/Table.Cell "Approved"]
       [:> se/Table.Cell "John is a boy"]
       ]
      [:> se/Table.Row
       [:> se/Table.Cell "Jamie"]
       [:> se/Table.Cell "Approved"]
       [:> se/Table.Cell "Jamie  is a girl"]
       ]
      [:> se/Table.Row
       [:> se/Table.Cell "nnnn"]
       [:> se/Table.Cell "Denied"]
       [:> se/Table.Cell "nnnn is a boy"]
       ]
      ]
     ]
    )
  )

(defn side-push []
  [:> se/Sidebar.Pusher
   [:> se/Segment
    {:basic  true
     :style {
             :margin-left "150px"
             :margin-top  "-700px"
             }
     }
    [text-area-form]
    [feedback-message]
    [table-message]

    ]
   ]
  )

(defn container []
  [:> se/Container
   {:style {:height "700px"
            :width "100%"
            :margin 0
            :padding 0}}
   [side-bar]
   [side-push]
   ])

(defn a-div []
  [:> se/Container
   [text-area-form]
   [feedback-message]
   [side-bar]
   [side-push]
   [container]
   [table-message]
   ])

(defn home-page []
  #_[:div.home "HOME"]
  [container])

