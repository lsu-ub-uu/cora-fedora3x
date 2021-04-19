module se.uu.ub.cora.fedora {
	requires java.xml;
	requires transitive se.uu.ub.cora.httphandler;
	requires se.uu.ub.cora.data;

	exports se.uu.ub.cora.fedora.reader;
}