(ns treeoflife.routes.auth
  (:require [treeoflife.layout :as layout :refer [error-page]]
            [compojure.core :refer [defroutes GET POST]]
            [markdown.core :refer [md-to-html-string]]
            [ring.util.http-response :as response]
            [treeoflife.page :refer [get_contents view-page parse-page
                                    login logout]]
            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth :refer [authenticated?]]

	    [ring.util.response :refer [response]]
	    [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))


(defn set-user! [id passwd {session :session}]
  (let [new_session (login id passwd session)]
    (-> (if-not (= new_session nil)
        (view-page "message" new_session {:message "Login Complete"})
        (view-page "message" new_session {:message "Login Failed"}))
        (assoc :session new_session)
        )))

(defn remove-user! [{session :session}]
  (let [new_session (logout session)]
    (-> (view-page "message" new_session {:message "Logout Complete"})
        (assoc :session new_session)
        )))

(defn clear-session! [session]
  (-> (view-page "message" session {:message "Clear-Session"})
      (dissoc :session)
      (assoc :headers {"Content-Type" "text/plain"})))

(defn check-session [{session :session}]
  (-> (view-page "message" session
    {:message
      (str
        "current id : " (session :user)
        " / level " (session :level)
        " / identity " (session :identity)
        " / test " (authenticated? session))})))

;  (GET "/login" [] (login-page))
;  (POST "/login" [username password :as req] (login! username password req))
(defroutes auth-routes
  (GET "/login" req (view-page "login" req))
  (POST "/login" [id passwd :as req] (set-user! id passwd req))
  (GET "/login2" [id passwd :as req] (set-user! id passwd req))
  (GET "/clear" req (clear-session! req))
  (GET "/logout" req (remove-user! req))
  (GET "/check" req (check-session req))
)
