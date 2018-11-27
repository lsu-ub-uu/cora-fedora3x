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
package se.uu.ub.cora.fedora.data;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

public class FedoraReaderPidListWithOptionalCursorTest {
	@Test
	public void testInstantiationNoCursor() {
		List<String> pidList = new ArrayList<>();
		var queryParameters = new FedoraReaderPidListWithOptionalCursor(pidList);

		assertEquals(queryParameters.getPidList(), pidList);
		assertNull(queryParameters.getCursor());
	}

	@Test
	public void testInstantiation() {
		List<String> pidList = new ArrayList<>();
		FedoraReaderCursor cursor = new FedoraReaderCursor("Test");
		var queryParameters = new FedoraReaderPidListWithOptionalCursor(pidList, cursor);

		assertEquals(queryParameters.getPidList(), pidList);
		assertEquals(queryParameters.getCursor(), cursor);
	}
}
