package se.uu.ub.cora.fedora.data;

import se.uu.ub.cora.httphandler.HttpHandler;

import java.io.InputStream;
import java.util.*;

public class HttpHandlerSpy implements HttpHandler {
    public Stack<String> urlCall = new Stack<>();
    public Stack<String> urlCallResponseText = new Stack<>();
    public Stack<Integer> urlCallResponseCode = new Stack<>();

    void wasCalledWith(String url) {
        urlCall.push(url);
    }
    public void addQueryResponse(String a, Map<Integer, String> b, Map<Integer, Integer>c, int d) {}
    public int getUrlCountCallFor(String a) { return 0; }

    @Override
    public void setRequestMethod(String requestMethod) {
    }

    @Override
    public String getResponseText() {
        if(urlCallResponseText.isEmpty()) {
            if(!urlCall.isEmpty()) {
                return urlCall.peek();
            }
            return "someHttpResponse";
        }
        return urlCallResponseText.pop();
    }

    @Override
    public int getResponseCode() {
        if(urlCallResponseCode.isEmpty()) {
            return 200;
        }
        return urlCallResponseCode.pop();
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
