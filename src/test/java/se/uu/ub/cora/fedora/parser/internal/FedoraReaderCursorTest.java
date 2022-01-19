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
package se.uu.ub.cora.fedora.parser.internal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

public class FedoraReaderCursorTest {

	public static final String SOME_CURSOR_TOKEN = "someCursorToken";
	public static final String SOME_CURSOR_POSITION = "someCursor";

	@Test
	public void initFedoraReaderCursor() {
		ListSession fedoraReaderCursor = ListSession.createListSessionUsingToken(SOME_CURSOR_TOKEN);
		assertNotNull(fedoraReaderCursor);
		assertEquals(fedoraReaderCursor.getToken(), SOME_CURSOR_TOKEN);
	}

	@Test
	public void testReadCursor() {
		ListSession fedoraReaderCursor = ListSession.createListSessionUsingToken(SOME_CURSOR_TOKEN);
		fedoraReaderCursor.setCursor(SOME_CURSOR_POSITION);
		assertEquals(fedoraReaderCursor.getCursor(), SOME_CURSOR_POSITION);
	}
}
