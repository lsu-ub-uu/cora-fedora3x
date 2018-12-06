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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.fedora.data.FedoraReaderXmlHelper;
import se.uu.ub.cora.fedora.data.FedoraReaderXmlHelperSpy;
import se.uu.ub.cora.fedora.data.HttpHandlerFactorySpy;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

public class FedoraReaderFactoryTest {

	private FedoraReaderFactory fedoraReaderFactory;
	private HttpHandlerFactory httpHandlerFactory;
	private FedoraReaderXmlHelper fedoraReaderXmlHelper;

	@BeforeMethod
	public void init() {
		httpHandlerFactory = new HttpHandlerFactorySpy();
		fedoraReaderXmlHelper = new FedoraReaderXmlHelperSpy();
		fedoraReaderFactory = FedoraReaderFactoryImp
				.usingHttpHandlerFactoryAndFedoraReaderXmlHelper(httpHandlerFactory,
						fedoraReaderXmlHelper);
	}

	@Test
	public void testGetFedoraReader() {
		assertTrue(fedoraReaderFactory.factor("someBaseUrl") instanceof FedoraReaderImp);
	}

	@Test
	public void testBaseUrlSentToFactored() throws Exception {
		String baseUrl = "someBaseUrl";
		FedoraReaderImp reader = (FedoraReaderImp) fedoraReaderFactory.factor(baseUrl);

		assertEquals(reader.forTestGetBaseUrl(), baseUrl);
	}

	@Test
	public void testOtherBaseUrlSentToFactored() throws Exception {
		String baseUrl = "someOtherBaseUrl";
		FedoraReaderImp reader = (FedoraReaderImp) fedoraReaderFactory.factor(baseUrl);

		assertEquals(reader.forTestGetBaseUrl(), baseUrl);
	}

	@Test
	public void testHttpHandlerFactorySentToFactored() {
		FedoraReaderImp reader = (FedoraReaderImp) fedoraReaderFactory.factor("someBaseUrl");
		assertSame(httpHandlerFactory, reader.forTestGetHttpHandlerFactory());
	}

	@Test
	public void testFedoraReaderXmlHelperSentToFactored() {
		FedoraReaderImp reader = (FedoraReaderImp) fedoraReaderFactory.factor("someBaseUrl");
		assertSame(fedoraReaderXmlHelper, reader.forTestGetFedoraReaderXmlHelper());
	}

}
