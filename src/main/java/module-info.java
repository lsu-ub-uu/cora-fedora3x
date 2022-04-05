/**
 * The fedora module provides interfaces and classes to use a Fedora Commons System in a Cora based
 * system.
 */
module se.uu.ub.cora.fedoralegacy {
	requires java.xml;
	requires se.uu.ub.cora.httphandler;
	requires transitive se.uu.ub.cora.data;

	exports se.uu.ub.cora.fedoralegacy;
	exports se.uu.ub.cora.fedoralegacy.reader;
	exports se.uu.ub.cora.fedoralegacy.parser;
}