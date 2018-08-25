(ns treeoflife.routes.home
  (:require [treeoflife.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [markdown.core :refer [md-to-html-string]]
            [ring.util.http-response :as response]
            [treeoflife.routes.auth :refer [auth-routes]]
            [treeoflife.page :refer [get_contents get_auth_info save_contents
                                     view-page parse-page]]
	    [ring.util.response :refer [response]]
	    [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defn edit-page
  ""
  [pagekey {session :session}]
  (let [contents (get_contents pagekey)]
    (layout/render
     "view.html"
     (merge
      {:pagekey pagekey}
      (parse-page (get_contents "edit-page")
                  (merge {:pagekey pagekey} (if (= contents nil)
                                              {:option {:document :markdown, :header 'nav-bar, :footer nil}}
                                              contents) (get_auth_info session)))
      ))))

; TODO: Save할 때 clojure runtime check
(defn save-page
  ""
  [pagekey title option body {session :session}]
  (let [contents (save_contents pagekey title option body)]
    (view-page
     "message_save"
     session
     {:pagekey pagekey
      :title title
      :option option
      :body body
      :params contents})))

(defroutes admin-routes
  (GET "/edit" [pagekey title body :as req] (edit-page pagekey req))
  (POST "/save" [pagekey title option body :as req] (save-page pagekey title option body req))
  )

(defn view-pagekey [pagekey {session :session}]
  (view-page pagekey session))

(defroutes home-routes
  (GET "/" req (view-pagekey "front_page" req))
  (GET "/view" [pagekey :as req] (view-pagekey pagekey req))
  (GET "/view/:pagekey" [pagekey :as req] (view-pagekey pagekey req))
  auth-routes)
