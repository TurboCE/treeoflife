(ns treeoflife.filters
  (:require [markdown.core :refer [md-to-html-string]]
            [clojure.string :as str]))

(defn wiki-links
  [text state]
  [(str/replace text #"\[\[([\w -]+)\]\]"
              (fn [i]
                (let [link-text (i 1)
                      link-ref (.toLowerCase (str/replace link-text " " "-"))]
                  (str "<a href=\"/view/" link-ref "\">" link-text "</a>"))))
   state])

(defn web-links
 [text state]
 [(str/replace text #"\[(https?://[\w\./]+)\]"
             (fn [i]
               (let [link-text (i 1)]
                 (str "<a href=\"" link-text "\">" link-text "</a>"))))
  state])


(defn capitalize [text state]
  [(.toUpperCase text) state])

(defn wiki-markdown
  [content]
  (md-to-html-string content :custom-transformers [wiki-links web-links]))

(defn wiki-clojure
  [content]
  (try
    (eval (read-string content))
    (catch Exception e
      (str "Error! " e))))
