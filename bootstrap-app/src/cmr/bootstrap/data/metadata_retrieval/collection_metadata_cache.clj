(ns cmr.bootstrap.data.metadata-retrieval.collection-metadata-cache
  "Defines a cache for catalog item metadata. It currently only stores collections.
  The metadata cache contains data like the following:

  concept-id -> revision-format-map
                 * concept-id
                 * revision-id
                 * native-format - A key or map of format and version identifying the native format
                 * various format keys each mapped to compressed metadata."
  (:require
   [clj-time.core :as t]
   [clojure.set :as cset]
   [cmr.common-app.data.metadata-retrieval.collection-metadata-cache :as cmn-coll-metadata-cache]
   [cmr.common-app.data.metadata-retrieval.revision-format-map :as crfm]
   [cmr.bootstrap.data.metadata-retrieval.metadata-transformer :as metadata-transformer]
   [cmr.common.config :refer [defconfig]]
   [cmr.common.hash-cache :as hash-cache]
   [cmr.common.jobs :refer [defjob]]
   [cmr.common.log :as log :refer [info]]
   [cmr.common.util :as c-util]
   [cmr.metadata-db.services.concept-service :as metadata-db]
   [cmr.umm-spec.versioning :as umm-version]))

(defconfig non-cached-collection-metadata-formats
  "Defines a set of collection metadata formats that will not be cached in memory"
  {:default #{}
   :type :edn})

(def all-formats
  "All the possible collection metadata formats that could be cached."
  #{:echo10
    :iso19115
    :dif
    :dif10
    ;; Note that when upgrading umm version we should also cache the previous version of UMM.
    {:format :umm-json
     :version umm-version/current-collection-version}})

(defn cached-formats
  "This is a set of formats that are cached."
  []
  (cset/difference all-formats (non-cached-collection-metadata-formats)))

(defn- concept-tuples->cache-map
  "Takes a set of concept tuples fetches the concepts from metadata db, converts them to revision
   format maps, and stores them into a cache map"
  [context concept-tuples]
  (let [mdb-context (cmn-coll-metadata-cache/context->metadata-db-context context)
        concepts (doall (metadata-db/get-concepts mdb-context concept-tuples true))
        concepts (cmn-coll-metadata-cache/concepts-without-xml-processing-inst concepts)
        rfms (c-util/fast-map #(crfm/compress
                                (crfm/concept->revision-format-map context
                                                                   %
                                                                   (cached-formats)
                                                                   metadata-transformer/transform-to-multiple-formats
                                                                   true))
                              concepts)]
    (reduce #(assoc %1 (:concept-id %2) %2) {} rfms)))

(defn update-cache
  "Updates the collection metadata cache by querying elastic search for updates since the 
  last time the cache was updated."
  [context]
  (info "Updating collection metadata cache")
  (let [incremental-since-refresh-date (str (t/now))
        concepts-tuples (cmn-coll-metadata-cache/fetch-updated-collections-from-elastic context)
        new-cache-value (reduce #(merge %1 (concept-tuples->cache-map context %2))
                                {}
                                (partition-all 1000 concepts-tuples))
        cache (hash-cache/context->cache context cmn-coll-metadata-cache/cache-key)]
    (hash-cache/set-value cache 
                          cmn-coll-metadata-cache/cache-key 
                          cmn-coll-metadata-cache/incremental-since-refresh-date-key 
                          incremental-since-refresh-date)
    (hash-cache/set-values cache 
                           cmn-coll-metadata-cache/cache-key 
                           new-cache-value)
    (info "Metadata cache update complete. Cache Size:" (hash-cache/cache-size cache cmn-coll-metadata-cache/cache-key))))

(defn refresh-cache
  "Refreshes the collection metadata cache"
  [context]
  (info "Refreshing collection metadata cache")
  (let [incremental-since-refresh-date (str (t/now))
        concepts-tuples (cmn-coll-metadata-cache/fetch-collections-from-elastic context)
        new-cache-value (reduce #(merge %1 (concept-tuples->cache-map context %2))
                                {}
                                (partition-all 1000 concepts-tuples))
        cache (hash-cache/context->cache context cmn-coll-metadata-cache/cache-key)]
    (hash-cache/set-value cache 
                          cmn-coll-metadata-cache/cache-key 
                          cmn-coll-metadata-cache/incremental-since-refresh-date-key 
                          incremental-since-refresh-date)
    (hash-cache/set-values cache cmn-coll-metadata-cache/cache-key new-cache-value)
    
    (info "Metadata cache refresh complete. Cache Size:" 
          (hash-cache/cache-size cache cmn-coll-metadata-cache/cache-key))))

(defjob RefreshCollectionsMetadataCache
  [ctx system]
  (refresh-cache {:system system}))

(defn refresh-collections-metadata-cache-job
  [job-key]
  {:job-type RefreshCollectionsMetadataCache
   :job-key job-key
   ;; The time here is UTC.
   :daily-at-hour-and-minute [06 00]})

(defjob UpdateCollectionsMetadataCache
  [ctx system]
  (update-cache {:system system}))

(defn update-collections-metadata-cache-job
  [job-key]
  {:job-type UpdateCollectionsMetadataCache
   :job-key job-key
   :interval (cmn-coll-metadata-cache/update-collection-metadata-cache-interval)})

(comment
  (refresh-cache {:system (get-in user/system [:apps :bootstrap])})
  (cmn-coll-metadata-cache/prettify-cache (get-in user/system [:apps :bootstrap :caches cmn-coll-metadata-cache/cache-key])))
