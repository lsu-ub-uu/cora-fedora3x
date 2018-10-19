package se.uu.ub.cora.fedora.reader;

public class FedoraReaderException extends Exception {
	private static final long serialVersionUID = 8822802076375291316L;

	public FedoraReaderException(String message) {
		super(message);
	}

	public FedoraReaderException(String message, Exception e) {
		super(message, e);
	}
}
