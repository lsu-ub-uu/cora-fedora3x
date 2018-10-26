package se.uu.ub.cora.fedora.data;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpMultiPartUploader;

public class HttpHandlerFactorySpy implements HttpHandlerFactory {

    public boolean couldNotFactorForUrl = false;
    public int factoredHttpHandlers = 0;
    public HttpHandlerSpy httpHandlerSpy;

    @Override
    public HttpHandler factor(String url) {
        factoredHttpHandlers++;
        if(couldNotFactorForUrl) {
            throw new RuntimeException("HttpHandlerFactory did not have a handler for: " + url);
        }
        httpHandlerSpy.wasCalledWith(url);
        return httpHandlerSpy;
    }

    @Override
    public HttpMultiPartUploader factorHttpMultiPartUploader(String url) {
        // TODO Auto-generated method stub
        return null;
    }

}