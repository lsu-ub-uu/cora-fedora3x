/*
 * Copyright 2019 Uppsala University Library
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
package se.uu.ub.cora.fedoralegacy.spies.data;

import se.uu.ub.cora.data.DataAttribute;

public class DataAttributeSpy implements DataAttribute {

	public String nameInData;
	public String value;

	public DataAttributeSpy(String nameInData, String value) {
		this.nameInData = nameInData;
		this.value = value;
	}

	@Override
	public String getNameInData() {
		return nameInData;
	}

	@Override
	public String getValue() {
		return value;
	}

}
