(ns treeoflife.routes.home
  (:require [treeoflife.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [markdown.core :refer [md-to-html-string]]
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
    (let [raw (edn/read-string (slurp (str "pages/" pagekey)))]
      (assoc raw :option (edn/read-string (:option raw))))
    (catch Exception e nil)))

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

(defn parse-page [page_contents & [params]]
  (let [page_option (:option page_contents)
        contents (case (:document page_option)
                   :clojure (eval (read-string (:body page_contents)))
                   :html (layout/render-string (rev_escape_html (:body page_contents)) params)
                   :latex (:body page_contents)
                   :markdown (md-to-html-string (:body page_contents))
                   :file (:body  page_contents)
                   page_contents)
        header-page (:header page_option)
        footer-page (:footer page_option)
        header (if-not (= header-page nil) (:body (parse-page (get_contents header-page))))
        footer (if-not (= footer-page nil) (:body (parse-page (get_contents footer-page))))]
    (if-not (= contents nil)
      (merge (assoc page_contents :body (str contents))
             {:header header, :footer footer}
             )
      page_contents)))

                                        ; type에 따라 분기시킨다.
                                        ; markdown으로 가느냐, clojure로 가느냐, latex로 가느냐, native web으로 가느냐
(defn view-page [pagekey]
  (let [page_contents (get_contents pagekey)
        page_option (:option page_contents)
        page_template (case (:document page_option)
                        :clojure "view_clojure.html"
                        :latex "view_latex.html"
                        :file "view_file.html"
                        "view.html")
        contents (parse-page page_contents)]
    (layout/render
     page_template
     (if-not (= contents nil)
       (merge {:pagekey pagekey} contents)
       (merge {:pagekey pagekey} (parse-page (get_contents "page_not_found")))))))

(defn edit-page [pagekey]
  (let [contents (get_contents pagekey)]
    (layout/render
     "view.html"
     (merge
      {:pagekey pagekey}
      (parse-page (get_contents "edit-page")
                  (merge {:pagekey pagekey} (if (= contents nil)
                                              {:option {:document :markdown, :header 'nav-bar, :footer nil}}
                                              contents)))
      ))))

(defn save-page [pagekey title option body]
  (let [contents (save_contents pagekey title option body)]
    (layout/render
     "save.html"
     {:pagekey pagekey
      :title title
      :option option
      :body body
      :params contents})))

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
  (GET "/docs" [] (docs-page))
  (GET "/about" [] (about-page)))
