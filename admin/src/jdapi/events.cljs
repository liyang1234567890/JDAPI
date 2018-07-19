(ns jdapi.events
  (:require
   [jdapi.db :as db]
   [re-frame.core :refer [dispatch reg-event-db
                          reg-event-fx reg-fx
                          reg-sub inject-cofx
                          after debug trim-v
                          path console]]))

(def defaults-db
  {:basic    [{:name "basic-1"}
              {:name "basic-2"}]
   :derived  [{:name "derived-1"}]
   :advanced [{:name "advanced-1"}]})

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

(reg-event-db
 :mock-api-list
 (fn [db _]
   (assoc db :api-list defaults-db)))

(reg-sub
 :basic-api-list
 (fn [db _]
   (get-in db [:api-list :basic])))

(reg-sub
 :derived-api-list
 (fn [db _]
   (get-in db [:api-list :derived])))

(reg-sub
 :page
 (fn [db _]
   (:page db)))

(reg-event-db
 :set-active-page
 (fn [db [_ page]]
   (assoc db :page page)))


(reg-event-db
 :append-to-list
 (fn [db [_ category value]]
   (update-in db [:api-list category] conj {:name value})))

(reg-event-db
 :set-active-item
 (fn [db [_ category index]]
   (assoc db :active-item [category index])))

(reg-sub
 :active-item
 (fn [db _]
   (:active-item db)))

(reg-event-db
 :remove-from-list
 (fn [db [_ category index]]
   (update-in db [:api-list category]
              (fn [lst]
                (vec (concat (take index lst)
                             (drop (inc index) lst)))))))


(reg-event-db
 :set-edit-item
 (fn [db [_ category index]]
   (assoc db :edit-item [category index])))

(reg-sub
 :edit-item
 (fn [db _]
   (:edit-item db)))

(reg-event-db
 :set-item-name
 (fn [db [_ category index value]]
   (update-in db [:api-list category]
              (fn [v]
                (assoc-in v [index :name] value)))))
