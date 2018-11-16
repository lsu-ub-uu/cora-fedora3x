package se.uu.ub.cora.fedora.data;

import se.uu.ub.cora.httphandler.HttpHandler;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpHandlerSpy implements HttpHandler {
    public Map<String, Integer> urlCallCount = new HashMap<>();
    public Map<String, Map<Integer, Integer>> urlResponseCodeForCallCount = new HashMap<>();
    public Map<String, Map<Integer, String>> urlResponseForCallCount = new HashMap<>();
    private String responseText;
    private int responseCode;

    public int getUrlCountCallFor(String query) {
        return urlCallCount.get(query);
    }

    public void addQueryResponse(String query, Map<Integer, String> responses, Map<Integer, Integer> responseCodes, int expectedNumberOfCalls) {
        urlCallCount.put(query, expectedNumberOfCalls);
        urlResponseCodeForCallCount.put(query, responseCodes);
        urlResponseForCallCount.put(query, responses);
    }

    public boolean allCallsAccountedFor() {
        return urlCallCount.entrySet().stream().allMatch(itm -> itm.getValue() == 0);
    }

    void wasCalledWith(String url) {
        updateUrlCounter(url);

        if (urlResponseForCallCount.containsKey(url)) {
            Map<Integer, String> callCountResponse = urlResponseForCallCount.get(url);
            int callCount = urlCallCount.get(url);
            if (callCountResponse.containsKey(callCount)) {
                responseText = callCountResponse.get(callCount);
            } else {
                System.err.println("HttpHandlerSpy::urlResponseForCallCount does not contain key: " + url + " and call count" + callCount);
                responseText = "someDefaultResponse";
            }

        } else {
            System.err.println("HttpHandlerSpy::urlResponseForCallCount does not contain key: " + url);
            responseText = "someDefaultResponse";
        }

        if (urlResponseCodeForCallCount.containsKey(url)) {
            Map<Integer, Integer> callCountResponseCode = urlResponseCodeForCallCount.get(url);
            int callCount = urlCallCount.get(url);
            if (callCountResponseCode.containsKey(callCount)) {
                responseCode = callCountResponseCode.get(callCount);
            } else {
                System.err.println("HttpHandlerSpy::urlResponseCodeForCallCount does not contain key: " + url + " and call count" + callCount);
                responseCode = 418;
            }
        } else {
            System.err.println("HttpHandlerSpy::urlResponseCodeForCallCount does not contain key: " + url);
            responseCode = 200;
        }

    }


    private void updateUrlCounter(String url) {
        if (urlCallCount.containsKey(url)) {
            urlCallCount.put(url, urlCallCount.get(url) - 1);
        } else {
            urlCallCount.put(url, 0);
        }
    }

    @Override
    public void setRequestMethod(String requestMethod) {
    }

    @Override
    public String getResponseText() {
        return responseText;
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public void setOutput(String outputString) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setRequestProperty(String key, String value) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getErrorText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setStreamOutput(InputStream stream) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getHeaderField(String name) {
        // TODO Auto-generated method stub
        return null;
    }
}
