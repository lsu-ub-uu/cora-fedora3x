package se.uu.ub.cora.fedora.reader;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.fedora.data.FedoraListSession;
import se.uu.ub.cora.fedora.data.FedoraReaderXmlHelper;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class FedoraReaderXmlHelperSpy2 implements FedoraReaderXmlHelper {
	public MethodCallRecorder MCR = new MethodCallRecorder();

	public List<String> pidList;

	public FedoraReaderXmlHelperSpy2() {
		pidList = new ArrayList<String>();
		pidList.add("SomePid");
		pidList.add("SomeOtherPid");
	}

	@Override
	public FedoraListSession getSessionIfAvailable(String responseXML) {
		MCR.addCall("xml", responseXML);

		// If no cursor available, null should be returned

		FedoraListSession fedoraReaderCursor = new FedoraListSession("someToken");
		MCR.addReturned(fedoraReaderCursor);
		return fedoraReaderCursor;
	}

	@Override
	public List<String> getPidList(String responseXML) {
		MCR.addCall("xml", responseXML);
		MCR.addReturned(pidList);

		return pidList;
	}

}
