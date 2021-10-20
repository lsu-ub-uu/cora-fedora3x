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

import java.util.List;

import se.uu.ub.cora.data.DataGroup;

public interface FedoraReader {
	String readObject(String objectId);

	List<String> readList(String type, DataGroup filter);

	void setMaxResults(int count);

	/**
	 * readPidsForType returns a list of all pids for a given type.
	 * 
	 * @param type
	 *            A String with the type to return pids for
	 * @return A List of Strings with the PID:s for the specified type
	 */
	List<String> readPidsForType(String type);
}
