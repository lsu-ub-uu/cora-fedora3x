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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FedoraReaderXmlHelperSpy implements FedoraReaderXmlHelper {
	public List<String> pidList;
	public int pidCountInStorage = 0;
	public boolean failPidExtraction;
	public int hardLimitOnMaxResults = 0;

	public FedoraReaderXmlHelperSpy() {
		failPidExtraction = false;
	}

	public void addPidListForXml(String xml, boolean hasCursor, List<String> pidList) {
	}

	@Override
	public FedoraReaderCursor getCursorIfAvailable(String xml) {
		FedoraReaderCursor fedoraReaderCursor = null;
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
				fedoraReaderCursor = new FedoraReaderCursor("someToken");
			}
		}
		return fedoraReaderCursor;
	}

	@Override
	public List<String> getPidList(String xml) {
		if (failPidExtraction) {
			throw new RuntimeException("Bad XML: " + xml);
		}
		Pattern p = Pattern.compile(".+maxResults=(\\d+).+");
		Matcher m = p.matcher(xml);
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
				return madeUpPidList;
			} else {
				List<String> requestedPidList;
				if (maxResults > pidList.size()) {
					requestedPidList = pidList;
					pidList = List.of();
				} else {
					requestedPidList = pidList.subList(0, maxResults);
					pidList = pidList.subList(maxResults, pidList.size());
				}
				pidCountInStorage = pidList.size();
				return requestedPidList;
			}
		}
		return List.of();
	}

	// @Override
	// public XMLXPathParserFactory getXmlXPathParseFactory() {
	// return null;
	// }
	//
	// @Override
	// public void setXmlXPathParseFactory(XMLXPathParserFactory xmlXPathParserFactory) {
	//
	// }

}
