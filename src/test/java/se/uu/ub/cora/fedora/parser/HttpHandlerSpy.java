/*
 * Copyright 2018 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.fedora.parser;

import java.io.InputStream;
import java.util.Map;
import java.util.Stack;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class HttpHandlerSpy implements HttpHandler {
	public MethodCallRecorder MCR = new MethodCallRecorder();

	public Stack<String> urlCall = new Stack<>();
	public Stack<String> urlCallResponseText = new Stack<>();
	public Stack<Integer> urlCallResponseCode = new Stack<>();

	void wasCalledWith(String url) {
		urlCall.push(url);
	}

	public void addQueryResponse(String a, Map<Integer, String> b, Map<Integer, Integer> c, int d) {
	}

	public int getUrlCountCallFor(String a) {
		return 0;
	}

	@Override
	public void setRequestMethod(String requestMethod) {
		MCR.addCall("requestMethod", requestMethod);
	}

	@Override
	public String getResponseText() {
		MCR.addCall();
		if (urlCallResponseText.isEmpty()) {
			if (!urlCall.isEmpty()) {
				MCR.addReturned(urlCall.peek() + " xml response");
				return urlCall.peek() + " xml response";
			}
			MCR.addReturned("someHttpResponse");
			return "someHttpResponse";
		}
		String pop = urlCallResponseText.pop();
		MCR.addReturned(pop);
		return pop;
	}

	@Override
	public int getResponseCode() {
		MCR.addCall();
		if (urlCallResponseCode.isEmpty()) {
			MCR.addReturned(200);
			return 200;
		}
		Integer pop = urlCallResponseCode.pop();
		MCR.addReturned(pop);
		return pop;
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

	@Override
	public void setBasicAuthorization(String username, String password) {
		// TODO Auto-generated method stub

	}
}
