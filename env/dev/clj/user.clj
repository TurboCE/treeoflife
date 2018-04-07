(ns user
  (:require 
            [mount.core :as mount]
            [treeoflife.core :refer [start-app]]))

(defn start []
  (mount/start-without #'treeoflife.core/repl-server))

(defn stop []
  (mount/stop-except #'treeoflife.core/repl-server))

(defn restart []
  (stop)
  (start))


