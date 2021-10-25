/*
 * Copyright 2018, 2021 Uppsala University Library
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
package se.uu.ub.cora.fedora.reader;

import se.uu.ub.cora.fedora.parser.FedoraReaderXmlHelperImp;
import se.uu.ub.cora.fedora.parser.XMLXPathParserFactoryImp;
import se.uu.ub.cora.fedora.reader.internal.FedoraReaderImp;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;

public class FedoraReaderFactoryImp implements FedoraReaderFactory {
	private HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();
	private XMLXPathParserFactoryImp xmlxPathParserFactory = new XMLXPathParserFactoryImp();

	@Override
	public FedoraReader factor(String baseUrl) {
		FedoraReaderXmlHelperImp fedoraReaderXmlHelper = new FedoraReaderXmlHelperImp(
				xmlxPathParserFactory);
		return FedoraReaderImp.usingHttpHandlerFactoryAndFedoraReaderXmlHelperAndBaseUrl(
				httpHandlerFactory, fedoraReaderXmlHelper, baseUrl);
	}
}
