package se.uu.ub.cora.fedora.reader.internal;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpMultiPartUploader;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class HttpHandlerFactorySpy2 implements HttpHandlerFactory {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public boolean throwError = false;
	public int[] returnCode = { 200 };

	@Override
	public HttpHandler factor(String url) {
		MCR.addCall("url", url);
		if (throwError) {
			throw new RuntimeException("Error from HttpHandlerFactory factor");
		}
		HttpHandlerSpy2 spy = new HttpHandlerSpy2();
		spy.returnCode = getReturnCodeToSetInSpy();
		MCR.addReturned(spy);
		return spy;
	}

	private int getReturnCodeToSetInSpy() {
		int numberOfCallsToMethod = MCR.getNumberOfCallsToMethod("factor");
		if (returnCode.length < numberOfCallsToMethod) {
			return returnCode[returnCode.length - 1];
		}
		return returnCode[numberOfCallsToMethod - 1];
	}

	@Override
	public HttpMultiPartUploader factorHttpMultiPartUploader(String url) {
		// TODO Auto-generated method stub
		return null;
	}

}
