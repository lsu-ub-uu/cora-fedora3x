package se.uu.ub.cora.fedora.data;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.httphandler.HttpHandler;

public class HttpHandlerSpy implements HttpHandler {
    public Map<String, Integer> urlCalls = new HashMap<>();
    public Map<String, String> urlResponse = new HashMap<>();
    public Map<String, Integer> urlResponseCode = new HashMap<>();

    private String responseText;
    private int responseCode;

    public int getUrlCountCallFor(String query) {
        return urlCalls.get(query);
    }

    public void addQueryResponse(String query, String response, int responseCode, int expectedNumberOfCalls) {
        urlCalls.put(query, expectedNumberOfCalls);
        urlResponseCode.put(query, responseCode);
        urlResponse.put(query, response);
    }

    public void addQueryResponse(String query, String response, int expectedNumberOfCalls) {
        urlCalls.put(query, expectedNumberOfCalls);
        urlResponseCode.put(query, 200);
        urlResponse.put(query, response);
    }

    public boolean allCallsAccountedFor() {
        return urlCalls.entrySet().stream().allMatch(itm -> itm.getValue() == 0);
    }

    void wasCalledWith(String url)
    {
        updateUrlCounter(url);
        responseText = urlResponse.get(url);
        responseCode = urlResponseCode.get(url);
    }

    private void updateUrlCounter(String url) {
        if(urlCalls.containsKey(url)) {
            urlCalls.put(url, urlCalls.get(url) - 1);
        } else {
            urlCalls.put(url, 0);
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
