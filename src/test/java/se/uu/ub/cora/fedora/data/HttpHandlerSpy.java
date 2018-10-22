package se.uu.ub.cora.fedora.data;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.httphandler.HttpHandler;

public class HttpHandlerSpy implements HttpHandler {
    public Map<String, Integer> urlCalls = new HashMap<>();
    public Map<String, String> urlResponse = new HashMap<>();
    private String responseText;

    public int getUrlCountCallFor(String query) {
        return urlCalls.get(query);
    }

    public void addQueryResponse(String query, String response) {
        urlCalls.put(query, 0);
        urlResponse.put(query, response);
    }

    public boolean allWasCalledOnce() {
        return urlCalls.entrySet().stream().allMatch(itm -> itm.getValue() > 0);
    }

    void wasCalledWith(String url)
    {
        updateUrlCounter(url);
        responseText = urlResponse.get(url);
    }

    private void updateUrlCounter(String url) {
        if(urlCalls.containsKey(url)) {
            urlCalls.put(url, urlCalls.get(url) + 1);
        } else {
            urlCalls.put(url, 1);
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
        // TODO Auto-generated method stub
        return 0;
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
