package se.uu.ub.cora.fedora.reader;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.fedora.parser.FedoraReaderXmlHelper;
import se.uu.ub.cora.fedora.parser.ListSession;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class FedoraReaderXmlHelperSpy2 implements FedoraReaderXmlHelper {
	public MethodCallRecorder MCR = new MethodCallRecorder();

	public List<String> pidList;

	public int noOfCallsToGetSessionsBeforeNoSession = 0;

	public FedoraReaderXmlHelperSpy2() {
		pidList = new ArrayList<String>();
		pidList.add("SomePid");
		pidList.add("SomeOtherPid");
	}

	@Override
	public ListSession getSession(String responseXML) {
		MCR.addCall("xml", responseXML);
		String token = "someToken";
		int numberOfCallsToMethod = MCR.getNumberOfCallsToMethod("getSession");
		ListSession session = ListSession
				.createListSessionUsingToken(token + numberOfCallsToMethod);

		if (numberOfCallsToMethod > noOfCallsToGetSessionsBeforeNoSession) {
			session = ListSession.createListSessionNoMoreResults();
		}

		MCR.addReturned(session);
		return session;
	}

	@Override
	public List<String> getPidList(String responseXML) {
		MCR.addCall("xml", responseXML);
		MCR.addReturned(pidList);

		return pidList;
	}

}
