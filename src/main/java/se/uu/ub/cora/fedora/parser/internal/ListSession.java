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

public class ListSession {
	public static ListSession createListSessionUsingToken(String token) {
		return new ListSession(token);
	}

	public static ListSession createListSessionNoMoreResults() {
		return new ListSession();
	}

	public String token;
	public String cursor;
	public boolean hasMoreResults = false;

	private ListSession(String token) {
		this.token = token;
		hasMoreResults = true;
	}

	private ListSession() {
		hasMoreResults = false;
	}

	public String getToken() {
		return token;
	}

	public String getCursor() {
		return cursor;
	}

	public void setCursor(String cursor) {
		this.cursor = cursor;
	}
}
