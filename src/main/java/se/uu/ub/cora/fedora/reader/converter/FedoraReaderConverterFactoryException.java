package se.uu.ub.cora.fedora.reader.converter;

public class FedoraReaderConverterFactoryException extends Exception {
	private static final long serialVersionUID = 462218384003989155L;

	public FedoraReaderConverterFactoryException(String message) {
		super(message);
	}

	public FedoraReaderConverterFactoryException(String message, Exception e) {
		super(message, e);
	}
}
