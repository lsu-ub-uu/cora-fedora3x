package se.uu.ub.cora.fedora.data;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NodeList;

public class FedoraReaderXmlHelperImp implements FedoraReaderXmlHelper {
	private XMLXPathParserFactory xmlXPathParserFactory;

	@Override
	public FedoraReaderPidListWithOptionalCursor extractPidListAndPossiblyCursor(XMLXPathParser xmlxPathParser) throws XMLXPathParserException {
		return new FedoraReaderPidListWithOptionalCursor(extractPidList(xmlxPathParser), extractCursor(xmlxPathParser));
	}

	@Override
	public FedoraReaderPidListWithOptionalCursor extractPidListAndPossiblyCursor(String xml) throws XMLXPathParserException {
		var xmlXPathParsers = xmlXPathParserFactory.factor().forXML(xml);
		return new FedoraReaderPidListWithOptionalCursor(extractPidList(xmlXPathParsers), extractCursor(xmlXPathParsers));
	}

	@Override
	public void setXmlXPathParseFactory(XMLXPathParserFactory xmlXPathParserFactory) {
		this.xmlXPathParserFactory = xmlXPathParserFactory;
	}

	@Override
	public XMLXPathParserFactory getXmlXPathParseFactory() {
		return xmlXPathParserFactory;
	}

	private static List<String> extractPidList(XMLXPathParser xmlxPathParser)
			throws XMLXPathParserException {
		throwIfResultListIsMissing(xmlxPathParser);
		var nodeList = xmlxPathParser
			.getNodeListFromDocumentUsingXPath("/result/resultList/objectFields/pid/text()");
		return getPidListFromNodeList(nodeList);
	}

	private static void throwIfResultListIsMissing(XMLXPathParser xmlxPathParser)
			throws XMLXPathParserException {
		if (!xmlxPathParser.hasNode("/result/resultList")) {
			throw new XMLXPathParserException("There was no resultList in given XML");
		}
	}

	private static List<String> getPidListFromNodeList(NodeList nodeList) {
		var result = new ArrayList<String>();
		for (int idx = 0; idx < nodeList.getLength(); idx++) {
			var node = nodeList.item(idx);
			result.add(node.getNodeValue());
		}
		return result;
	}


	private static FedoraReaderCursor extractCursor(XMLXPathParser xmlxPathParser)
			throws XMLXPathParserException {
		FedoraReaderCursor fedoraReaderCursor = null;
		if (xmlxPathParser.hasNode("/result/listSession")) {
			fedoraReaderCursor = tryGetFedoraReaderCursor(xmlxPathParser);
		}
		return fedoraReaderCursor;
	}

	private static FedoraReaderCursor tryGetFedoraReaderCursor(XMLXPathParser xmlxPathParser)
			throws XMLXPathParserException {
		FedoraReaderCursor fedoraReaderCursor = getFedoraReaderCursorWithToken(xmlxPathParser);
		String cursor = tryGetCursorFromXml(xmlxPathParser);
		fedoraReaderCursor.setCursor(cursor);
		return fedoraReaderCursor;
	}

	private static FedoraReaderCursor getFedoraReaderCursorWithToken(XMLXPathParser xmlxPathParser)
			throws XMLXPathParserException {
		var token = xmlxPathParser
			.getStringFromDocumentUsingXPath("/result/listSession/token/text()");
		throwIfRequiredElementNotFound(token, "token not found in XML");
		return new FedoraReaderCursor(token);
	}

	private static void throwIfRequiredElementNotFound(String element, String message)
			throws XMLXPathParserException {
		if (element.isEmpty()) {
			throw new XMLXPathParserException(message);
		}
	}

	private static String tryGetCursorFromXml(XMLXPathParser xmlxPathParser)
			throws XMLXPathParserException {
		var cursor = xmlxPathParser
			.getStringFromDocumentUsingXPath("/result/listSession/cursor/text()");
		throwIfRequiredElementNotFound(cursor, "cursor not found in XML");
		return cursor;
	}
}
