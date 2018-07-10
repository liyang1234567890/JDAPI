(ns jdapi.events
  (:require
   [jdapi.db :as db]
   [re-frame.core :refer [dispatch reg-event-db
                          reg-event-fx reg-fx
                          reg-sub inject-cofx
                          after debug trim-v
                          path console]]))

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
 :repl-input
 (fn [db [_ input]]
   (assoc db :repl-input input)))

(reg-sub
 :on-repl-input
 (fn [db _]
   (:repl-input db)))



