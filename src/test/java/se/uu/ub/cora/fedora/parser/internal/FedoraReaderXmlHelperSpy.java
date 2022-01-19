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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.uu.ub.cora.fedora.parser.FedoraReaderXmlHelper;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class FedoraReaderXmlHelperSpy implements FedoraReaderXmlHelper {
	public MethodCallRecorder MCR = new MethodCallRecorder();

	public List<String> pidList;
	public List<String> pidList2;
	public int pidCountInStorage = 0;
	public boolean failPidExtraction;
	public int hardLimitOnMaxResults = 0;

	public FedoraReaderXmlHelperSpy() {
		failPidExtraction = false;
	}

	public void addPidListForXml(String xml, boolean hasCursor, List<String> pidList) {
	}

	@Override
	public ListSession getSession(String xml) {
		MCR.addCall("xml", xml);
		ListSession listSession = ListSession.createListSessionNoMoreResults();
		if (pidList == null) {
			Pattern p = Pattern.compile(".+maxResults=(\\d+).+");
			Matcher m = p.matcher(xml);
			int maxResults = 3;
			if (m.find()) {
				maxResults = Integer.parseInt(m.group(1));
			}
			if (hardLimitOnMaxResults != 0 && hardLimitOnMaxResults < maxResults) {
				maxResults = hardLimitOnMaxResults;
			}
			pidCountInStorage -= maxResults;
		} else {
			if (!pidList.isEmpty()) {
				listSession = ListSession.createListSessionUsingToken("someToken");
			}
		}
		MCR.addReturned(listSession);
		return listSession;
	}

	@Override
	public List<String> getPidList(String xml) {
		MCR.addCall("xml", xml);
		if (failPidExtraction) {
			throw new RuntimeException("Bad XML: " + xml);
		}
		if (pidList2 != null) {
			MCR.addReturned(pidList2);
			return pidList2;
		}
		Pattern p = Pattern.compile(".+maxResults=(\\d+).+");
		Matcher m = p.matcher(xml);
		List<String> of = List.of();
		if (m.find()) {
			int maxResults = Integer.parseInt(m.group(1));
			if (hardLimitOnMaxResults != 0 && hardLimitOnMaxResults < maxResults) {
				maxResults = hardLimitOnMaxResults;
			}
			if (pidList == null) {
				List<String> madeUpPidList = new ArrayList<>();
				for (int idx = 0; idx < maxResults; idx++) {
					madeUpPidList.add("generatedPid:" + idx);
				}
				MCR.addReturned(madeUpPidList);
				return madeUpPidList;
			} else {
				List<String> requestedPidList;
				if (maxResults > pidList.size()) {
					requestedPidList = pidList;
					pidList = of;
				} else {
					requestedPidList = pidList.subList(0, maxResults);
					pidList = pidList.subList(maxResults, pidList.size());
				}
				pidCountInStorage = pidList.size();
				MCR.addReturned(requestedPidList);
				return requestedPidList;
			}
		}
		MCR.addReturned(of);
		return of;
	}

}
