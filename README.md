# cmr-search-app

Provides a public search API for concepts in the CMR.

## Example CURL requests

### Common GET parameters/headers

#### Parameters

 * page_size - number of results per page - default is 10, max is 2000
 * pretty - return formatted results

#### Headers

  * Accept - specifies the format to return references in. Default is json.
    * `curl -H "Accept: application/xml" -i "http://localhost:3003/collections"`

### Search for Collections

#### Find all collections

    curl "http://localhost:3003/collections"

#### Find collections by concept id

A CMR concept id is in the format `<concept-type-prefix> <unique-number> "-" <provider-id>`

  * `concept-type-prefix` is a single capital letter prefix indicating the concept type. "C" is used for collections
  * `unique-number` is a single number assigned by the CMR during ingest.
  * `provider-id` is the short name for the provider. i.e. "LPDAAC_ECS"

Example: `C123456-LPDAAC_ECS`

    curl "http://localhost:3003/collections?concept_id\[\]=C123456-LPDAAC_ECS"

#### Find collections by echo collection id

  Find a collection matching a echo collection id. Note more than one echo collection id may be supplied.

     curl "http://localhost:3003/collections?echo_collection_id\[\]=C1000000001-CMR_PROV2"


#### Find collections by entry title

One entry title

    curl "http://localhost:3003/collections?entry_title\[\]=DatasetId%204"

a dataset id (alias for entry title)

    curl "http://localhost:3003/collections?dataset_id\[\]=DatasetId%204"

with multiple dataset ids

    curl "http://localhost:3003/collections?entry_title\[\]=DatasetId%204&entry_title\[\]=DatasetId%205"

with a entry title case insensitively

    curl "http://localhost:3003/collections?entry_title\[\]=datasetId%204&options\[entry_title\]\[ignore_case\]=true"

with a entry title pattern

    curl "http://localhost:3003/collections?entry_title\[\]=DatasetId*&options\[entry_title\]\[pattern\]=true"

#### Find collections by entry id

One entry id

    curl "http://localhost:3003/collections?entry_id\[\]=SHORT_V5"

One dif\_entry\_id (alias for entry id)

    curl "http://localhost:3003/collections?dif_entry_id\[\]=SHORT_V5"

#### Find collections by archive center, supports pattern and ignore_case

  Find collections matching 'archive_center' param value
    curl "http://localhost:3003/collections?archive_center\[\]=LARC"
    curl "http://localhost:3003/collections?archive_center=Sedac+AC"

  Find collections matching any of the 'archive_center' param values

     curl "http://localhost:3003/collections?archive_center\[\]=Larc&archive_center\[\]=SEDAC"

#### Find collections with multiple temporal

The temporal datetime has to be in yyyy-MM-ddTHH:mm:ssZ format.

    curl "http://localhost:3003/collections?temporal\[\]=2000-01-01T10:00:00Z,2010-03-10T12:00:00Z,30,60&temporal\[\]=2000-01-01T10:00:00Z,,30&temporal\[\]=2000-01-01T10:00:00Z,2010-03-10T12:00:00Z"

### Find collections by campaign param, supports pattern, ignore_case and option :and. Note: 'campaign' maps to 'project' in UMM

  Find collections matching 'campaign' param value

     curl "http://localhost:3003/collections?campaign\[\]=ESI"

  Find collections matching any of the 'campaign' param values

     curl "http://localhost:3003/collections?campaign\[\]=ESI&campaign\[\]=EVI&campaign\[\]=EPI"

  Find collections that match all of the 'campaign' param values

     curl "http://localhost:3003/collections?campaign\[\]=ESI&campaign\[\]=EVI&campaign\[\]=EPI&options\[campaign\]\[and\]=true"

### Find collections by updated_since param

  Find collections which have revision date starting at or after 'updated_since' param value

     curl "http://localhost:3003/collections?updated_since=2014-05-08T20:06:38.331Z"

### Find collections by processing_level_id param, supports pattern and ignore_case

  Find collections matching 'processing_level_id' param value

     curl "http://localhost:3003/collections?processing_level_id\[\]=1B"

  Find collections matching any of the 'processing_level_id' param values

     curl "http://localhost:3003/collections?processing_level_id\[\]=1B&processing_level_id\[\]=2B"

### Find collections by platform param, supports pattern, ignore_case and option :and

  Find collections matching 'platform' param value

     curl "http://localhost:3003/collections?platform\[\]=1B"

  Find collections matching any of the 'platform' param values

     curl "http://localhost:3003/collections?platform\[\]=1B&platform\[\]=2B"

### Find collections by instrument param, supports pattern, ignore_case and option :and

  Find collections matching 'instrument' param value

     curl "http://localhost:3003/collections?instrument\[\]=1B"

  Find collections matching any of the 'instrument' param values

     curl "http://localhost:3003/collections?instrument\[\]=1B&instrument\[\]=2B"

### Find collections by sensor param, supports pattern, ignore_case and option :and

  Find collections matching 'sensor' param value

     curl "http://localhost:3003/collections?sensor\[\]=1B"

  Find collections matching any of the 'sensor' param values

     curl "http://localhost:3003/collections?sensor\[\]=1B&sensor\[\]=2B"

### Find collections by spatial_keyword param, supports pattern, ignore_case and option :and

  Find collections matching 'spatial_keyword' param value

     curl "http://localhost:3003/collections?spatial_keyword\[\]=DC"

  Find collections matching any of the 'spatial_keyword' param values

     curl "http://localhost:3003/collections?spatial_keyword\[\]=DC&spatial_keyword\[\]=LA"

### Find collections by science_keywords params, supports option :or

  Find collections matching 'science_keywords' param value

     curl "http://localhost:3003/collections?science_keywords\[0\]\[category\]=Cat1"

  Find collections matching multiple 'science_keywords' param values, default is :and

     curl "http://localhost:3003/collections?science_keywords\[0\]\[category\]=Cat1&science_keywords\[0\]\[topic\]=Topic1&science_keywords\[1\]\[category\]=Cat2"

### Find collections by collection_data_type param, supports ignore_case and the following aliases for "NEAR_REAL_TIME": "near_real_time","nrt", "NRT", "near real time","near-real time","near-real-time","near real-time".

  Find collections matching 'collection_data_type' param value

     curl "http://localhost:3003/collections?collection_data_type\[\]=NEAR_REAL_TIME"

  Find collections matching any of the 'collection_data_type' param values

     curl "http://localhost:3003/collections?collection_data_type\[\]=NEAR_REAL_TIME&collection_data_type\[\]=OTHER"

#### Sorting Collection Results

Collection results are sorted by ascending entry title by default. One or more sort keys can be specified using the `sort_key[]` parameter. The order used impacts searching. Fields can be prepended with a `-` to sort in descending order. Ascending order is the default but `+` can be used to explicitly request ascending.

##### Valid Collection Sort Keys

  * entry_title
  * dataset_id - alias for entry_title
  * start_date
  * end_date
  * platform
  * instrument
  * sensor
  * provider

Example of sorting by start_date in descending order: (Most recent data first)

    curl "http://localhost:3003/collections?sort_key\[\]=-start_date

### Search for Granules

#### Find all granules

    curl "http://localhost:3003/granules"

#### Find granules with a granule-ur

    curl "http://localhost:3003/granules?granule_ur\[\]=DummyGranuleUR"

#### Find granules with a producer granule id

    curl "http://localhost:3003/granules?producer_granule_id\[\]=DummyID"

#### Find granules matching either granule ur or producer granule id

    curl "http://localhost:3003/granules?readable_granule_name\[\]=DummyID"

#### Find granules by online_only

    curl "http://localhost:3003/granules?online_only=true"

#### Find granules by downloadable

    curl "http://localhost:3003/granules?downloadable=true"

#### Find granules by additional attribute

Find an attribute attribute with name "PERCENTAGE" of type float with value 25.5

    curl "http://localhost:3003/granules?attribute\[\]=float,PERCENTAGE,25.5"

Find an attribute attribute with name "PERCENTAGE" of type float in range 25.5 - 30.

    curl "http://localhost:3003/granules?attribute\[\]=float,PERCENTAGE,25.5,30"

Find an attribute attribute with name "PERCENTAGE" of type float with min value 25.5.

    curl "http://localhost:3003/granules?attribute\[\]=float,PERCENTAGE,25.5,"

Find an attribute attribute with name "PERCENTAGE" of type float with max value 30.

    curl "http://localhost:3003/granules?attribute\[\]=float,PERCENTAGE,,30"

Find an additional attribute with name "X,Y,Z" with value 7.

    curl "http://localhost:3003/granules?attribute\[\]=float,X\,Y\,Z,7"

Find an additional attribute with name "X\Y\Z" with value 7.

    curl "http://localhost:3003/granules?attribute\[\]=float,X\Y\Z,7"

Multiple attributes can be provided. The default is for granules to match all the attribute parameters. This can be changed by specifying `or` option with `option[attribute][or]=true`.

### Find granules by Spatial

#### Polygon

Polygon points are provided in clockwise order. The last point should match the first point to close the polygon. The values are listed comma separated in longitude latitude order, i.e. lon1,lat1,lon2,lat2,...

    curl "http://localhost:3003/granules?polygon=10,10,10,20,30,20,30,10,10,10"

### Find granules by orbit number

  Find granules with an orbit number of 10

    curl "http://localhost:3003/granules?orbit_number=10"

  Find granules with an orbit number in a range of 0.5 to 1.5

    curl "http://localhost:3003/granules?orbit_number=0.5,1.5"

### Find granules by orbit equator crossing longitude

  Find granules with an orbit equator crossing longitude in the range of 0 to 10

    curl "http://localhost:3003/granules?:equator+crossing_longitude=0,10

  Find granules with an equator crossing longitude in the range from 170 to -170
  (across the antimeridian)

    curl "http://localhost:3003/granules?:equator+crossing_longitude=170,-170

### Find granules by orbit equator crossing date

  Find granules with an orbit equator crossing date in the range of
  2000-01-01T10:00:00Z to 2010-03-10T12:00:00Z

    curl "http://localhost:3003/granules?:equator+crossing_date=2000-01-01T10:00:00Z,2010-03-10T12:00:00Z

### Find granules by updated_since param

  Find granules which have revision date starting at or after 'updated_since' param value

     curl "http://localhost:3003/granules?updated_since=2014-05-08T20:12:35Z"

### Find granules by cloud_cover param

  Find granules with just the min cloud cover value set to 0.2

     curl "http://localhost:3003/granules?cloud_cover=0.2,"

  Find granules with just the max cloud cover value set to 30

     curl "http://localhost:3003/granules?cloud_cover=,30"

  Find granules with cloud cover numeric range set to min: -70.0 max: 120.0

     curl "http://localhost:3003/granules?cloud_cover=-70.0,120.0"

### Find collections by platform param, supports pattern, ignore_case and option :and

     curl "http://localhost:3003/granules?platform\[\]=1B"

### Find collections by instrument param, supports pattern, ignore_case and option :and

     curl "http://localhost:3003/granules?instrument\[\]=1B"

### Find collections by sensor param, supports pattern, ignore_case and option :and

     curl "http://localhost:3003/granules?sensor\[\]=1B"

### Find granules by echo granule id, echo collection id and concept ids.
    Note: more than one may be supplied

  Find granule by concept id

    curl "http://localhost:3003/granules?concept_id\[\]=G1000000002-CMR_PROV1"

  Find granule by echo granule id

    curl "http://localhost:3003/granules?echo_granule_id\[\]=G1000000002-CMR_PROV1"

  Find granules by echo collection id

    curl "http://localhost:3003/granules?echo_collection_id\[\]=C1000000001-CMR_PROV2"

  Find granules by parent concept id

    curl "http://localhost:3003/granules?concept_id\[\]=C1000000001-CMR_PROV2"

### Exclude granules from elastic results by echo granule id and concept ids. Note: more than one id may be supplied in exclude param

Exclude granule by echo granule id

   curl "http://localhost:3003/granules?echo_granule_id\[\]=G1000000002-CMR_PROV1&echo_granule_id\[\]=G1000000003-CMR_PROV1&echo_granule_id\[\]=G1000000006-CMR_PROV2&exclude\[echo_granule_id\]\[\]=G1000000006-CMR_PROV2"

   curl "http://localhost:3003/granules?exclude\[echo_granule_id\]\[\]=G1000000006-CMR_PROV2&cloud_cover=-70,120"

Exclude granule by concept id

   curl "http://localhost:3003/granules?echo_granule_id\[\]=G1000000002-CMR_PROV1&echo_granule_id\[\]=G1000000003-CMR_PROV1&echo_granule_id\[\]=G1000000006-CMR_PROV2&exclude\[concept_id\]\[\]=G1000000006-CMR_PROV2"

Exclude granule by parent concept id

   curl "http://localhost:3003/granules?echo_granule_id\[\]=G1000000002-CMR_PROV1&echo_granule_id\[\]=G1000000003-CMR_PROV1&echo_granule_id\[\]=G1000000006-CMR_PROV2&exclude\[concept_id\]\[\]=C1000000001-CMR_PROV2"

#### Sorting Granule Results

Granule results are sorted by ascending provider and start date by default. One or more sort keys can be specified using the `sort_key[]` parameter. The order used impacts searching. Fields can be prepended with a `-` to sort in descending order. Ascending order is the default but `+` can be used to explicitly request ascending.

##### Valid Granule Sort Keys

  * campaign - alias for project
  * entry_title
  * dataset_id - alias for entry_title
  * data_size
  * end_date
  * granule_ur
  * producer_granule_id
  * project
  * provider
  * readable_granule_name - this sorts on a combination of producer_granule_id and granule_ur. If a producer_granule_id is present, that value is used. Otherwise, the granule_ur is used.
  * short_name
  * start_date
  * version
  * platform
  * instrument
  * sensor

##### Future Granule Sort Keys
Not yet implemented

  * browse_only
  * cloud_cover
  * day_night_flag
  * online_only

Example of sorting by start_date in descending order: (Most recent data first)

    curl "http://localhost:3003/granules/sort_key\[\]=-start_date


### Retrieve concept with a given cmr-concept-id
    curl -i "http://localhost:3003/concepts/G100000-PROV1"

### Reset cache
curl -i -XPOST -H "Content-Type: application/json" http://localhost:3003/reset

## Search Flow

### Stage 1: Convert to query model

/granules?provider=PROV1&dataset_id=foo&cloud_cover=50

  * Query
    * type: granule
    * condition:
      * AND
        * collection_query_condition:
          * condition:
            * provider=PROV1
        * collection_query_condition:
          * condition
            * dataset_id=foo
        * NumericRange
          * cloud_cover=50

### Stage 2: Add Acls to query


In a future sprint lookup acls and convert to query conditions then add on to the query.




### Stage 3: Resolve Dataset Query Conditions

#### A: Merge dataset query conditiosn

query = Search::Simplification::DatasetQueryConditionSimplifier.simplify(query)

  * Query
    * type: granule
    * condition:
      * AND
        * collection_query_condition:
          * condition:
            * AND
              * provider=PROV1
              * dataset_id=foo
        * NumericRange
          * cloud_cover=50

#### B: Resolve dataset query conditiosn

query = ElasticSearch::DatasetQueryResolver.resolve_collection_query_conditions(query)

Executes this query for collections
  * Query
    * type: collection
    * condition:
      * AND
        * provider=PROV1
        * dataset_id=foo

  * Query
    * type: granule
    * condition:
      * AND
        * String
          * echo_collection_id=C5-PROV1
        * NumericRange
          * cloud_cover=50


### Normal path...



## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

## License

Copyright © 2014 NASA
