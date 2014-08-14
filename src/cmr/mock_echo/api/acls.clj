(ns cmr.mock-echo.api.acls
  "Defines the HTTP URL routes for acls"
  (:require [compojure.core :refer :all]
            [cheshire.core :as json]
            [cmr.mock-echo.data.acl-db :as acl-db]
            [cmr.mock-echo.api.api-helpers :as ah]
            [cmr.common.services.errors :as svc-errors]))

(defn create-acl
  [context body]
  (let [acl (json/decode body true)]
    (acl-db/create-acl context (json/decode body true))))

(defn get-acls
  [context params]
  (when-not (= "CATALOG_ITEM" (:object_identity_type params))
    (svc-errors/throw-service-error
      :bad-request
      "Mock ECHO currently only supports retrieving ACLS with object identity type CATALOG_ITEM"))
  (when (:provider_id params)
    (svc-errors/throw-service-error
      :bad-request
      "Mock ECHO does not currently support retrieving acls by provider."))
  (when-not (= "false" (:reference params))
    (svc-errors/throw-service-error
      :bad-request
      "Mock ECHO does not currently support retrieving acls as references"))
  (acl-db/get-acls context))


(defn build-routes [system]
  (routes
    (context "/acls" []
      ;; Creates one ACL
      (POST "/" {params :params context :request-context body :body}
        (create-acl context (slurp body))
        {:status 201})

      ;; Mimics echo-rest retrieval of acls
      (GET "/" {context :request-context params :params}
        (ah/status-ok (get-acls context params))))))

