/*
 * Copyright 2020 Uppsala University Library
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
package se.uu.ub.cora.fedora.reader.internal;

import java.io.InputStream;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class HttpHandlerSpy2 implements HttpHandler {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public int returnCode = 200;

	@Override
	public void setRequestMethod(String requestMetod) {
		MCR.addCall("requestMetod", requestMetod);
		// TODO Auto-generated method stub

	}

	@Override
	public String getResponseText() {
		MCR.addCall();
		String out = "Some response text from spy";
		MCR.addReturned(out);
		return out;
	}

	@Override
	public int getResponseCode() {
		MCR.addCall();
		MCR.addReturned(returnCode);
		return returnCode;
	}

	@Override
	public void setOutput(String outputString) {
		MCR.addCall("outputString", outputString);
	}

	@Override
	public void setRequestProperty(String key, String value) {
		MCR.addCall("key", key, "value", value);
	}

	@Override
	public String getErrorText() {
		MCR.addCall();
		String out = "Some error text from spy";
		MCR.addReturned(out);
		return out;
	}

	@Override
	public void setStreamOutput(InputStream stream) {
		MCR.addCall("stream", stream);
	}

	@Override
	public String getHeaderField(String name) {
		MCR.addCall("name", name);
		String out = "Some header from spy";
		MCR.addReturned(out);
		return out;
	}

	@Override
	public void setBasicAuthorization(String username, String password) {
		MCR.addCall("username", username, "password", password);
	}

}
