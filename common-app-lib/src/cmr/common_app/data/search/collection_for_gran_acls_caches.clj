(ns cmr.common-app.data.search.collection-for-gran-acls-caches
  "Defines common functions and defs a cache for search collections.
  This namespace defines two separate caches that are tightly coupled and will be created and updated/refreshed at the same time:
  - coll-by-concept-id-cache = collection-for-gran-acls-by-concept-id-cache"
  (:require
  [cmr.redis-utils.redis-hash-cache :as red-hash-cache]
  [cmr.common.hash-cache :as hash-cache]
  [cmr.common.jobs :refer [defjob]]
  [cmr.common.log :as log :refer (debug info warn error)]
  [clojure.walk :as walk]
  [cmr.common-app.services.search.query-execution :as qe]
  [cmr.common-app.services.search.query-model :as q-mod]
  [cmr.common.date-time-parser :as time-parser]
  [cmr.common-app.services.search.acl-results-handler-helper :as acl-rhh]
  [cmr.common.util :as util]))

(def coll-by-concept-id-cache-key
  "Identifies the key used when the cache is stored in the system."
  :collections-for-gran-acls-by-concept-id)

(def job-refresh-rate
  "This is the frequency that the cron job will run. It should be less than cache-ttl"
  ;; 15 minutes
  (* 60 15))

(def cache-ttl
  "This is when Redis should expire the data, this value should never be hit if
   the cron job is working correctly, however in some modes (such as local dev
   with no database) it may come into play."
   ;; 30 minutes
  (* 60 30))

(defn create-coll-by-concept-id-cache-client
  "Creates connection to collection-for-gran-acls coll-by-concept-id-cache.
  Field/Value Structure is as follows:
  concept-id -> {collection info}"
  []
  (red-hash-cache/create-redis-hash-cache {:keys-to-track [coll-by-concept-id-cache-key]
                                           :ttl           cache-ttl}))

(defn clj-times->time-strs
  "Take a map and convert any date objects into strings so the map can be cached.
  This can be reversed with time-strs->clj-times."
  [data]
  (walk/postwalk
    #(if (true? (instance? org.joda.time.DateTime %))
       (time-parser/clj-time->date-time-str %)
       %)
    data))

(defn time-strs->clj-times
  "Take a map which has string dates and convert them to DateTime objects.
  This can be reversed with clj-times->time-strs."
  [data]
  (walk/postwalk
    #(if-let [valid-date (time-parser/try-parse-datetime %)] valid-date %)
    data))

(defn- execute-coll-for-gran-acls-query
  [context query-condition page-size]
  ;; when creating a result processor, realize all the lazy (delay) values to
  ;; actual values so that the resulting object can be cached in redis or some
  ;; other text based caching system and not clojure memory
  (let [result-processor (fn [_ _ elastic-item]
                           (assoc (util/delazy-all (acl-rhh/parse-elastic-item :collection elastic-item)) :concept-id (:_id elastic-item)))
        query (q-mod/query {:concept-type :collection
                            :condition       query-condition
                            :skip-acls?      true
                            :page-size       page-size
                            :result-format   :query-specified
                            :result-fields   (cons :concept-id acl-rhh/collection-elastic-fields)
                            :result-features {:query-specified {:result-processor result-processor}}})]
    (:items (qe/execute-query context query))))

(defn- fetch-collections
  ([context]
   "Fetches all collections from elastic. If no collections are found, will return nil."
   (execute-coll-for-gran-acls-query context q-mod/match-all :unlimited))
  ([context collection-concept-id]
   "Fetches one collection from elastic search by concept id. If no collection is found, will return nil."
   (let [query-condition (q-mod/string-condition :concept-id collection-concept-id true false)
         colls-found (execute-coll-for-gran-acls-query context query-condition 1)]
     (first colls-found))))

(defn refresh-entire-cache
  "Refreshes the collections-for-gran-acls coll-by-concept-id and coll-by-provider-id-and-entry-title caches.
  This should be called from a background job on a timer to keep the cache fresh.
  This will throw an exception if there is a problem fetching collections.
  The caller is responsible for catching and logging the exception."
  [context]
  (info "Refreshing entire Collections-for-gran-acls caches")
  (let [coll-by-concept-id-cache (hash-cache/context->cache context coll-by-concept-id-cache-key)
        collections (fetch-collections context)]
    (doseq [coll collections]
      (hash-cache/set-value coll-by-concept-id-cache
                            coll-by-concept-id-cache-key
                            (:concept-id coll)
                            (clj-times->time-strs coll)))
    (info (str "Collections-for-gran-acls caches refresh complete."
         " coll-by-concept-id-cache Cache Size: " (hash-cache/cache-size coll-by-concept-id-cache coll-by-concept-id-cache-key) " bytes"))))

(defn set-caches
  "Updates collections-for-gran-acl caches for one given collection by concept id and  returns found collection or nil"
  ([context collection-concept-id]
   (let [collection-found (fetch-collections context collection-concept-id)
         coll-by-concept-id-cache (hash-cache/context->cache context coll-by-concept-id-cache-key)]
     (when-not (nil? collection-found)
       (hash-cache/set-value coll-by-concept-id-cache
                             coll-by-concept-id-cache-key
                             (:concept-id collection-found)
                             (clj-times->time-strs collection-found)))
     collection-found)))

(defjob RefreshCollectionsCacheForGranuleAclsJob
        [ctx system]
        (refresh-entire-cache {:system system}))

(defn refresh-collections-cache-for-granule-acls-job
  [job-key]
  {:job-type RefreshCollectionsCacheForGranuleAclsJob
   :job-key job-key
   :interval job-refresh-rate})
