(ns treeoflife.routes.home
  (:require [treeoflife.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn home-page []
  (layout/render
   "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn history []
  (layout/render
   "history.html" {:docs (-> "docs/history.md" io/resource slurp)}))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/history" [] (history))
  (GET "/about" [] (about-page)))
