package se.uu.ub.cora.fedora.data;
import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpMultiPartUploader;

public class HttpHandlerFactorySpy implements HttpHandlerFactory {
    public Map<String, Integer> urlCalls = new HashMap<>();
    public Map<String, HttpHandler> urlHandlers = new HashMap<>();
    public int factoredHttpHandlers = 0;
    public String responseText = "someXml";
    public Map<String,Integer> totalNumberOfItemsOfType = new HashMap<>();

    public HttpHandlerSpy getSpyFor(String url) {
        if(urlCalls.containsKey(url)) {
            return (HttpHandlerSpy) urlHandlers.get(url);
        }
        return null;
    }

    @Override
    public HttpHandler factor(String url) {
        if(urlHandlers.containsKey(url)) {
            factoredHttpHandlers++;
            int counts = 0;
            if(urlCalls.containsKey(url)) {
                counts = urlCalls.get(url);
            }
            urlCalls.put(url, counts);
            HttpHandlerSpy httpHandler = (HttpHandlerSpy) urlHandlers.get(url);
            httpHandler.wasCalled = true;
            return httpHandler;
        }
        throw new RuntimeException("HttpHandlerFactory did not have a handler for: " + url);
    }

    @Override
    public HttpMultiPartUploader factorHttpMultiPartUploader(String url) {
        // TODO Auto-generated method stub
        return null;
    }

}