package se.uu.ub.cora.fedora.data;

public class XMLXPathParserException extends Exception {
	private static final long serialVersionUID = -255261285196817577L;

	public XMLXPathParserException(String message) {
		super(message);
	}

	public XMLXPathParserException(String message, Exception e) {
		super(message, e);
	}
}
