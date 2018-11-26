package se.uu.ub.cora.fedora.reader.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;
import se.uu.ub.cora.fedora.data.FedoraReaderXmlHelper;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

public class FedoraReaderPureImp implements FedoraReaderPure {
	private static final int OK = 200;
	private static final int NOT_FOUND = 404;
	private static final int DEFAULT_MAX_RESULTS = 100;
	private HttpHandlerFactory httpHandlerFactory;
	private FedoraReaderXmlHelper fedoraReaderXmlHelper;
	private String baseUrl;
	private int maxResults;

	public FedoraReaderPureImp(HttpHandlerFactory httpHandlerFactory,
			FedoraReaderXmlHelper fedoraReaderXmlHelper, String baseUrl) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.fedoraReaderXmlHelper = fedoraReaderXmlHelper;
		this.baseUrl = baseUrl;
		this.maxResults = DEFAULT_MAX_RESULTS;
	}

	@Override
	public void setMaxResults(int count) {
		maxResults = count;
	}

	@Override
	public String readObject(String objectId) {
		var objectUrl = getURLForFedoraObject(objectId);
		var httpHandler = httpHandlerFactory.factor(objectUrl);
		throwIfNotOk(objectId, httpHandler.getResponseCode());
		return httpHandler.getResponseText();
	}

	private String getURLForFedoraObject(String objectId) {
		return String.format("%s/objects/%s/datastreams/METADATA/content", baseUrl, objectId);
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
		int start = getStartFromFilter(filter);
		String listUrl;
		if (start > 0) {
			listUrl = skipToStart(type, start);
		} else {
			listUrl = getFedoraUrlForType(type, maxResults);
		}
		if (listUrl == null) {
			return List.of();
		}
		if (filterHasRowsRequest(filter)) {
			int rows = tryGetRowsFromFilter(filter);
			if (rows == 0) {
				return List.of();
			}
			return getObjectXmlListLimitByRows(type, listUrl, rows);
		}
		return getObjectXmlList(type, listUrl);
	}

	private String skipToStart(String type, int start) {
		String listUrl = getFedoraUrlForType(type, start);
		try {
			while (true) {
				var httpHandler = httpHandlerFactory.factor(listUrl);
				throwIfNotOk(type, httpHandler.getResponseCode());
				var pidList = fedoraReaderXmlHelper.getPidList(httpHandler.getResponseText());
				start -= pidList.size();
				var cursor = fedoraReaderXmlHelper
						.getCursorIfAvailable(httpHandler.getResponseText());
				if (cursor == null) {
					return null;
				}
				if (start == 0) {
					listUrl = getFedoraCursorUrlForType(type, maxResults, cursor);
					break;
				}
				listUrl = getFedoraCursorUrlForType(type, start, cursor);
			}
		} catch (Exception e) {
			throw new RuntimeException("Invalid XML", e);
		}
		return listUrl;
	}

	private int tryGetRowsFromFilter(DataGroup filter) {
		var rows = getRowsFromFilter(filter);
		if (rows < 0) {
			throw new RuntimeException(String.format("Invalid row count (%d)", rows));
		}
		return rows;
	}

	private int getStartFromFilter(DataGroup filter) {
		var start = getStartValueOrZeroAsDefaultFromFilter(filter);
		throwIfStartIsInvalid(start);
		return start;
	}

	private int getStartValueOrZeroAsDefaultFromFilter(DataGroup filter) {
		return Integer.parseInt(filter.getFirstAtomicValueWithNameInDataOrDefault("start", "0"));
	}

	private void throwIfStartIsInvalid(int start) {
		if (start < 0) {
			throw new RuntimeException(String.format("Invalid start value (%d)", start));
		}
	}

	private boolean filterHasRowsRequest(DataGroup filter) {
		return filter.containsChildWithNameInData("rows");
	}

	private int getRowsFromFilter(DataGroup filter) {
		return Integer.parseInt(filter.getFirstAtomicValueWithNameInData("rows"));
	}

	private String getFedoraUrlForType(String type, int maxResults) {
		return String.format("%s/objects?pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",
				baseUrl, maxResults, type);
	}

	private List<String> getObjectXmlList(String type, String pidListUrl) {
		var httpHandler = httpHandlerFactory.factor(pidListUrl);
		throwIfNotOk(type, httpHandler.getResponseCode());
		try {
			String responseXML = httpHandler.getResponseText();

			var pidList = fedoraReaderXmlHelper.getPidList(responseXML);
			List<String> result = pidList.stream().map(this::readObject)
					.collect(Collectors.toList());

			result.addAll(possiblyReadMoreObjectsFromFedora(type, responseXML));

			return result;
		} catch (Exception e) {
			throw new RuntimeException("Invalid XML", e);
		}
	}

	private List<String> getObjectXmlListLimitByRows(String type, String pidListUrl, int rows) {
		var httpHandler = httpHandlerFactory.factor(pidListUrl);
		throwIfNotOk(type, httpHandler.getResponseCode());
		try {
			String responseXML = httpHandler.getResponseText();

			var pidList = fedoraReaderXmlHelper.getPidList(responseXML);
			List<String> result = possiblyGetObjectsFromFedoraLimitByRows(rows, pidList);

			int remainingRowsToRead = rows - pidList.size();
			result.addAll(possiblyReadMoreObjectsFromFedoraLimitByRows(type, remainingRowsToRead,
					responseXML));

			return result;
		} catch (Exception e) {
			throw new RuntimeException("Invalid XML", e);
		}
	}

	private List<String> possiblyReadMoreObjectsFromFedora(String type, String responseXML) {
		var cursor = fedoraReaderXmlHelper.getCursorIfAvailable(responseXML);
		if (cursor != null) {
			String nextPageInCursor = getFedoraCursorUrlForType(type, maxResults, cursor);
			return getObjectXmlList(type, nextPageInCursor);
		}
		return List.of();
	}

	private List<String> possiblyReadMoreObjectsFromFedoraLimitByRows(String type, Integer rows,
			String responseXML) {
		var cursor = fedoraReaderXmlHelper.getCursorIfAvailable(responseXML);
		if (cursor != null) {
			return getRemainingObjectsFromFedoraLimitByRows(type, rows, cursor);
		}
		return List.of();
	}

	private List<String> getRemainingObjectsFromFedoraLimitByRows(String type, Integer rows,
			FedoraReaderCursor cursor) {
		if (rows > 0) {
			String nextPageInCursor = getFedoraCursorUrlForType(type, maxResults, cursor);
			return getObjectXmlListLimitByRows(type, nextPageInCursor, rows);
		}
		return List.of();
	}

	private List<String> possiblyGetObjectsFromFedoraLimitByRows(int rows, List<String> pidList) {
		var result = new ArrayList<String>();
		for (int idx = 0; idx < rows; idx++) {
			if (idx >= pidList.size()) {
				break;
			}
			result.add(readObject(pidList.get(idx)));
		}
		return result;
	}

	private String getFedoraCursorUrlForType(String type, int maxResults,
			FedoraReaderCursor cursor) {
		return String.format(
				"%s/objects?sessionToken=%s&pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",
				baseUrl, cursor.getToken(), maxResults, type);
	}
}
