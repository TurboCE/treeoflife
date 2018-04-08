(ns treeoflife.routes.home
  (:require [treeoflife.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def error_page_not_found
  {:pagekey "page_not_found"
   :title "Page Not Found!"
   :body "Page Not Found! Body"})

(def error_page_save_failed
  {:pagekey "page_save_error"
   :title "Page Save Error!"
   :body "Page Save Error! Body"})

(defn get_contents [pagekey]
  (try
    (edn/read-string (slurp (str "pages/" pagekey)))
    (catch Exception e error_page_not_found)))

(defn save_contents [pagekey title option body]
  (let [contents {:title title
                  :option option
                  :body body}]
    (-> (str "pages/" pagekey) (spit (prn-str contents)))))

(defn rev_escape_html
  [text]
  (.. #^String (hiccup.util/as-str text)
      (replace "&amp;" "&")
      (replace "&lt;" "<")
      (replace "&gt;" ">")
      (replace "&quot;" "\"")))

(defn edit-page [pagekey]
  (let [contents (get_contents pagekey)]
    (layout/render
     "edit.html"
     (merge
      {:pagekey pagekey}
      (if (= contents error_page_not_found) nil contents))
     )))

(defn save-page [pagekey title option body]
  (let [contents (save_contents pagekey title option body)]
    (layout/render
     "save.html"
     {:pagekey pagekey
      :title title
      :option option
      :body body
      :params contents})))

(defn view-page [pagekey]
  (let [contents (get_contents pagekey)]
    (layout/render
     "view.html"
     (if-not (= contents error_page_not_found)
       (merge {:pagekey pagekey} (get_contents pagekey))
       (merge {:pagekey pagekey} (get_contents "page_not_found"))))))

(defn history-page []
  (layout/render
   "history.html" {:docs (-> "docs/history.md" io/resource slurp)}))

(defn docs-page []
  (layout/render
   "docs.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page []
  (layout/render "about.html"))

(defn login-page []
  (layout/render "login.html"))

(def user {:id "dekaf" :pass "test"})

(defn login! [username password {session :session}]
  (when (= password (user :pass))
    (assoc :session (assoc session :identity username))))

(defroutes home-routes
  (GET "/" [] (view-page "front_page"))
  (GET "/login" [] (login-page))
  (POST "/login" [username password :as req] (login! username password req))
  (GET "/edit" [pagekey title body] (edit-page pagekey))
  (POST "/save" [pagekey title option body] (save-page pagekey title option body))
  (GET "/view" [pagekey] (view-page pagekey))
  (GET "/history" [] (history-page))
  (GET "/docs" [] (docs-page))
  (GET "/about" [] (about-page)))
