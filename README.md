# Addon: exo-analytics

An addon that provide tools to display platform usage statistics using charts.

# Compatibility

The exo-analytics addon is compatible on the following dependencies versions :

| Addon version        | JDK | Elasticsearch | eXo Platform          |
|----------------------|-----|---------------|-----------------------|
| exo-addons:1.0.0-M01 | 8   | 5.6           | 6.0                   |

# Prerequisites

The addon requires eXo Platform with embedded Elasticsearch version inside eXo Platform (default configuration)
or in a standalone Elasticsearch server configured.
See [Elasticsearch configuration in eXo Platform](https://docs.exoplatform.org/en/latest/Configuration.html#elasticsearch-configuration)

# How to install

1. Go to eXo Platform Tomcat server Home
2. Install exo-analytics addon using command line:

```bash
# Replace version by the needed version of addon, like 1.0.x-SNAPSHOT or 1.0.0-M01
./addon install exo-analytics:VERSION
```
3. Start eXo Platform

## Configuration options

You can can change some options of the addon using properties that you can add into [exo.properties](https://docs.exoplatform.org/en/latest/Configuration.html#configuration-configurationoverview) file

| VARIABLE               | MANDATORY | DEFAULT VALUE | DESCRIPTION                                                                               |
|------------------------|-----------|---------------|-------------------------------------------------------------------------------------------|
| exo.analytics.admin.permission        | NO        | *:/platform/analytics | Group of users that can modify Charts application settings. All other users, *even members of /platform/administrators* will not be able to modify charts settings |
| exo.analytics.aggregation.terms.doc_size        | NO        | 200 | Limit of number of resturned documents in aggregations result of type 'terms' (not used for aggregations of type : sum, avg, date_histogram, histogram and cardinality) |
| exo.addon.analytics.es.mapping.path | NO        | jar:/analytics-es-mapping.json             | Initial [ES mapping](https://github.com/exo-addons/analytics/blob/master/analytics-services/src/main/resources/analytics-es-mapping.json) that will be used to generate daily [ES index](https://www.elastic.co/guide/en/elasticsearch/reference/5.6/mapping.html)   |
| exo.cache.analytics.queue.MaxNodes | NO        | 2000             | Number of maximum entries in in-memory cached Analytics Queue that is processed each 10 seconds |
| exo.cache.analytics.queue.TimeToLive | NO        | -1             | lifetime of entries in Analytics Queue. Default: infinite. |

## How to build addon

1. install Apache maven 3.5+
1. Go to Home path of addon
2. Execute the following command :

```bash
mvn clean verify -Prun-its
```

## How to use it

Once the addon installed and eXo Platform started,
and after [logged-in on eXo](https://docs.exoplatform.org/en/latest/GettingStarted.html#welcome-to-exo-platform),
you can follow those steps:

1. Add a user into analytics administrators (Under Platform => Analytics) group [using group managment UI](https://docs.exoplatform.org/en/latest/Administration.html#managing-groups)
2. Go to application registry and [add Analytics portlet](https://docs.exoplatform.org/en/latest/Administration.html#managing-applications) in an application category
3. Go to home page and [add a new page](https://docs.exoplatform.org/en/latest/Administration.html#adding-a-new-page) in current site
4. You can add multiple instances of analytics portlet inside the page to show multiple statistics data
5. Save the page

You will see the analytics application added in your page and displaying by default a default chart including all statistics data.
You can change settings of each application to show you different charts type, title and statistics data.

By default, the user root is a member of `/platform/*analytics*` group, so he can see the charts,
while for other users (if you added right access permissions on page and added portlets in page),
he will see only his data without being able to change charts settings.
In fact, the user and space filtering on statistics data is made automatically switch loggedin user and page context (inside a space or in public page)

The default collected statistics data are:
* [Activities statistics data](https://github.com/exo-addons/analytics/blob/master/analytics-listeners/src/main/java/org/exoplatform/analytics/listener/AnalyticsActivityListener.java) :
  * create activity
  * create comment
  * update activity
  * update comment
  * like activity
  * like comment.
* [Space statistics data](https://github.com/exo-addons/analytics/blob/master/analytics-listeners/src/main/java/org/exoplatform/analytics/listener/SpaceAnalyticsListener.java):
  * create a space
  * remove a space
  * rename a space
  * update space description
  * update space registration
  * update space access
  * update space banner
  * update space avatar
  * add space application
  * disable space application
  * delete space application
  * grant space member as manager
  * revoke space member as manager
  * joining a space
  * leaving a space
  * inviting a user to a space
  * requesting join a space by a user
* [User statistics data](https://github.com/exo-addons/analytics/blob/master/analytics-listeners/src/main/java/org/exoplatform/analytics/listener/UserAnalyticsEventListener.java):
  * Create a user
  * Delete a user
  * Enable/Disable a user
* [User Profile statistics data](https://github.com/exo-addons/analytics/blob/master/analytics-listeners/src/main/java/org/exoplatform/analytics/listener/AnalyticsProfileListener.java):
  * create new user social profile (generally triggered at same time than create a user event)
  * update avatar
  * update banner
  * update contact section
  * update experience section
* [User login statistics data](https://github.com/exo-addons/analytics/blob/master/analytics-listeners/src/main/java/org/exoplatform/analytics/listener/LoginAnalyticsListener.java):
  * login / logout of a user
* [User relationship statistics data](https://github.com/exo-addons/analytics/blob/master/analytics-listeners/src/main/java/org/exoplatform/analytics/listener/AnalyticsRelationshipListener.java):
  * request connection with other user
  * accept connection with other user
  * deny connection with other user
  * ignore connection request of other user
  * remove connection with other user

Technical note: By default, the produced and persisted `statistic samples/data` by those `listeners`
has basic properties stored into it and that could be extended any time because a dynamic mapping is used on each daily
created Elasticsearch index.

The default produced data for all events in general:
* `id` : Technical id of statistics sample
* `spaceId` : Technical id of space (space name is displayed in UI in list of samples)
 (Perspective: could be retrieved using suggester for data filtering and charts setttings)
* `userId` : Technical id of user making the event (user display name is displayed in UI in list of samples)
 (Perspective: could be retrieved using suggester for data filtering and charts setttings)
* `module` : eXo project name: `social`, `portal`...
* `subModule` : `activity`, `space`, `login`, `relationship`, `profile`, `relationship`...
* `operation` : type of made operation, like `createActivity`, `login`, `logout`, `login`, `confirmed`...
* `status` : status of operation `OK` if operation succeeded else `KO`
* `timestamp` : time of statistic event in ms

Specifically for each listener, it has default produced data are: (data can be extend, like adding number of likes, comments...)
* Activity statistic data:
  * `module` = `social`
  * `subModule` = `activity`
  * `operation` =
    * `createActivity`
    * `updateActivity`
    * `createComment`
    * `updateComment`
    * `likeActivity`
    * `likeComment`
  * `streamIdentityId` : Technical identity ID of stream owner (Space ID or User ID)
   (Perspective: could be retrieved using suggester for data filtering and charts setttings)
  * `activityId` : Technical activity ID
  * `commentId` : Technical comment ID
  * `subCommentId` : Technical sub comment ID
  * `activityType` : activity type, like : `exosocial:people`, `USER_PROFILE_ACTIVITY`... (See complete list of [actitivy types](https://docs.exoplatform.org/en/latest/Configuration.html#enabling-disabling-any-activity-type))
* Space statistic data:
  * `module` = `social`
  * `subModule` = `space`
  * `operation` =
    * `spaceAccessEdited`
    * `spaceBannerEdited`
    * `spaceCreated`
    * `spaceRemoved`
    * `spaceRenamed`
    * `spaceDescriptionEdited`
    * `spaceRegistrationEdited`
    * `spaceAvatarEdited`
    * `applicationActivated`
    * `applicationDeactivated`
    * `applicationRemoved`
    * `grantedLead`
    * `joined`
    * `revokedLead`
    * `addInvitedUser`
    * `addPendingUser`
  * `modifierUserId`: Technical identity ID of user havin modified portlet
   (Perspective: could be retrieved using suggester for data filtering and charts setttings)
  * `spaceTemplate`: space template name
* Login statistic data:
  * `module` = `portal`
  * `subModule` = `login`
  * `operation` =
    * `login`
    * `logout`
* User statistic data:
  * `module` = `organization`
  * `subModule` = `user`
  * `operation` =
    * `createUser`
    * `saveUser`
    * `enableUser`
    * `deleteUser`
  * `isEnabled` : if user is enabled
* Profile statistic data:
  * `module` = `social`
  * `subModule` = `profile`
  * `operation` =
    * `avatar`
    * `banner`
    * `contactSection`
    * `experienceSection`
    * `createProfile`
* Relationship statistic data:
  * `module` = `social`
  * `subModule` = `relationship`
  * `operation` =
    * `avatar`
    * `banner`
    * `contactSection`
    * `experienceSection`
    * `createProfile`
  * `from`: Technical identity ID of user (Perspective: could be retrieved using suggester for data filtering and charts setttings)
  * `to` : Technical identity ID of user (Perspective: could be retrieved using suggester for data filtering and charts setttings)

You can consult the data used to display analytics chart using `View samples` button on each portlet.
By viewing samples, you will be able to know what `fields` to use in settings to :
* choose X Axis field (date by default)
  * Per day
  * Per week
  * Per month
  * Per quarter
  * Per year
  * Per custom field : you can choose a custom field from the selectbox:
  for now, the fields are retrieved without name, just its technical name that you can find in displayed `statistic samples/data` in drawer.
  For example, if you choose `spaceId`, then in the X Axis, you will get the space name.
* choose Y Axis:
  * *count* of `statistic samples/data` per choosen X Axis field (date period or custom field)
  * *count samples with distinct fields*.
  For example, to get the number logins per 'unique users' per day, you will choose `Per day` as X Axis and this option as Y Axis.
  * *sum of* `statistic samples/data numeric field` : this will display in Y Axis the sum of a specific `Numeric` field.
  * *average of* `statistic samples/data numeric field` : this will display in Y Axis the average rate of a specific `Numeric` field.
* Data filters : a list of conditions on to filter on used data in computing results
* Multiple charts :
  * When enabled, this will display multiple charts in one single.

# Examples of settings

For example, if you want to display the number of created activities per day and per space (pay attention to the number of spaces).

ou can choose the following parameters:
  * X Axis = `Per day`
  * Y Axis = `count samples`
  * Multiple charts = true, in addition choose field `spaceId` (a chart will be retrieved per spaceId)
  * Data filters :
   ** `operation` (`equals`) `createActivity`
   ** `spaceId` (`greater`) `1` (or if you have a lot of spaces, you will have to choose a set of `spaceId`, like :
   `spaceId` (`in set`) `1,2,3,895`)

## Issues

If you have an issue with the addon, you can ask your question in [eXo Tribe](https://community.exoplatform.com/)
or report your issue in current github repository.
