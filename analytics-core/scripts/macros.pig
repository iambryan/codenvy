/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

---------------------------------------------------------------------------
-- Loads resources.
-- @return {ip : bytearray, dt : datetime,  event : bytearray, message : chararray, user : bytearray, ws : bytearray} 
-- In details:
--   field 'date' contains date in format 'YYYYMMDD'
--   field 'time' contains seconds from midnight
---------------------------------------------------------------------------
DEFINE loadResources(resourceParam, from, to, userType, wsType) RETURNS Y {
  l1 = LOAD '$resourceParam' using PigStorage() as (message : chararray);
  l2 = FOREACH l1 GENERATE REGEX_EXTRACT_ALL($0, '([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}) ([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}).*EVENT\\#([^\\#]*)\\#.*') 
                          AS pattern, message;
  l3 = FILTER l2 BY pattern.$2 != '';
  l4 = FOREACH l3 GENERATE pattern.$0 AS ip, ToDate(pattern.$1, 'yyyy-MM-dd HH:mm:ss,SSS') AS dt, pattern.$2 AS event, message;
  l5 = DISTINCT l4;

  l6 = filterByDate(l5, '$from', '$to');
  l7 = extractUser(l6, '$userType');
  $Y = extractWs(l7, '$wsType');
};

---------------------------------------------------------------------------
-- Removes tuples with empty fields
---------------------------------------------------------------------------
DEFINE removeEmptyField(X, fieldParam) RETURNS Y {
  $Y = FILTER $X BY $fieldParam != '' AND $fieldParam != 'default' AND $fieldParam != 'null' AND $fieldParam IS NOT NULL;
};

---------------------------------------------------------------------------
-- Filters events by date of occurrence.
-- @param fromDateParam - date in format 'YYYYMMDD'
-- @param toDateParam  - date in format 'YYYYMMDD'
---------------------------------------------------------------------------
DEFINE filterByDate(X, fromDateParam, toDateParam) RETURNS Y {
  $Y = FILTER $X BY MilliSecondsBetween(ToDate('$fromDateParam', 'yyyyMMdd'), dt) <= 0 AND
                    MilliSecondsBetween(AddDuration(ToDate('$toDateParam', 'yyyyMMdd'), 'P1D'), dt) > 0;
};

---------------------------------------------------------------------------
-- Returns the unique sequence for every field
-- @return {fieldName1 : chararray, {(fieldName2 : chararray)}}
---------------------------------------------------------------------------
DEFINE setByField(X, fieldName1, fieldName2) RETURNS Y {
	x1 = GROUP $X BY $fieldName1;
	$Y = FOREACH x1 {
		t1 = FOREACH $X GENERATE $fieldName2;
		t = DISTINCT t1;
		GENERATE group, t;
	}
};

---------------------------------------------------------------------------
-- Return the number of tuples in the relation
-- @return {countAll : long}
---------------------------------------------------------------------------
DEFINE countAll(X) RETURNS Y {
	x1 = GROUP $X ALL;
	$Y = FOREACH x1 GENERATE COUNT($X.$0) AS countAll;
};

---------------------------------------------------------------------------
-- Return the number of tuples in the relation
-- @return {fieldNameParam : chararray, countAll : long}
---------------------------------------------------------------------------
DEFINE countByField(X, fieldNameParam) RETURNS Y {
	x1 = GROUP $X BY $fieldNameParam;
	$Y = FOREACH x1 GENERATE group AS $fieldNameParam, COUNT($X.$0) AS countAll;
};

---------------------------------------------------------------------------
-- Filters events by names. Keeps only events from passed list.
-- @param eventNamesParam - comma separated list of event names
---------------------------------------------------------------------------
DEFINE filterByEvent(X, eventNamesParam) RETURNS Y {
  $Y = FILTER $X BY '$eventNamesParam' == '*' OR INDEXOF('$eventNamesParam', event, 0) >= 0;
};

---------------------------------------------------------------------------
-- Filters events by names. Keeps only events out of passed list.
-- @param eventsNameParam - comma separated list of event names
---------------------------------------------------------------------------
DEFINE removeEvent(X, eventNamesParam) RETURNS Y {
  $Y = FILTER $X BY INDEXOF('$eventNamesParam', event, 0) < 0;
};

---------------------------------------------------------------------------
-- Extract workspace name out of message and adds as field to tuple.
-- @return  {..., ws : bytearray}
---------------------------------------------------------------------------
DEFINE extractWs(X, wsType) RETURNS Y {
  x1 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[.*\\]\\[(.*)\\]\\[.*\\] - .*')) AS ws1, FLATTEN(REGEX_EXTRACT_ALL(message, '.*WS\\#([^\\#]*)\\#.*')) AS ws2;
  x2 = FOREACH x1 GENERATE *, (ws1 IS NOT NULL AND ws1 != '' ? ws1 : (ws2 IS NOT NULL AND ws2 != '' ? ws2 : 'default')) AS ws;
  $Y = FILTER x2 BY '$wsType' == 'ANY' OR 
		    ('$wsType' == 'TEMPORARY' AND INDEXOF(UPPER(ws), 'TMP-', 0) == 0) OR 
		    ('$wsType' == 'PERSISTENT' AND INDEXOF(UPPER(ws), 'TMP-', 0) < 0);
};

---------------------------------------------------------------------------
-- Extract user name out of message and adds as field to tuple.
-- @return  {..., user : bytearray}
---------------------------------------------------------------------------
DEFINE extractUser(X, userType) RETURNS Y {
  x1 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*USER\\#([^\\#]*)\\#.*')) AS user1,
			      FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[(.*)\\]\\[.*\\]\\[.*\\] - .*')) AS user2,
			      FLATTEN(REGEX_EXTRACT_ALL(message, '.*ALIASES\\#[\\[]?([^\\#^\\[^\\]]*)[\\]]?\\#.*')) AS user3;
  x2 = FOREACH x1 GENERATE *, (user1 IS NOT NULL AND user1 != '' ? user1 : (user2 IS NOT NULL AND user2 != '' ? user2 : (user3 IS NOT NULL AND user3 != '' ? user3 : 'default'))) AS newUser;
  x3 = FOREACH x2 GENERATE *, FLATTEN(TOKENIZE(newUser, ',')) AS user;
  $Y = FILTER x3 BY '$userType' == 'ANY' OR
		    ('$userType' == 'ANTONYMOUS' AND INDEXOF(UPPER(user), 'ANONYMOUSUSER_', 0) == 0) OR
		    ('$userType' == 'REGISTERED' AND INDEXOF(UPPER(user), 'ANONYMOUSUSER_', 0) < 0);
};

---------------------------------------------------------------------------
-- Extract parameter value out of message and adds as field to tuple.
-- @param paramNameParam - the parameter name
-- @param paramFieldNameParam - the name of filed in the tuple
-- @return  {..., $paramFieldNameParam : bytearray}
---------------------------------------------------------------------------
DEFINE extractParam(X, paramNameParam, paramFieldNameParam) RETURNS Y {
  $Y = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*$paramNameParam\\#([^\\#]*)\\#.*')) AS $paramFieldNameParam;
};

---------------------------------------------------------------------------------------------
-- Extracts session id
-- @return {user : bytearray, ws: bytearray, id: bytearray, dt: datetime}
---------------------------------------------------------------------------------------------
DEFINE extractEventsWithSessionId(X, eventParam) RETURNS Y {
    x1 = filterByEvent($X, '$eventParam');
    x2 = extractParam(x1, 'SESSION-ID', id);
    $Y = FOREACH x2 GENERATE user, ws, id, dt;
};

---------------------------------------------------------------------------------------------
-- Combines small sessions into big one if time between them is less than $inactiveInterval
-- @return {user : bytearray, ws: bytearray, dt: datetime, delta: long}
---------------------------------------------------------------------------------------------
DEFINE combineSmallSessions(X, startEvent, finishEvent) RETURNS Y {

    a = extractEventsWithSessionId($X, '$startEvent');

    b1 = extractEventsWithSessionId($X, '$finishEvent');

    -- avoids cases when there are several $finishEvent with same id, let's take the first one
    b2 = FOREACH b1 GENERATE ws, user, id, dt, MilliSecondsBetween(dt, ToDate('2010-01-01', 'yyyy-MM-dd')) AS delta;
    b3 = GROUP b2 BY id;
    b4 = FOREACH b3 GENERATE FLATTEN(group), MIN(b2.delta) AS minDelta, FLATTEN(b2);
    b5 = FILTER b4 BY delta == minDelta;
    b = FOREACH b5 GENERATE b2::ws AS ws, b2::user AS user, id AS id, b2::dt AS dt;


    -- joins $startEvent and $finishEvent by same id, removes events without corresponding pair
    c1 = JOIN a BY id LEFT, b BY id;
    c = removeEmptyField(c1, 'b::id');

    -- split them back
    d1 = FOREACH c GENERATE *, FLATTEN(TOKENIZE('$startEvent,$finishEvent', ',')) AS event;
    SPLIT d1 INTO d2 IF event == '$startEvent', d3 OTHERWISE;

    -- A: $startEvent
    A = FOREACH d2 GENERATE a::ws AS ws, a::user AS user, a::dt AS dt, a::id AS id;

    -- B: $finishEvent
    B = FOREACH d3 GENERATE b::ws AS ws, b::user AS user, b::dt AS dt, b::id AS id;

    -- joins $finishEvent and $startEvent, finds for every $finishEvent the closest
    -- $startEvent to decide whether the pause between them is less than $inactiveInterval
    e1 = JOIN B BY (ws, user) LEFT, A BY (ws, user);
    e2 = FILTER e1 BY A::ws IS NOT NULL;
    e3 = FOREACH e2 GENERATE B::id AS finishId, A::id AS startId, MilliSecondsBetween(A::dt, B::dt) AS interval;
    e = FILTER e3 BY interval > 0 AND interval <= (long) 10 * 60 * 1000; -- $inactiveInterval = 10min

    -- removes $startEvents which are close to any $finishEvent
    d1 = JOIN A BY id LEFT, e BY startId;
    d2 = FILTER d1 BY e::startId IS NULL;
    S = FOREACH d2 GENERATE A::ws AS ws, A::user AS user, A::dt AS dt, '$startEvent' AS event;

    -- removes $finishEvent which are close to any $startEvent
    f1 = JOIN B BY id LEFT, e BY finishId;
    f2 = FILTER f1 BY e::finishId IS NULL;
    F = FOREACH f2 GENERATE B::ws AS ws, B::user AS user, B::dt AS dt, '$finishEvent' AS event;

    -- finally, combines closest events to get completed sessions
    U = UNION S, F;
    $Y = combineClosestEvents(U, '$startEvent', '$finishEvent');
};

---------------------------------------------------------------------------------------------
-- Calculates time between pairs of $startEvent and $finishEvent
-- @return {user : bytearray, ws: bytearray, dt: datetime, delta: long}
---------------------------------------------------------------------------------------------
DEFINE combineClosestEvents(X, startEvent, finishEvent) RETURNS Y {
    x1 = removeEmptyField($X, 'ws');
    x = removeEmptyField(x1, 'user');

    a1 = filterByEvent(x, '$startEvent');
    a = FOREACH a1 GENERATE ws, user, event, dt;

    b1 = filterByEvent(x, '$startEvent,$finishEvent');
    b = FOREACH b1 GENERATE ws, user, event, dt;

    -- joins $startEvent with all other events to figure out which event is mostly close to '$startEvent'
    c1 = JOIN a BY (ws, user), b BY (ws, user);
    c2 = FOREACH c1 GENERATE a::ws AS ws, a::user AS user, a::event AS event, a::dt AS dt, b::event AS secondEvent, b::dt AS secondDt;

    -- @param delta: milliseconds between $startEvent and second event
    c3 = FOREACH c2 GENERATE *, MilliSecondsBetween(secondDt, dt) AS delta;

    -- removes cases when second event is preceded by $startEvent (before $startEvent in time line)
    c = FILTER c3 BY delta > 0;

    g1 = GROUP c BY (ws, user, event, dt);
    g2 = FOREACH g1 GENERATE group.ws AS ws, group.user AS user, group.dt AS dt, FLATTEN(c), MIN(c.delta) AS minDelta;

    -- the desired closest event have to be $finishEvent anyway
    g = FILTER g2 BY delta == minDelta AND c::secondEvent == '$finishEvent';

    -- converts time into seconds
    $Y = FOREACH g GENERATE ws, user, dt, delta / 1000 AS delta;
};