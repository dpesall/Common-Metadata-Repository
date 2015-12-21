(ns cmr.common.test.api.web-server
  "This tests capabilities of the web server component."
  (:require [clojure.test :refer :all]
            [cmr.common.api.web-server :as s]
            [cmr.common.lifecycle :as l]
            [clj-http.client :as h]
            [clojure.string :as str]
            [cmr.common.util :as u]))

(def PORT 3123)

(def long-body
  "A body long enough that it should be compressed"
  (str/join (repeat (inc s/MIN_GZIP_SIZE) "0")))

(def short-body
  "A body short enough that it shouldn't be compressed"
  (str/join (repeat (dec s/MIN_GZIP_SIZE) "0")))

(defn routes-fn
  "The routes function to use with the web server. Returns a response long enough that it should be
  compressed."
  [system]
  (fn [request]
    (if (= (:uri request) "/short")
      {:status 200
       :headers {"Content-Type" "application/xml; charset=utf-8"}
       :body short-body}
      {:status 200
       :headers {"Content-Type" "application/xml; charset=utf-8"}
       :body long-body})))

(defn get-uri
  [path accept-encoding]
  (h/with-middleware
    ;; We remove all middleware so we can get the raw input stream body back
    [clj-http.client/wrap-url
     clj-http.client/wrap-method]
    (h/get (str "http://localhost:" PORT path)
           {:headers {"Accept-encoding" accept-encoding}})))

(defn assert-compressed-response
  [response expected-body]
  (is (= "gzip" (get-in response [:headers "content-encoding"])))
  (is (= expected-body
         (-> response :body java.util.zip.GZIPInputStream. slurp))))

(defn assert-not-compressed-response
  [response expected-body]
  (is (not= "gzip" (get-in response [:headers "content-encoding"])))
  (is (= expected-body (-> response :body slurp))))

(deftest test-gzip-compression
  (let [server (l/start (s/create-web-server PORT routes-fn true false) nil)]
    (try
      (testing "A large body is compressed"
        (assert-compressed-response (get-uri "/long" "gzip") long-body))
      (testing "A large body is compressed with gzip, deflate"
        (assert-compressed-response (get-uri "/long" "gzip, deflate") long-body))
      (testing "A short body is not compressed"
        (assert-not-compressed-response (get-uri "/short" "gzip") short-body))
      (testing "A large body is not compressed without accept encoding header"
        (assert-not-compressed-response (get-uri "/long" nil) long-body))
      (finally
        (l/stop server nil)))))

