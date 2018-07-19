(ns jdapi.helper)

(def env "beta")

(defn proxy-uri
  [uri]
  (str "/p/" env uri))
