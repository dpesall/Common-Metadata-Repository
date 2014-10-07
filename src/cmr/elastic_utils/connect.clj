(ns cmr.elastic-utils.connect
  "Provide functions to invoke elasticsearch"
  (:require [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.admin :as admin]
            [cmr.common.log :as log :refer (debug info warn error)]
            [cmr.common.services.errors :as errors]
            [clj-http.conn-mgr :as conn-mgr]
            [cmr.common.api.web-server :as web-server]))

(defn- connect-with-config
  "Connects to ES with the given config"
  [config]
  (let [{:keys [host port]} config
        http-options {:conn-mgr (conn-mgr/make-reusable-conn-manager
                                  {;; Maximum number of threads that will be used for connecting.
                                   ;; Very important that this matches the maximum number of threads that will be running
                                   :threads web-server/MAX_THREADS
                                   ;; Maximum number of simultaneous connections per host
                                   :default-per-route 10})
                      :socket-timeout 10000
                      :conn-timeout 10000}]
    (info (format "Connecting to single ES on %s %d" host port))
    (esr/connect (str "http://" host ":" port) http-options)))

(defn try-connect
  [config]
  (try
    (connect-with-config config)
    (catch Exception e
      (errors/internal-error!
        (format "Unable to connect to elasticsearch at: %s. with %s" config e)))))

(defn- get-elastic-health
  "Returns the elastic health by calling elasticsearch cluster health api"
  [conn]
  (try
    (admin/cluster-health conn :wait_for_status "yellow" :timeout "30s")
    (catch Exception e
      {:status "Unaccessible"
       :problem (format "Unable to get elasticsearch cluster health, caught exception: %s"
                        (.getMessage e))})))

(defn health
  "Returns the health state of elasticsearch."
  [context elastic-key-in-context]
  (let [conn (get-in context [:system elastic-key-in-context :conn])
        health-detail (get-elastic-health conn)
        status (:status health-detail)]
    (if (some #{status} ["green" "yellow"])
      {:ok? true}
      {:ok? false
       :problem health-detail})))
