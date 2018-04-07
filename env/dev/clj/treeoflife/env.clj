(ns treeoflife.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [treeoflife.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[treeoflife started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[treeoflife has shut down successfully]=-"))
   :middleware wrap-dev})
