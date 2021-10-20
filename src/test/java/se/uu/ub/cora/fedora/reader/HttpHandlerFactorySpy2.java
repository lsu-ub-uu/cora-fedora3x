package se.uu.ub.cora.fedora.reader;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpMultiPartUploader;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class HttpHandlerFactorySpy2 implements HttpHandlerFactory {
	public MethodCallRecorder MCR = new MethodCallRecorder();

	@Override
	public HttpHandler factor(String url) {
		MCR.addCall("url", url);
		HttpHandlerSpy2 spy = new HttpHandlerSpy2();
		MCR.addReturned(spy);
		return spy;
	}

	@Override
	public HttpMultiPartUploader factorHttpMultiPartUploader(String url) {
		// TODO Auto-generated method stub
		return null;
	}

}
