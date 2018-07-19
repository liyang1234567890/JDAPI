(defproject secudata "0.1.0-SNAPSHOT"
  :description "Joudou API service, secudata API"
  :url "https://www.joudou.com"
  :license {:name "Commercial"
            :url  "www.joudou.com"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.7.3"]
                 [org.postgresql/postgresql "42.2.2"]
                 [mysql/mysql-connector-java "5.1.38"]
                 [honeysql "0.7.0"]                       ;; SQL builder
                 [hikari-cp "1.8.3"]

                 [org.clojure/tools.cli "0.3.5"]          ;; Command line
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.clojure/tools.logging "0.4.0"]
                 [clj-http "2.2.0"]                       ;; HTTP Client
                 [ring "1.6.3"]                           ;; WEB HTTP framework
                 [ring/ring-jetty-adapter "1.6.3"]
                 [compojure "1.5.0"]                      ;; WEB Route framework
                 [selmer "1.0.7"]                         ;; WEB Template like Django
                 [cheshire "5.6.3"]                       ;; JSON
                 [clojurewerkz/quartzite "2.0.0"]         ;; 定时任务
                 [org.clojure/data.xml "0.0.8"]
                 [proto-repl "0.3.1"]                     ;; for atom
                 [org.clojure/core.async "0.2.391"]
                 [com.taoensso/timbre "4.7.4"]
                 [com.taoensso/tufte "1.0.2"]
                 [mount "0.1.11"]
                 [com.fzakaria/slf4j-timbre "0.3.7"]
                 [com.draines/postal "2.0.2"]             ;; send mail

                 [joudou/jdcommon "0.1.0"]]

  :plugins [[lein-ring "0.9.7"]
            [lein-tar "3.3.0"]
            [lein-kibit "0.1.2"]
            [jonase/eastwood "0.2.3"]]
  :jvm-opts ["-Xmx1g" "-Dfile.encoding=UTF-8"]
  :main ^:skip-aot secudata.core
  :omit-source true
  :profiles {:uberjar {:aot [secudata.core]}}
  :repositories {"joudoufile" ~(str (.toURI (java.io.File.
                                             (str (System/getProperty "user.home")
                                                  "/local_m2_repo/"))))})
