(ns jdapi.util)

(defn indexed [data]
  (map vector (iterate inc 0) data))
