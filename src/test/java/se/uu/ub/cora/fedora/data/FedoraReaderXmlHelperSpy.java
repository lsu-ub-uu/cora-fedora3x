package se.uu.ub.cora.fedora.data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FedoraReaderXmlHelperSpy implements FedoraReaderXmlHelper {
    public List<String> pidList;

    public boolean failPidExtraction;

    public FedoraReaderXmlHelperSpy() {
        failPidExtraction = false;
    }

    public void addPidListForXml(String xml, boolean hasCursor, List<String> pidList) {
    }

    @Override
    public FedoraReaderCursor getCursorIfAvailable(String xml) {
        FedoraReaderCursor fedoraReaderCursor = null;
        if(pidList == null) {
            fedoraReaderCursor = new FedoraReaderCursor("someUninitializedToken");
        } else {
            if (!pidList.isEmpty()) {
                fedoraReaderCursor = new FedoraReaderCursor("someToken");
            }
        }
        return fedoraReaderCursor;
    }

    @Override
    public List<String> getPidList(String xml) {
        if(failPidExtraction) {
            throw new RuntimeException("Bad XML: " + xml);
        }
        Pattern p = Pattern.compile(".+maxResults=(\\d+).+");
        Matcher m = p.matcher(xml);
        if(m.find()) {
            int maxResults = Integer.parseInt(m.group(1));
            if(pidList == null || pidList.size() < maxResults) {
                List<String> madeUpPidList = new ArrayList<>();
                for(int idx = 0; idx < maxResults; idx++) {
                    madeUpPidList.add("generatedPid:" + idx);
                }
                return madeUpPidList;
            }
            List<String> requestedPidList = pidList.subList(0, maxResults);
            pidList = pidList.subList(maxResults, pidList.size());
            return requestedPidList;
        }
        return List.of();
    }

    @Override
    public void setXmlXPathParseFactory(XMLXPathParserFactory xmlXPathParserFactory) {

    }

    @Override
    public XMLXPathParserFactory getXmlXPathParseFactory() {
        return null;
    }

}
