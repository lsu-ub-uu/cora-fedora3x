/*
 * Copyright 2018 Uppsala University Library
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

import se.uu.ub.cora.fedora.data.FedoraReaderXmlHelper;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

public class FedoraReaderFactoryImp implements FedoraReaderFactory {
	private HttpHandlerFactory httpHandlerFactory;
	private FedoraReaderXmlHelper fedoraReaderXmlHelper;

	public static FedoraReaderFactoryImp usingHttpHandlerFactoryAndFedoraReaderXmlHelper(
			HttpHandlerFactory httpHandlerFactory, FedoraReaderXmlHelper fedoraReaderXmlHelper) {
		return new FedoraReaderFactoryImp(httpHandlerFactory, fedoraReaderXmlHelper);
	}

	private FedoraReaderFactoryImp(HttpHandlerFactory httpHandlerFactory,
			FedoraReaderXmlHelper fedoraReaderXmlHelper) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.fedoraReaderXmlHelper = fedoraReaderXmlHelper;
	}

	@Override
	public FedoraReader factor(String baseUrl) {
		return FedoraReaderImp.usingHttpHandlerFactoryAndFedoraReaderXmlHelperAndBaseUrl(
				httpHandlerFactory, fedoraReaderXmlHelper, baseUrl);
	}
}
