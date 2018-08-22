(ns treeoflife.routes.auth
  (:require [treeoflife.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [markdown.core :refer [md-to-html-string]]
            [ring.util.http-response :as response]

            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth :refer [authenticated?]]

	    [ring.util.response :refer [response]]
	    [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def user-db {:id "dekaf" :pass "test"})

(defn set-user! [id passwd {session :session}]
  (-> (if (= passwd (user-db :pass))
        (->
         (response (str "Login Ok. User set to: " id))
         (assoc :session (assoc session :user id :level 0 :identity "foo")))
        (response (str "Login Failed")))
      (assoc :headers {"Content-Type" "text/plain"})
      ))

(defn remove-user! [{session :session}]
  (-> (response "User removed")
      (assoc :session (dissoc session :user :level :identity))
      (assoc :headers {"Content-Type" "text/plain"})))

(defn clear-session! []
  (-> (response "Session cleared")
      (dissoc :session)
      (assoc :headers {"Content-Type" "text/plain"})))

(defn check-session [{session :session}]
  (-> (response (str "current id : " (session :user) " / level " (session :level) " / identity " (session :identity) " / test " (authenticated? session)))
      (assoc :headers {"Content-Type" "text/plain"})))

;  (GET "/login" [] (login-page))
;  (POST "/login" [username password :as req] (login! username password req))
(defroutes auth-routes
  (GET "/login" [id passwd :as req] (set-user! id passwd req))
  (GET "/clear" req (restrict clear-session! {:handler authenticated?}))
  (GET "/logout" req (remove-user! req))
  (GET "/check" req (check-session req))
)
