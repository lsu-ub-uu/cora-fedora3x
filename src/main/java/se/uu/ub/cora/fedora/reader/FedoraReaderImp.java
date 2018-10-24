package se.uu.ub.cora.fedora.reader;

import se.uu.ub.cora.bookkeeper.data.DataElement;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.CoraLogger;
import se.uu.ub.cora.fedora.reader.converter.*;
import se.uu.ub.cora.fedora.data.*;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.spider.data.SpiderReadResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FedoraReaderImp implements FedoraReader {
    private FedoraReaderConverterFactory fedoraReaderConverterFactory;
    private HttpHandlerFactory httpHandlerFactory;
    private XMLXPathParserFactory xmlxPathParserFactory;
    private CoraLogger logger;

    public FedoraReaderImp(FedoraReaderConverterFactory fedoraReaderConverterFactory, HttpHandlerFactory httpHandlerFactory, XMLXPathParserFactory xmlxPathParserFactory, CoraLogger logger) {
        this.fedoraReaderConverterFactory = fedoraReaderConverterFactory;
        this.httpHandlerFactory = httpHandlerFactory;
        this.xmlxPathParserFactory = xmlxPathParserFactory;
        this.logger = logger;
    }

    @Override
    public DataElement read(String type, String id) throws FedoraReaderException {
        try {
            var converter = fedoraReaderConverterFactory.factorConverter(type);
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

    @Override
    public SpiderReadResult readList(String type, DataGroup filter) throws FedoraReaderException {
        try {
            var converter = fedoraReaderConverterFactory.factorConverter(type);
            long start = Long.parseLong(filter.getFirstAtomicValueWithNameInDataOrDefault("start", "1")) - 1;
            return getSpiderReadResult(filter, converter, pid -> getDataGroupForFedoraPid(converter, pid), start);
        } catch (FedoraReaderConverterFactoryException | XMLXPathParserException | FedoraReaderConverterException e) {
            throw new FedoraReaderException(e.getMessage(), e);
        }
    }

    private SpiderReadResult getSpiderReadResult(DataGroup filter, FedoraReaderConverter converter, Function<String, DataGroup> converterLambda, long start) throws XMLXPathParserException, FedoraReaderConverterException {
        Function<Long, Function<String, DataGroup>> conditionalConverterFunction = getConditionalConverterFunction(filter, converterLambda, start);
        var spiderReadResult = createEmptySpiderReadResult();
        return getSpiderReadResultForConverter(null, spiderReadResult, filter, converter, conditionalConverterFunction);
    }

    private Function<Long, Function<String, DataGroup>> getConditionalConverterFunction(DataGroup filter, Function<String, DataGroup> converterLambda, long start) {
        Function<Long, Function<String, DataGroup>> conditionalConverterFunction;
        boolean limitNumberOfRows = filter.containsChildWithNameInData("rows");
        if (limitNumberOfRows) {
            long rows = Integer.parseInt(filter.getFirstAtomicValueWithNameInDataOrDefault("rows", "1"));
            conditionalConverterFunction =
                    FedoraReadPositionConverter.convertFromStartToStop(start, start + rows, converterLambda);
        } else {
            conditionalConverterFunction =
                    FedoraReadPositionConverter.convertFromStart(start, converterLambda);
        }
        return conditionalConverterFunction;
    }

    private DataGroup getDataGroupForFedoraPid(FedoraReaderConverter converter, String pid) {
        String pidUrl = tryGetUrlForPid(converter, pid);
        XMLXPathParser xmlForPid = tryGetXmlXPathParser(pidUrl);
        converter.loadXml(xmlForPid);
        return tryConvertXML(converter);
    }


    private DataGroup tryConvertXML(FedoraReaderConverter converter) {
        DataGroup result = null;
        try {
            result = converter.convert();
        } catch (FedoraReaderConverterException e) {
            logger.write(e.getMessage());
        }
        return result;
    }

    private XMLXPathParser tryGetXmlXPathParser(String pidUrl) {
        XMLXPathParser xmlForPid = null;
        try {
            xmlForPid = tryGetXmlXPathParserFromFedora(pidUrl);
        } catch (XMLXPathParserException e) {
            logger.write(e.getMessage());
        }
        return xmlForPid;
    }

    private String tryGetUrlForPid(FedoraReaderConverter converter, String pid) {
        String pidUrl = null;
        try {
            pidUrl = converter.getQueryForObjectId(pid);
        } catch (FedoraReaderConverterException e) {
            logger.write(e.getMessage());
        }
        return pidUrl;
    }

    private SpiderReadResult getSpiderReadResultForConverter(FedoraReaderCursor cursor, SpiderReadResult spiderReadResult, DataGroup filter, FedoraReaderConverter converter, Function<Long, Function<String, DataGroup>>  conditionalConverter) throws XMLXPathParserException, FedoraReaderConverterException {

        var pidListAndCursor = getFedoraReaderPidListWithOptionalCursorFromBlah(filter, converter, cursor);

        var pidList = pidListAndCursor.getPidList();
        int fedoraReadLength = pidList.size();


        var pos = spiderReadResult.totalNumberOfMatches;
        List<DataGroup> result = new ArrayList<>();
        for (var elem : pidList) {
            var converterForElem = conditionalConverter.apply(pos);
            if (converterForElem != null) {
                result.add(converterForElem.apply(elem));
            }
            pos++;
        }

// TODO: handle failing stuff with : result.stream().filter(Objects::nonNull).collect(Collectors.toList())
        spiderReadResult.listOfDataGroups.addAll(result);
        spiderReadResult.totalNumberOfMatches += fedoraReadLength;

        if (isAValidCursor(pidListAndCursor.getCursor())) {
            getSpiderReadResultForConverter(pidListAndCursor.getCursor(), spiderReadResult, filter, converter, conditionalConverter);
        }
        return spiderReadResult;
    }

    private FedoraReaderPidListWithOptionalCursor getFedoraReaderPidListWithOptionalCursorFromBlah(DataGroup filter, FedoraReaderConverter converter, FedoraReaderCursor cursor) throws XMLXPathParserException {
        var fedoraReaderXmlHelper = xmlxPathParserFactory.factorHelper();
        String requestQuery;
        if(cursor == null) {
            requestQuery = converter.getQueryForList(filter);
        } else {
            requestQuery = converter.getQueryForList(filter, cursor);
        }

        XMLXPathParser parserPidList = tryGetXmlXPathParserFromFedora(requestQuery);
        return fedoraReaderXmlHelper.extractPidListAndPossiblyCursor(parserPidList);
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
