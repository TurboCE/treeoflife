(ns treeoflife.page
  (:require [treeoflife.layout :as layout]
            [selmer.parser :as parser]
            [markdown.core :refer [md-to-html-string]]
            [ring.util.http-response :as response]

            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]))


(def error_page_not_found
  {:pagekey "page_not_found"
   :title "Page Not Found!"
   :body "Page Not Found! Body"})

(def error_page_save_failed
  {:pagekey "page_save_error"
   :title "Page Save Error!"
   :body "Page Save Error! Body"})

(defn get_contents
  "Read contents from file."
  [pagekey]
  (try
    (let [raw (edn/read-string (slurp (str "pages/" pagekey)))]
      (assoc raw :option (edn/read-string (:option raw))))
    (catch Exception e nil)))

(defn save_contents
  "Save contents to file."
  [pagekey title option body]
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

(defn parse-page
"Make contents to web format."
  [page_contents & [params]]
  (let [page_option (:option page_contents)
        contents (case (:document page_option)
                    :html (layout/render-string (rev_escape_html (:body page_contents)) params)
                    :latex (:body page_contents) ;TODO 
                    :clojure (parser/render "{{body|clojure}}" page_contents)
                    :markdown (parser/render "{{body|markdown}}" page_contents)
                    :file (:body  page_contents)
                   page_contents)
        header-page (:header page_option)
        footer-page (:footer page_option)
        header (if-not (= header-page nil) (:body (parse-page (get_contents header-page) params)))
        footer (if-not (= footer-page nil) (:body (parse-page (get_contents footer-page) params)))]
    (if-not (= contents nil)
      (merge (assoc page_contents :body (str contents))
             {:header header, :footer footer}
             )
      page_contents)))

;Session Auth system
(def user-db {:id "dekaf" :pass "test"})

(defn get_auth_info [session]
  (if-not (= session nil)
  {:user (session :user), :level (session :level), :identity (session :identity)}
  {:user nil, :level 999}))

(defn login [id passwd session]
  (if (= passwd (user-db :pass))
          (assoc session :user id :level 0 :identity "foo")
          nil
  ))

(defn logout [{session :session}]
  (dissoc session :user :level :identity))

; type에 따라 분기시킨다.
; markdown으로 가느냐, clojure로 가느냐, latex로 가느냐, native web으로 가느냐
(defn view-page
 "Decorate contents with template"
 [pagekey session & [params]]
 (let [page_contents (get_contents pagekey )
       page_option (:option page_contents)
       page_template (case (:document page_option)
                       :markdown "view_markdown.html"
                       :latex "view_latex.html"
                       :file "view_file.html"
                       "view.html")
       contents (parse-page page_contents (merge {:pagekey pagekey} (get_auth_info session) params))]
   (layout/render
    page_template
    (merge (if-not (= contents nil)
      (merge {:pagekey pagekey} contents)
      (merge {:pagekey pagekey} (parse-page (get_contents "page_not_found"))))
      {:system ""}))))
