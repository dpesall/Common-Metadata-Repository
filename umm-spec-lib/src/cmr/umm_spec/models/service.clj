;; WARNING: This file was generated from umm-s-json-schema.json. Do not manually modify.
(ns cmr.umm-spec.models.service
   "Defines UMM-S clojure records."
 (:require [cmr.common.dev.record-pretty-printer :as record-pretty-printer]))

(defrecord UMM-S
  [
   ;; Defines a responsibility by role which is either an organization such as a data center, or
   ;; institution responsible for distributing, archiving, or processing the data, etc., or a person
   ;; such as an Investigator, a Technical Contact, Metadata Author, etc.
   Responsibilities

   ;; This includes any metadata related dates.
   MetadataDates

   ;; Describes the language used in the preparation, storage, and description of the service It is
   ;; the language of the information object, not the language used to describe or interact with the
   ;; metadata record. It does not refer to the language of the metadata.
   ServiceLanguage

   ;; This element is used to identify the keywords from the EN ISO 19115-1:2014 Geographic
   ;; Information – Metadata – Part 1: Fundamentals (http://www.isotc211.org/) Topic Category Code
   ;; List. It is a high-level thematic classification to assist in the grouping and search of
   ;; available services.
   ISOTopicCategories

   ;; Abstract provides a brief description of the data or service the metadata represents.
   Abstract

   ;; The language used in the metadata record.
   MetadataLanguage

   ;; This element contains suggested usage or purpose for the data or service.
   Purpose

   ;; This element describes key bibliographic citations pertaining to the data.
   PublicationReferences

   ;; This element permits the user to properly cite the service and specifies how the data should
   ;; be cited in professional scientific literature. This element provides a citation for the item
   ;; itself, and is not designed for listing bibliographic references of scientific research
   ;; articles arising from search results. A list of references related to the research results
   ;; should be in the Publication Reference element. A DOI that specifically identifies the service
   ;; is listed here.
   ServiceCitation

   ;; This element describes any data/service related URLs that include project home pages,
   ;; services, related data archives/servers, metadata extensions, direct links to online software
   ;; packages, web mapping services, links to images, or other data.
   RelatedUrls

   ;; This element with the description field allows the author to provide information about any
   ;; constraints for accessing the service. This includes any special restrictions, legal
   ;; prerequisites, limitations and/or warnings on obtaining the service. Some words that may be
   ;; used in this element's value include: Public, In-house, Limited, None. The value field is used
   ;; for special ACL rules (Access Control Lists
   ;; (http://en.wikipedia.org/wiki/Access_control_list)). For example it can be used to hide
   ;; metadata when it isn't ready for public consumption.
   AccessConstraints

   ;; This element enables specification of service keywords.
   ServiceKeywords

   ;; This entity stores the data’s distinctive attributes (i.e. attributes used to describe the
   ;; unique characteristics of the service which extend beyond those defined).
   AdditionalAttributes

   ;; The entry ID of the service described by the metadata.
   EntryId

   ;; This element enables specification of Earth science keywords.
   ScienceKeywords

   ;; This element permits the author to provide the following information about an item described
   ;; in the metadata: 1) Quality of the item; and 2) Any quality assurance procedures followed in
   ;; producing the item. Examples of appropriate element information include: A) succinct
   ;; description; B) indicators of item quality or quality flags - both validated or invalidated;
   ;; C) recognized or potential problems with quality; D) established quality control mechanisms;
   ;; and E) established quantitative quality measurements.
   Quality

   ;; The title of the service described by the metadata.
   EntryTitle

   ;; This entity stores the data’s distinctive attributes (i.e. attributes used to describe the
   ;; unique characteristics of the service which extend beyond those defined).
   Distributions

   ;; The Use Constraints element is designed to protect privacy and/or intellectual property by
   ;; allowing the author to specify how the item may or may not be used after access is granted.
   ;; This includes any special restrictions, legal prerequisites, terms and conditions, and/or
   ;; limitations on using the item. Providers may request acknowledgement of the item from users
   ;; and claim no responsibility for quality and completeness. Note: Use Constraints describe how
   ;; the item may be used once access has been granted; and is distinct from Access Constraints,
   ;; which refers to any constraints in accessing the item.
   UseConstraints

   ;; This element allows authors to provide words or phrases to further describe the data.
   AncillaryKeywords

   ;; This element describes the relevant platforms used to acquire the data related to the service.
   ;; Platform types are controlled and include Spacecraft, Aircraft, Vessel, Buoy, Platform,
   ;; Station, Network, Human, etc.
   Platforms

   ;; The project element describes the name of the scientific program, field campaign, or project
   ;; from which the data were collected. This element is intended for the non-space assets such as
   ;; aircraft, ground systems, balloons, sondes, ships, etc. associated with campaigns. This
   ;; element may also cover a long term project that continuously creates new data sets — like
   ;; MEaSUREs from ISCCP and NVAP or CMARES from MISR. Project also includes the Campaign
   ;; sub-element to support multiple campaigns under the same project.
   Projects

   ;; This element is used to identify other services, collections, visualizations, granules, and
   ;; other metadata types and resources that are associated with or dependent on the data described
   ;; by the metadata. This element is also used to identify a parent metadata record if it exists.
   ;; This usage should be reserved for instances where a group of metadata records are subsets that
   ;; can be better represented by one parent metadata record, which describes the entire set. In
   ;; some instances, a child may point to more than one parent. The EntryId is the same as the
   ;; element described elsewhere in this document where it contains and ID, and Version.
   MetadataAssociations
  ])
(record-pretty-printer/enable-record-pretty-printing UMM-S)

;; This element describes the relevant platforms used to acquire the data. Platform types are
;; controlled and include Spacecraft, Aircraft, Vessel, Buoy, Platform, Station, Network, Human,
;; etc.
(defrecord PlatformType
  [
   ;; The most relevant platform type.
   Type

   ShortName

   LongName

   ;; The characteristics of platform specific attributes. The characteristic names must be unique
   ;; on this platform; however the names do not have to be unique across platforms.
   Characteristics

   Instruments
  ])
(record-pretty-printer/enable-record-pretty-printing PlatformType)

;; This element enables specification of Earth science keywords.
(defrecord ServiceKeywordType
  [
   Category

   Topic

   Term

   ServiceSpecificName
  ])
(record-pretty-printer/enable-record-pretty-printing ServiceKeywordType)