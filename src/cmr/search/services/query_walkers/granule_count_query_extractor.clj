(ns cmr.search.services.query-walkers.granule-count-query-extractor
  "Extracts a query to find granule counts for each collection result in a collection query."
  (:require [cmr.common.services.errors :as errors]
            [cmr.search.services.query-walkers.condition-extractor :as condition-extractor]
            [cmr.search.models.query :as q])
  (:import [cmr.search.models.query
            Query
            ConditionGroup
            SpatialCondition
            TemporalCondition]))

(defmulti query-results->concept-ids
  "Returns the concept ids from the query results. It is expected that results handlers will
  implement this multi-method"
  (fn [results]
    (:result-format results)))

(defn- valid-parent-condition?
  "The granule count query extractor can extract spatial and temporal conditions from a collection
  query. It only supports extracting spatial and temporal conditions that aren't OR'd or negated
  as well. Returns true if the parent condition is allowed"
  [condition]
  (let [cond-type (type condition)]
    (or (= cond-type Query)
        (and (= cond-type ConditionGroup)
             (= :and (:operation condition))))))

(defn- validate-path-to-condition
  "Validates that the path to the condition we are extracting from the query doesn't contain
  any of the conditions that change it's meaning. This is mainly that the condition is AND'd from
  the top level and not negated. Throws an exception if one is encountered."
  [query condition-path]
  (loop [conditions condition-path]
    (when-let [condition (first conditions)]
      (when-not (valid-parent-condition? condition)
        (errors/internal-error!
          (str "Granule query extractor found spatial or temporal condition within an unexpected "
               "condition. Query:" (pr-str query))))
      (recur (rest conditions)))))

(defn- extract-spatial-and-temporal-conditions
  "Returns a list of the spatial and temporal conditions in the query. Throws an exception if a
  parent of the spatial or temporal condition isn't expected."
  [query]
  (condition-extractor/extract-conditions
    query
    ;; Matches spatial or temporal conditions
    (fn [condition-path condition]
      (let [cond-type (type condition)]
        (when (or (= cond-type SpatialCondition)
                  (= cond-type TemporalCondition))
          ;; Validate that the parent of the spatial or temporal condition is acceptable
          (validate-path-to-condition query condition-path)
          true)))))

(defn extract-granule-count-query
  "Extracts a query to find the number of granules per collection in the results from a collection query
  coll-query - The collection query
  results - the results of the collection query"
  [coll-query results]
  ;; TODO this needs to enforce granule ACLs on the query
  (let [collection-ids (query-results->concept-ids results)
        spatial-temp-conds (extract-spatial-and-temporal-conditions coll-query)
        condition (if (seq collection-ids)
                    (q/and-conds (cons (q/string-conditions :collection-concept-id collection-ids true)
                                       spatial-temp-conds))
                    ;; The results were empty so the granule count query doesn't need to find anything.
                    (q/->MatchNoneCondition))]
    (q/query {:concept-type :granule
              :condition condition
              ;; We don't need any results
              :page-size 0
              :aggregations {:granule-counts-by-collection-id
                             {:terms {:field :collection-concept-id
                                      :size (count collection-ids)}}}})))

(defn search-results->granule-counts
  "Extracts the granule counts per collection id in a map from the search results from ElasticSearch"
  [results]
  (let [aggs (get-in results [:aggregations :granule-counts-by-collection-id :buckets])]
    (into {} (for [{collection-id :key num-granules :doc_count} aggs]
               [collection-id num-granules]))))

