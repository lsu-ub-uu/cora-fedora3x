package se.uu.ub.cora.fedora.reader;

import se.uu.ub.cora.bookkeeper.data.DataElement;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterException;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterFactory;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterFactoryException;
import se.uu.ub.cora.fedora.data.*;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.spider.data.SpiderReadResult;

import java.util.ArrayList;

public class FedoraReaderImp implements FedoraReader {
    private FedoraReaderConverterFactory fedoraReaderConverterFactory;
    private HttpHandlerFactory httpHandlerFactory;
    private XMLXPathParserFactory xmlxPathParserFactory;

    public FedoraReaderImp(FedoraReaderConverterFactory fedoraReaderConverterFactory, HttpHandlerFactory httpHandlerFactory, XMLXPathParserFactory xmlxPathParserFactory) {
        this.fedoraReaderConverterFactory = fedoraReaderConverterFactory;
        this.httpHandlerFactory = httpHandlerFactory;
        this.xmlxPathParserFactory = xmlxPathParserFactory;
    }

    @Override
    public DataElement read(String type, String id) throws FedoraReaderException {
        try {
            var converter = fedoraReaderConverterFactory.factor(type);
            var requestUrl = converter.getQueryForObjectId(id);
            XMLXPathParser parser = tryGetXmlXPathParserFromFedora(requestUrl);
            converter.loadXml(parser);
            return converter.convert();
        } catch (FedoraReaderConverterFactoryException | XMLXPathParserException | FedoraReaderConverterException e) {
            throw new FedoraReaderException(e.getMessage(), e);
        }
    }

    private XMLXPathParser tryGetXmlXPathParserFromFedora(String requestUrl) throws XMLXPathParserException {
        var responseXml = getResponseXMLForRequest(requestUrl);
        return getParserForResponseXML(responseXml);
    }

    private String getResponseXMLForRequest(String objectUrl) {
        return httpHandlerFactory.factor(objectUrl).getResponseText();
    }

    private XMLXPathParser getParserForResponseXML(String responseXml) throws XMLXPathParserException {
        return xmlxPathParserFactory.factor().forXML(responseXml);
    }

//
//    FedoraReaderCursor readStuff(String query) {
//        return null;
//    }
//    FedoraReaderCursor readStuff(FedoraReaderCursor cursor, String query) {
//        return null;
//    }

    @Override
    public SpiderReadResult readList(String type, DataGroup filter) throws FedoraReaderException {
        try {
            var converter = fedoraReaderConverterFactory.factor(type);

            var requestQuery = converter.getQueryForList(filter);
            XMLXPathParser parserPidList = tryGetXmlXPathParserFromFedora(requestQuery);
            var fedoraReaderXmlHelper = xmlxPathParserFactory.factorHelper();
            var pidListAndCursor = fedoraReaderXmlHelper.extractPidListAndPossiblyCursor(parserPidList);



//            var cursor = readStuff(requestQuery);
//            while (cursor != null) {
//                cursor = readStuff(cursor, requestQuery);
//            }
            /// parse

            /// if cursor -> pidListAndCursor = callForMore

            SpiderReadResult result = new SpiderReadResult();
            var pidList = pidListAndCursor.getPidList();
            result.listOfDataGroups = new ArrayList<>();
            if (!pidList.isEmpty()) {
                for (var pid : pidList) {
                    var pidUrl = converter.getQueryForObjectId(pid);
                    var xmlForPid = tryGetXmlXPathParserFromFedora(pidUrl);
                    converter.loadXml(xmlForPid);
                    result.listOfDataGroups.add(converter.convert());
                }
            }
            result.totalNumberOfMatches = pidList.size(); // getTotalNumberOfMatchesFromXml(parserPidList);
            if(pidListAndCursor.getCursor() != null && pidListAndCursor.getCursor().getToken() != null) {
                var continueFindObjects = converter.getQueryForList(filter, pidListAndCursor.getCursor());
                XMLXPathParser parserPidList1 = tryGetXmlXPathParserFromFedora(continueFindObjects);
                var pidListAndCursor1 = fedoraReaderXmlHelper.extractPidListAndPossiblyCursor(parserPidList1);
                result.totalNumberOfMatches += pidListAndCursor1.getPidList().size();
            }
//            result.totalNumberOfMatches = result.listOfDataGroups.size();
//      result.totalNumberOfMatches += getRemainingNumberOfItemsInStorageFromCursor(pidListAndCursor.getCursor());
//            var totalNumberOfMatches = 0;
//            var start = requestParameters.getStart();
//            var end = start + requestParameters.getRows();
//            var index = totalNumberOfMatches;
//            while (index < pidListAndCursor.getPidList().size() && index + totalNumberOfMatches >= start && index + totalNumberOfMatches < end) {
//                var pidUrl = converter.getQueryForObjectId(pid);
//                var xmlForPid = tryGetXmlXPathParserFromFedora(pidUrl);
//                converter.loadXml(xmlForPid);
//                result.listOfDataGroups.add(converter.convert());
//            }
//
//            totalNumberOfMatches = pidListAndCursor.getPidList().size();
//
//            var cursor = pidListAndCursor.getCursor();
//            while (cursor != null) {
//                // request more
//                var moreStuff = tryGetXmlXPathParserFromFedora(requestParameters.getQuery());
//                var morePidsAndPossiblyCursors = fedoraReaderXmlHelper.extractPidListAndPossiblyCursor(moreStuff);
//
//                totalNumberOfMatches += morePidsAndPossiblyCursors.getPidList().size();
//                cursor = morePidsAndPossiblyCursors.getCursor();
//            }
//

            // q: start, rows
            // pidList -> size
            // hasCursor -> more to read
            // no cursor, total is read


            return result;
        } catch (FedoraReaderConverterFactoryException | XMLXPathParserException | FedoraReaderConverterException e) {
            throw new FedoraReaderException(e.getMessage(), e);
        }
    }

//    private long getRemainingNumberOfItemsInStorageFromCursor(FedoraReaderCursor cursor) {
//        //TODO: This should really, really be non-blocking.
//        if(cursor == null) {
//            return 0;
//        }
//        return 0;
//    }
}
