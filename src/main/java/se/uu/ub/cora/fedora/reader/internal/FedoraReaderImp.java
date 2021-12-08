/*
 * Copyright 2018, 202 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.fedora.reader.internal;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.fedora.parser.FedoraReaderXmlHelper;
import se.uu.ub.cora.fedora.parser.ListSession;
import se.uu.ub.cora.fedora.reader.FedoraException;
import se.uu.ub.cora.fedora.reader.FedoraReader;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

public class FedoraReaderImp implements FedoraReader {
	private static final String CREATED_DATE = "cDate";
	private static final String MODIFIED_DATE = "mDate";
	private static final int OK = 200;
	private static final int NOT_FOUND = 404;
	private static final int DEFAULT_MAX_RESULTS = 100;
	private static final String EQUALS = "%3D";
	private static final String LARGER_THAN = "%3E";
	private static final String SMALLER_THAN = "%3C";
	private static final String SPACE = "%20";
	private static final String TILDE = "%7E";

	private final HttpHandlerFactory httpHandlerFactory;
	private final FedoraReaderXmlHelper fedoraReaderXmlHelper;
	private final String baseUrl;
	private int maxResults = DEFAULT_MAX_RESULTS;
	private List<String> pidListOut;

	public static FedoraReaderImp usingHttpHandlerFactoryAndFedoraReaderXmlHelperAndBaseUrl(
			HttpHandlerFactory httpHandlerFactory, FedoraReaderXmlHelper fedoraReaderXmlHelper,
			String baseUrl) {
		return new FedoraReaderImp(httpHandlerFactory, fedoraReaderXmlHelper, baseUrl);
	}

	protected FedoraReaderImp(HttpHandlerFactory httpHandlerFactory,
			FedoraReaderXmlHelper fedoraReaderXmlHelper, String baseUrl) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.fedoraReaderXmlHelper = fedoraReaderXmlHelper;
		this.baseUrl = baseUrl;
	}

	@Override
	public void setMaxResults(int count) {
		maxResults = count;
	}

	@Override
	public String readObject(String objectId) {
		var objectUrl = getURLForFedoraObject(objectId);
		return getResponseFromFedora(objectId, objectUrl);
	}

	private String getURLForFedoraObject(String objectId) {
		return String.format("%sobjects/%s/datastreams/METADATA/content", baseUrl, objectId);
	}

	private String getResponseFromFedora(String id, String listUrl) {
		var httpHandler = httpHandlerFactory.factor(listUrl);
		throwIfNotOk(id, httpHandler.getResponseCode());
		return httpHandler.getResponseText();
	}

	private void throwIfNotOk(String id, int responseCode) {
		if (responseCode == NOT_FOUND) {
			throw new RuntimeException("Fedora object not found: " + id);
		}
		if (responseCode != OK) {
			throw new RuntimeException("Fedora call failed: " + responseCode);
		}
	}

	@Override
	public List<String> readList(String type, DataGroup filter) {
		String listUrl = getListUrlAfterSkippingToStart(type, filter);
		return getXmlForTypeFromFedora(type, filter, listUrl);
	}

	private String getListUrlAfterSkippingToStart(String type, DataGroup filter) {
		int start = getStartFromFilter(filter);
		if (start > 0) {
			return skipToStart(type, start);
		} else {
			return getFedoraUrlForType(type, maxResults);
		}
	}

	private int getStartFromFilter(DataGroup filter) {
		var start = getStartValueOrZeroAsDefaultFromFilter(filter);
		throwIfStartIsInvalid(start);
		return start;
	}

	private void throwIfStartIsInvalid(int start) {
		if (start < 0) {
			throw new RuntimeException(String.format("Invalid start value (%d)", start));
		}
	}

	private String skipToStart(String type, int start) {
		String listUrl = getFedoraUrlForType(type, start);
		try {
			while (listUrl != null && start > 0) {
				String responseText = getResponseFromFedora(type, listUrl);
				start = updateStartWithNumberOfPidRead(start, responseText);
				listUrl = getNextListUrlUsingSessionAndStart(type, start, responseText);
			}
		} catch (Exception e) {
			throw new RuntimeException("FedoraReader: " + e.getMessage(), e);
		}
		return listUrl;
	}

	private String getFedoraUrlForType(String type, int maxResults) {
		return String.format("%sobjects?pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",
				baseUrl, maxResults, type);
	}

	private List<String> getXmlForTypeFromFedora(String type, DataGroup filter, String listUrl) {
		if (listUrl == null) {
			return List.of();
		}
		if (filterHasRowsRequest(filter)) {
			return getFedoraXmlLimitedByRows(type, filter, listUrl);
		}
		return getFedoraXml(type, listUrl);
	}

	private List<String> getFedoraXmlLimitedByRows(String type, DataGroup filter, String listUrl) {
		int rows = tryGetRowsFromFilter(filter);
		if (rows == 0) {
			return List.of();
		}
		return getObjectXmlListLimitByRows(type, listUrl, rows);
	}

	private int updateStartWithNumberOfPidRead(int start, String responseText) {
		var pidList = fedoraReaderXmlHelper.getPidList(responseText);
		start -= pidList.size();
		return start;
	}

	private String getNextListUrlUsingSessionAndStart(String type, int start, String responseText) {
		ListSession listSession = fedoraReaderXmlHelper.getSession(responseText);
		if (!listSession.hasMoreResults) {
			return null;
		}
		if (start == 0) {
			return getFedoraSessionUrlForType(type, maxResults, listSession);
		}
		return getFedoraSessionUrlForType(type, start, listSession);
	}

	private int tryGetRowsFromFilter(DataGroup filter) {
		var rows = getRowsFromFilter(filter);
		if (rows < 0) {
			throw new RuntimeException(String.format("Invalid row count (%d)", rows));
		}
		return rows;
	}

	private int getStartValueOrZeroAsDefaultFromFilter(DataGroup filter) {
		if (filter.containsChildWithNameInData("start")) {

			return Integer.parseInt(filter.getFirstAtomicValueWithNameInData("start"));
		}
		return 0;
	}

	private boolean filterHasRowsRequest(DataGroup filter) {
		return filter.containsChildWithNameInData("rows");
	}

	private int getRowsFromFilter(DataGroup filter) {
		return Integer.parseInt(filter.getFirstAtomicValueWithNameInData("rows"));
	}

	private List<String> getFedoraXml(String type, String pidListUrl) {
		try {
			var responseText = getResponseFromFedora(type, pidListUrl);
			return possiblyGetRequestedObjectsFromFedora(type, responseText);
		} catch (Exception e) {
			throw new RuntimeException("FedoraReader: " + e.getMessage(), e);
		}
	}

	private List<String> possiblyGetRequestedObjectsFromFedora(String type, String responseXML) {
		var pidList = fedoraReaderXmlHelper.getPidList(responseXML);
		List<String> result = pidList.stream().map(this::readObject).collect(Collectors.toList());

		result.addAll(possiblyReadMoreObjectsFromFedora(type, responseXML));

		return result;
	}

	private List<String> getObjectXmlListLimitByRows(String type, String pidListUrl, int rows) {
		if (rows <= 0) {
			return List.of();
		}
		String responseXML = getResponseFromFedora(type, pidListUrl);
		try {
			return possiblyGetRequestedObjectsFromFedoraLimitedByRows(type, rows, responseXML);
		} catch (Exception e) {
			throw new RuntimeException("Invalid XML", e);
		}
	}

	private List<String> possiblyGetRequestedObjectsFromFedoraLimitedByRows(String type, int rows,
			String responseXML) {
		var pidList = fedoraReaderXmlHelper.getPidList(responseXML);
		List<String> result = possiblyGetObjectsFromFedoraLimitByRows(rows, pidList);

		int remainingRowsToRead = rows - pidList.size();
		result.addAll(possiblyReadMoreObjectsFromFedoraLimitByRows(type, remainingRowsToRead,
				responseXML));
		return result;
	}

	private List<String> possiblyReadMoreObjectsFromFedora(String type, String responseXML) {
		ListSession listSession = fedoraReaderXmlHelper.getSession(responseXML);
		if (listSession.hasMoreResults) {
			String sessionUrl = getFedoraSessionUrlForType(type, maxResults, listSession);
			return getFedoraXml(type, sessionUrl);
		}
		return List.of();
	}

	private List<String> possiblyReadMoreObjectsFromFedoraLimitByRows(String type, int rows,
			String responseXML) {
		ListSession listSession = fedoraReaderXmlHelper.getSession(responseXML);
		if (listSession.hasMoreResults) {
			String sessionUrl = getFedoraSessionUrlForType(type, maxResults, listSession);
			return getObjectXmlListLimitByRows(type, sessionUrl, rows);
		}
		return List.of();
	}

	private List<String> possiblyGetObjectsFromFedoraLimitByRows(int rows, List<String> pidList) {
		int upperLimitOfPidList = getUpperLimitOfPidList(rows, pidList);
		return pidList.subList(0, upperLimitOfPidList).stream().map(this::readObject)
				.collect(Collectors.toList());
	}

	private int getUpperLimitOfPidList(int rows, List<String> pidList) {
		return rows > pidList.size() ? pidList.size() : rows;
	}

	private String getFedoraSessionUrlForType(String type, int maxResults, ListSession session) {
		return String.format(
				"%sobjects?sessionToken=%s&pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",
				baseUrl, session.getToken(), maxResults, type);
	}

	public String onlyForTestGetBaseUrl() {
		return baseUrl;
	}

	public HttpHandlerFactory onlyForTestGetHttpHandlerFactory() {
		return httpHandlerFactory;
	}

	public FedoraReaderXmlHelper onlyForTestGetFedoraReaderXmlHelper() {
		return fedoraReaderXmlHelper;
	}

	@Override
	public List<String> readPidsForType(String type) {
		try {
			String urlQuery = queryForActivePidsForType(type);
			return readListOfPidsUsingUrlQuery(urlQuery);
		} catch (Exception e) {
			throw FedoraException.withMessageAndException("Error reading pids for type: " + type,
					e);
		}
	}

	private String queryForActivePidsForType(String type) {
		return queryForPidsForTypeAndState(type, "A");
	}

	private String queryForPidsForTypeAndState(String type, String state) {
		return baseUrl + "objects?pid=true&maxResults=" + Integer.MAX_VALUE
				+ "&resultFormat=xml&query=state" + EQUALS + state + SPACE + "pid" + TILDE + type
				+ ":*";
	}

	List<String> readListOfPidsUsingUrlQuery(String urlQuery) {
		pidListOut = new ArrayList<>();
		String resultFromFedora = getFirstFedoraResponseForUrl(urlQuery);
		addPidsFromResponseToPidList(resultFromFedora);
		possiblyAddSubsequentPidsToPidList(resultFromFedora);
		return pidListOut;
	}

	private String getFirstFedoraResponseForUrl(String urlQuery) {

		return getFedoraResponseForUrl(urlQuery);
	}

	private String getFedoraResponseForUrl(String urlQuery) {
		HttpHandler httpHandler = httpHandlerFactory.factor(urlQuery);
		int responseCode = httpHandler.getResponseCode();
		if (responseCode == OK) {
			return httpHandler.getResponseText();
		}
		throw new RuntimeException(
				"Error communicating with fedora, responseCode: " + responseCode);
	}

	private void addPidsFromResponseToPidList(String responseText) {
		List<String> pidList = fedoraReaderXmlHelper.getPidList(responseText);
		pidListOut.addAll(pidList);
	}

	private void possiblyAddSubsequentPidsToPidList(String resultFromFedora) {
		ListSession session = fedoraReaderXmlHelper.getSession(resultFromFedora);
		if (session.hasMoreResults) {
			addSubsequentPidsToPidList(session);
		}
	}

	private void addSubsequentPidsToPidList(ListSession session) {
		String listUrl = baseUrl + "objects?resultFormat=xml&sessionToken=" + session.getToken();

		String resultFromFedora = getFedoraResponseForUrl(listUrl);
		addPidsFromResponseToPidList(resultFromFedora);
		possiblyAddSubsequentPidsToPidList(resultFromFedora);
	}

	@Override
	public List<String> readPidsForTypeCreatedAfter(String type, String dateTime) {
		try {
			String urlQuery = queryForActivePidsForType(type) + SPACE + CREATED_DATE + LARGER_THAN
					+ dateTime;
			return readListOfPidsUsingUrlQuery(urlQuery);
		} catch (Exception e) {
			String logM = "Error reading pids created after for type: {0} and dateTime: {1}";
			throw FedoraException
					.withMessageAndException(MessageFormat.format(logM, type, dateTime), e);
		}
	}

	@Override
	public List<String> readPidsForTypeUpdatedAfter(String type, String dateTime) {
		try {
			String urlQuery = queryForActivePidsForType(type) + SPACE + MODIFIED_DATE + LARGER_THAN
					+ dateTime;
			return readListOfPidsUsingUrlQuery(urlQuery);
		} catch (Exception e) {
			String logM = "Error reading pids updated after for type: {0} and dateTime: {1}";
			throw FedoraException
					.withMessageAndException(MessageFormat.format(logM, type, dateTime), e);
		}
	}

	@Override
	public List<String> readPidsForTypeCreatedBeforeAndUpdatedAfter(String type, String dateTime) {
		try {
			String urlQuery = queryForActivePidsForType(type) + SPACE + CREATED_DATE + SMALLER_THAN
					+ dateTime + SPACE + MODIFIED_DATE + LARGER_THAN + EQUALS + dateTime;
			return readListOfPidsUsingUrlQuery(urlQuery);
		} catch (Exception e) {
			String logM = "Error reading pids created before and updated after for type: {0} "
					+ "and dateTime: {1}";
			throw FedoraException
					.withMessageAndException(MessageFormat.format(logM, type, dateTime), e);
		}
	}

	@Override
	public List<String> readPidsForTypeDeletedAfter(String type, String dateTime) {
		try {
			String urlQuery = queryForDeletedPidsForType(type) + SPACE + MODIFIED_DATE + LARGER_THAN
					+ EQUALS + dateTime;
			return readListOfPidsUsingUrlQuery(urlQuery);
		} catch (Exception e) {
			String logM = "Error reading pids deleted after for type: {0} and dateTime: {1}";
			throw FedoraException
					.withMessageAndException(MessageFormat.format(logM, type, dateTime), e);
		}

	}

	private String queryForDeletedPidsForType(String type) {
		return queryForPidsForTypeAndState(type, "D");
	}

}
