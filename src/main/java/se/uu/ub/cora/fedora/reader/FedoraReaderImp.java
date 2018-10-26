package se.uu.ub.cora.fedora.reader;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.bookkeeper.data.DataElement;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.CoraLogger;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;
import se.uu.ub.cora.fedora.data.FedoraReaderPidListWithOptionalCursor;
import se.uu.ub.cora.fedora.data.XMLXPathParser;
import se.uu.ub.cora.fedora.data.XMLXPathParserException;
import se.uu.ub.cora.fedora.data.XMLXPathParserFactory;
import se.uu.ub.cora.fedora.reader.converter.FedoraReadPositionConverter;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverter;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterException;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterFactory;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterFactoryException;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.spider.data.SpiderReadResult;

public class FedoraReaderImp implements FedoraReader {
	private FedoraReaderConverterFactory fedoraReaderConverterFactory;
	private HttpHandlerFactory httpHandlerFactory;
	private XMLXPathParserFactory xmlxPathParserFactory;
	private CoraLogger logger;

	public FedoraReaderImp(FedoraReaderConverterFactory fedoraReaderConverterFactory,
			HttpHandlerFactory httpHandlerFactory, XMLXPathParserFactory xmlxPathParserFactory,
			CoraLogger logger) {
		this.fedoraReaderConverterFactory = fedoraReaderConverterFactory;
		this.httpHandlerFactory = httpHandlerFactory;
		this.xmlxPathParserFactory = xmlxPathParserFactory;
		this.logger = logger;
	}

	@Override
	public DataElement read(String type, String id) {
		try {
			var converter = fedoraReaderConverterFactory.factor(type);
			converter.setLogger(logger);
			var requestUrl = converter.getQueryForObjectId(id);
			XMLXPathParser parser = tryGetXmlXPathParserFromFedora(requestUrl);
			var objectConverter = converter.getConverter();
			objectConverter.loadXml(parser);
			return objectConverter.convert();
		} catch (FedoraReaderConverterFactoryException | FedoraReaderConverterException e) {
			log(e.getMessage());
		}
		return null;
	}

	private void log(String message) {
		if (logger != null) {
			logger.write(message);
		} else {
			throw new RuntimeException(message);
		}
	}

	private XMLXPathParser tryGetXmlXPathParserFromFedora(String requestUrl) {
		var responseXml = getResponseXMLForRequest(requestUrl);
		return getParserForResponseXML(responseXml);
	}

	private String getResponseXMLForRequest(String objectUrl) {
		return httpHandlerFactory.factor(objectUrl)
			.getResponseText();
	}

	private XMLXPathParser getParserForResponseXML(String responseXml) {
		XMLXPathParser xmlXPathParser = null;
		try {
			xmlXPathParser = xmlxPathParserFactory.factor()
				.forXML(responseXml);
		} catch (XMLXPathParserException e) {
			log(e.getMessage());
		}
		return xmlXPathParser;
	}

	@Override
	public SpiderReadResult readList(String type, DataGroup filter) throws FedoraReaderException {
		try {
			long start = Long
				.parseLong(filter.getFirstAtomicValueWithNameInDataOrDefault("start", "1")) - 1;
			boolean limitNumberOfRows = filter.containsChildWithNameInData("rows");
			FedoraReadPositionConverter fedoraReadPositionConverter;
			if (limitNumberOfRows) {
				long rows = Integer
					.parseInt(filter.getFirstAtomicValueWithNameInDataOrDefault("rows", "1"));
				fedoraReadPositionConverter = fedoraReaderConverterFactory.factor(type, start,
						start + rows);
			} else {
				fedoraReadPositionConverter = fedoraReaderConverterFactory.factor(type, start);
			}
			return getSpiderReadResult(fedoraReadPositionConverter, filter);
		} catch (FedoraReaderConverterFactoryException e) {
			throw new FedoraReaderException(e.getMessage(), e);
		}
	}

	private SpiderReadResult getSpiderReadResult(FedoraReadPositionConverter converter,
			DataGroup filter) {
		var spiderReadResult = createEmptySpiderReadResult();
		return getSpiderReadResultForConverter(converter, filter, null, spiderReadResult);
	}

	private SpiderReadResult getSpiderReadResultForConverter(FedoraReadPositionConverter converter,
			DataGroup filter, FedoraReaderCursor cursor, SpiderReadResult spiderReadResult) {
		List<String> pidList = new ArrayList<>();
		FedoraReaderCursor nextCursor = null;
		try {
			var pidListAndCursor = getFedoraReaderPidListWithOptionalCursorFrom(converter, filter,
					cursor);
			pidList = pidListAndCursor.getPidList();
			nextCursor = pidListAndCursor.getCursor();
		} catch (XMLXPathParserException e) {
			log(e.getMessage());
		}
		int fedoraReadLength = pidList.size();

		var pidListToConvert = converter.filterPidList(spiderReadResult.totalNumberOfMatches,
				pidList);
		List<DataGroup> result = new ArrayList<>();
		for (var pid : pidListToConvert) {
			var pidUrl = converter.getQueryForObjectId(pid);
			result.add(getDataGroupForFedoraPid(converter.getConverter(), pidUrl));
		}

		// TODO: handle failing stuff with :
		// result.stream().filter(Objects::nonNull).collect(Collectors.toList())
		spiderReadResult.listOfDataGroups.addAll(result);
		spiderReadResult.totalNumberOfMatches += fedoraReadLength;

		if (isAValidCursor(nextCursor)) {
			getSpiderReadResultForConverter(converter, filter, nextCursor, spiderReadResult);
		}
		return spiderReadResult;
	}

	private FedoraReaderPidListWithOptionalCursor getFedoraReaderPidListWithOptionalCursorFrom(
			FedoraReadPositionConverter converter, DataGroup filter, FedoraReaderCursor cursor)
			throws XMLXPathParserException {
		var fedoraReaderXmlHelper = xmlxPathParserFactory.factorHelper();
		String requestQuery;
		if (cursor == null) {
			requestQuery = converter.getQueryForList(filter);
		} else {
			requestQuery = converter.getQueryForList(filter, cursor);
		}
		XMLXPathParser parserPidList = tryGetXmlXPathParserFromFedora(requestQuery);
		return fedoraReaderXmlHelper.extractPidListAndPossiblyCursor(parserPidList);
	}

	private DataGroup getDataGroupForFedoraPid(FedoraReaderConverter converter, String pidUrl) {
		XMLXPathParser xmlForPid = tryGetXmlXPathParserFromFedora(pidUrl);
		converter.loadXml(xmlForPid);
		return tryConvertXML(converter);
	}

	private DataGroup tryConvertXML(FedoraReaderConverter converter) {
		try {
			return converter.convert();
		} catch (FedoraReaderConverterException e) {
			logger.write(e.getMessage());
		}
		return null;
	}

	private SpiderReadResult createEmptySpiderReadResult() {
		SpiderReadResult result = new SpiderReadResult();
		result.listOfDataGroups = new ArrayList<>();
		result.totalNumberOfMatches = 0;
		return result;
	}

	private boolean isAValidCursor(FedoraReaderCursor cursor) {
		return cursor != null && cursor.getToken() != null;
	}

}
