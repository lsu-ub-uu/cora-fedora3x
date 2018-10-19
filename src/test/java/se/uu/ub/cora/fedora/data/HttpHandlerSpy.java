package se.uu.ub.cora.fedora.data;

import java.io.InputStream;
import java.util.Map;

import se.uu.ub.cora.httphandler.HttpHandler;

public class HttpHandlerSpy implements HttpHandler {

    public String requestMethod;
    public String responseText;
    public boolean wasCalled = false;
    @Override
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
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
