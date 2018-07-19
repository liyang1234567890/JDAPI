(ns jdapi.events
  (:require
   [jdapi.db :as db]
   [re-frame.core :refer [dispatch reg-event-db
                          reg-event-fx reg-fx
                          reg-sub inject-cofx
                          after debug trim-v
                          path console]]
   [ajax.core :as ajax]
   [jdapi.helper :refer [proxy-uri]]))

(def default-api-list
  {:basic    [{:name "basic-1"}
              {:name "basic-2"}
              {:name "basic-3"}]
   :derived  [{:name "derived-1"}
              {:name "derived-2"}
              {:name "derived-3"}]
   :advanced [{:name "advanced-1"}
              {:name "advanced-2"}
              {:name "advanced-3"}]})

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
   (assoc db :api-list default-api-list)))


(reg-sub
 :basic-api-list
 (fn [db _]
   (get-in db [:api-list :basic])))

(reg-sub
 :derived-api-list
 (fn [db _]
   (get-in db [:api-list :derived])))

(reg-sub
 :advanced-api-list
 (fn [db _]
   (get-in db [:api-list :advanced])))

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


(reg-event-db
 :set-active-panel
 (fn [db [_ category index]]
   (assoc db :panel-attr [category index])))

(reg-sub
 :active-panel-attr
 (fn [db _]
   (:panel-attr db)))

(reg-event-db
 :on-repl-save
 (fn [db [_ category index val]]
   (assoc-in db [:repl-contents category index] val)))


(def default-http-xhrio
  {:method          :get
   :format          (ajax/json-request-format)
   :response-format (ajax/json-response-format {:keyswords? true})
   :on-success      [:xhrio-null-success]
   :on-failure      [:xhrio-null-failed]})

(defn make-request
  [{:keys [method uri on-success on-failed params body]}]
  {:http-xhrio
   (cond-> default-http-xhrio
     method (assoc :method method)
     uri (assoc :uri (proxy-uri uri))
     on-success (assoc :on-success on-success)
     on-failed (assoc :on-failed on-failed)
     params (assoc :params params)
     body (assoc :body params))})

(reg-event-fx
 :on-repl-run
 (fn [{:keys [db]} [_ category index val]]
   (merge (make-request
           {:method     :post
            :uri        "xxx"
            :params     {:repl (:repl val)}
            :on-success [:on-repl-run-success category index val]})
          {:dispatch {:on-repl-save category index val}})))

(reg-event-db
 :on-repl-run-success
 (fn [db [_ category index val response]]
   (assoc-in db [:repl-contents category index]
             (assoc val :result (:data response)))))


(reg-sub
 :repl-val
 (fn [db _]
   (get-in db [:repl-contents
               (get-in db [:panel-attr 0])
               (get-in db [:panel-attr 1])
               :repl])))

(reg-sub
 :note-val
 (fn [db _]
   (get-in db [:repl-contents
               (get-in db [:panel-attr 0])
               (get-in db [:panel-attr 1])
               :note])))
