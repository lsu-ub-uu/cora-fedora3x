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

import java.util.List;

import se.uu.ub.cora.data.DataGroup;

/**
 * FedoraReader exposes methods to read data from a Fedora Commons system.
 * 
 * <p>
 * Implementations of this interface does not have to be threadsafe. But MUST handle sequential
 * calls to all methods.
 */
public interface FedoraReader {
	String readObject(String objectId);

	List<String> readList(String type, DataGroup filter);

	void setMaxResults(int count);

	/**
	 * readPidsForType returns a list of all pids for a given type that has fedora state A (Active).
	 * <p>
	 * If the list of pids can not be read SHOULD a {@link FedoraException} be thrown indicating
	 * what went wrong.
	 * 
	 * @param type
	 *            A String with the type to return pids for
	 * @return A List of Strings with the PID:s for the specified type
	 */
	List<String> readPidsForType(String type);

	/**
	 * readPidsForTypeCreatedAfter returns a list of all pids for a given type that has fedora state
	 * A (Active) and that are created after the specified dateTime.
	 * <p>
	 * If the list of pids can not be read SHOULD a {@link FedoraException} be thrown indicating
	 * what went wrong.
	 * 
	 * @param type
	 *            A String with the type to return pids for
	 * @param dateTime
	 *            A String on the format yyyy-MM-ddTHH:mm:ssZ
	 * @return A List of Strings with the PID:s for the specified type
	 */
	List<String> readPidsForTypeCreatedAfter(String type, String dateTime);

	/**
	 * readPidsForTypeCreatedBeforeAndUpdatedAfter returns a list of all pids for a given type that
	 * has fedora state A (Active) and that are created before and updated after the specified
	 * dateTime.
	 * <p>
	 * If the list of pids can not be read SHOULD a {@link FedoraException} be thrown indicating
	 * what went wrong.
	 * 
	 * @param type
	 *            A String with the type to return pids for
	 * @param dateTime
	 *            A String on the format yyyy-MM-ddTHH:mm:ssZ
	 * @return A List of Strings with the PID:s for the specified type
	 */
	List<String> readPidsForTypeCreatedBeforeAndUpdatedAfter(String type, String dateTime);

	/**
	 * readPidsForTypeUpdatedAfter returns a list of all pids for a given type that has fedora state
	 * A (Active) and that are updated after the specified dateTime.
	 * <p>
	 * If the list of pids can not be read SHOULD a {@link FedoraException} be thrown indicating
	 * what went wrong.
	 * 
	 * @param type
	 *            A String with the type to return pids for
	 * @param dateTime
	 *            A String on the format yyyy-MM-ddTHH:mm:ssZ
	 * @return A List of Strings with the PID:s for the specified type
	 */
	List<String> readPidsForTypeUpdatedAfter(String type, String dateTime);

	/**
	 * readPidsForTypeDeletedAfter returns a list of all pids for a given type that has fedora state
	 * D (Deleted) and that are updated after the specified dateTime.
	 * <p>
	 * If the list of pids can not be read SHOULD a {@link FedoraException} be thrown indicating
	 * what went wrong.
	 * 
	 * @param type
	 *            A String with the type to return pids for
	 * @param dateTime
	 *            A String on the format yyyy-MM-ddTHH:mm:ssZ
	 * @return A List of Strings with the PID:s for the specified type
	 */
	List<String> readPidsForTypeDeletedAfter(String type, String datetime);
}
