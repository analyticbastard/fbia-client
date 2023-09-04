(ns fbia-client.core
  (:require [clojure.core.async :refer [chan pipeline]]
            [clojure.data.json :as json]
            [fbia-client.util
             :refer
             [delete-request
              get-request
              graph-url
              post-request
              api-version
              *xf-standard*]]))

(defn list-articles
  "Retrieve a list of instant articles for a page"
  [page-id params]
  (let [res (chan 1)]
    (pipeline 1 res
              *xf-standard*
              (get-request (graph-url api-version
                                      (str "/" page-id "/instant_articles")
                                      params)))
    res))

(defn lookup-article
  "Retrieve a specific instant article by canonical URL"
  [params]
  (let [res (chan 1)]
    (pipeline 1 res
              *xf-standard*
              (get-request (graph-url api-version ""
                                       params)))
    res))

(defn get-article
  "Retrieve a specific instant article by instant article id"
  [id params]
  (let [res (chan 1)]
    (pipeline 1 res
              *xf-standard*
              (get-request (graph-url api-version (str "/" id)
                                      params)))
    res))

(defn create-article [page-id params]
  (let [res (chan 1)]
    (pipeline 1 res
              *xf-standard*
              (post-request (graph-url api-version (str "/" page-id "/instant_articles") {}) params))
    res))

(defn import-status [import-status-id params]
  (let [res (chan 1)]
    (pipeline 1 res
              *xf-standard*
              (get-request (graph-url api-version (str "/" import-status-id) params)))
    res))

(defn delete-article [article-id params]
  (let [res (chan 1)]
    (pipeline 1 res
              *xf-standard*
              (delete-request (graph-url api-version (str "/" article-id) params)))
    res))

(defn- get-multi-req [fields id]
  {:method "GET"
   :relative_url (str "?id=" id "&fields=" fields)})

(defn- del-multi-req [fields id]
  {:method "DELETE"
   :relative_url (str "/" id)})

(defn lookup-article-multi
  "Retrieve a specific instant article by canonical URL"
  [{:keys [fields ids] :as params}]
  (let [res (chan 1)
        batch (json/write-str (map (partial get-multi-req fields) ids))]
    (pipeline 1 res
              *xf-standard*
              (post-request (graph-url api-version "" {}) (-> params
                                                      (assoc :batch batch)
                                                      (dissoc :ids))))
    res))

(defn delete-article-multi
  "Retrieve a specific instant article by canonical URL"
  [{:keys [fields ids] :as params}]
  (let [res (chan 1)
        batch (json/write-str (map (partial del-multi-req fields) ids))]
    (pipeline 1 res
              *xf-standard*
              (post-request
                (graph-url api-version "" {}) (-> params
                                          (assoc :batch batch)
                                          (dissoc :ids))))
    res))

(defn verify-key
  "Checks api keys. params is a map {:input_token \"X\" :access_token \"Y\"} where X might be the same as Y"
  [params]
  (let [res (chan 1)]
    (pipeline 1 res
              *xf-standard*
              (get-request (graph-url api-version
                                      "/debug_token"
                                      params)))
    res))
