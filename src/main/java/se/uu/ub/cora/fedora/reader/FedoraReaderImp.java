/*
 * Copyright 2018 Uppsala University Library
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
package se.uu.ub.cora.fedora.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.fedora.data.FedoraListSession;
import se.uu.ub.cora.fedora.data.FedoraReaderXmlHelper;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

public class FedoraReaderImp implements FedoraReader {
	private static final int OK = 200;
	private static final int NOT_FOUND = 404;
	private static final int DEFAULT_MAX_RESULTS = 100;
	private final HttpHandlerFactory httpHandlerFactory;
	private final FedoraReaderXmlHelper fedoraReaderXmlHelper;
	private final String baseUrl;
	private int maxResults = DEFAULT_MAX_RESULTS;

	public static FedoraReaderImp usingHttpHandlerFactoryAndFedoraReaderXmlHelperAndBaseUrl(
			HttpHandlerFactory httpHandlerFactory, FedoraReaderXmlHelper fedoraReaderXmlHelper,
			String baseUrl) {
		return new FedoraReaderImp(httpHandlerFactory, fedoraReaderXmlHelper, baseUrl);
	}

	private FedoraReaderImp(HttpHandlerFactory httpHandlerFactory,
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
		return String.format("%s/objects/%s/datastreams/METADATA/content", baseUrl, objectId);
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
				listUrl = getNextListUrlUsingCursorAndStart(type, start, responseText);
			}
		} catch (Exception e) {
			throw new RuntimeException("FedoraReader: " + e.getMessage(), e);
		}
		return listUrl;
	}

	private String getFedoraUrlForType(String type, int maxResults) {
		return String.format("%s/objects?pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",
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

	private String getNextListUrlUsingCursorAndStart(String type, int start, String responseText) {
		var cursor = fedoraReaderXmlHelper.getSessionIfAvailable(responseText);
		if (cursor == null) {
			return null;
		}
		if (start == 0) {
			return getFedoraCursorUrlForType(type, maxResults, cursor);
		}
		return getFedoraCursorUrlForType(type, start, cursor);
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
		var cursor = fedoraReaderXmlHelper.getSessionIfAvailable(responseXML);
		if (cursor != null) {
			String nextPageInCursor = getFedoraCursorUrlForType(type, maxResults, cursor);
			return getFedoraXml(type, nextPageInCursor);
		}
		return List.of();
	}

	private List<String> possiblyReadMoreObjectsFromFedoraLimitByRows(String type, int rows,
			String responseXML) {
		var cursor = fedoraReaderXmlHelper.getSessionIfAvailable(responseXML);
		if (cursor != null) {
			String nextPageInCursor = getFedoraCursorUrlForType(type, maxResults, cursor);
			return getObjectXmlListLimitByRows(type, nextPageInCursor, rows);
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

	private String getFedoraCursorUrlForType(String type, int maxResults,
			FedoraListSession cursor) {
		return String.format(
				"%s/objects?sessionToken=%s&pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",
				baseUrl, cursor.getToken(), maxResults, type);
	}

	protected String onlyForTestGetBaseUrl() {
		return baseUrl;
	}

	protected HttpHandlerFactory onlyForTestGetHttpHandlerFactory() {
		return httpHandlerFactory;
	}

	protected FedoraReaderXmlHelper onlyForTestGetFedoraReaderXmlHelper() {
		return fedoraReaderXmlHelper;
	}

	@Override
	public List<String> readPidsForType(String type) {
		String fedoraUrlForType = getFedoraUrlForType("someType", Integer.MAX_VALUE);
		HttpHandler httpHandler = httpHandlerFactory.factor(fedoraUrlForType);
		httpHandler.getResponseCode();

		String resultFromHttpHandler = httpHandler.getResponseText();
		List<String> pidList = fedoraReaderXmlHelper.getPidList(resultFromHttpHandler);
		FedoraListSession session = fedoraReaderXmlHelper
				.getSessionIfAvailable(resultFromHttpHandler);

		List<String> pidListOut = new ArrayList<>();
		pidListOut.addAll(pidList);
		// responseXML
		// fedoraReaderXmlHelper.getPidList
		// läggas till lista som ska returneras
		// loopa
		return pidListOut;
	}
}
