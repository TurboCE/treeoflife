(ns treeoflife.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [treeoflife.layout :refer [error-page]]
            [treeoflife.routes.home :refer [home-routes admin-routes]]
            [compojure.route :as route]
            [treeoflife.env :refer [defaults]]
            [mount.core :as mount]
            [treeoflife.middleware :as middleware]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(mount/defstate app
  :start
  (middleware/wrap-base
   (routes
    (->
        #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (->
        #'admin-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats)
        (wrap-routes middleware/wrap-restricted)
        )
    (route/not-found
     (:body
      (error-page {:status 404
                   :title "page not found"}))))))
