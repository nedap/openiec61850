/*
 * Copyright 2011-13 Fraunhofer ISE, energy & meteo Systems GmbH and other contributors
 *
 * This file is part of OpenIEC61850.
 * For more information visit http://www.openmuc.org
 *
 * OpenIEC61850 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * OpenIEC61850 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with OpenIEC61850.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.openiec61850.client;

import java.util.ArrayList;
import java.util.List;

import org.openmuc.openiec61850.BdaEntryTime;
import org.openmuc.openiec61850.BdaOctetString;
import org.openmuc.openiec61850.BdaOptFlds;
import org.openmuc.openiec61850.BdaReasonForInclusion;
import org.openmuc.openiec61850.DataSet;

public class Report {

	private final String rptId;
	private final BdaOptFlds optFlds;
	private final Integer sqNum;
	private final Integer subSqNum;
	private final boolean moreSegmentsFollow;
	private final String dataSetRef;
	private final boolean bufOvfl;
	private final Long confRev;
	private final BdaEntryTime timeOfEntry;
	private final BdaOctetString entryId;
	private final byte[] inclusionBitString;
	private final List<BdaReasonForInclusion> reasonCodes;
	private final DataSet dataSet;

	private final List<ReportEntryData> entryData = new ArrayList<ReportEntryData>();

	public Report(String rptId, BdaOptFlds optFlds, Integer sqNum, Integer subSqNum, boolean moreSegmentsFollow,
			String dataSetRef, boolean bufOvfl, Long confRev, BdaEntryTime timeOfEntry, BdaOctetString entryId,
			byte[] inclusionBitString, List<BdaReasonForInclusion> reasonCodes, DataSet dataSet) {
		this.rptId = rptId;
		this.optFlds = optFlds;
		this.sqNum = sqNum;
		this.subSqNum = subSqNum;
		this.moreSegmentsFollow = moreSegmentsFollow;
		this.dataSetRef = dataSetRef;
		this.bufOvfl = bufOvfl;
		this.confRev = confRev;
		this.timeOfEntry = timeOfEntry;
		this.entryId = entryId;
		this.inclusionBitString = inclusionBitString;
		this.reasonCodes = reasonCodes;
		this.dataSet = dataSet;
	}

	public String getRptId() {
		return rptId;
	}

	public BdaOptFlds getOptFlds() {
		return optFlds;
	}

	/**
	 * Sequence numberThe parameter MoreSegmentsFollow indicates that more report segments with the same sequence number
	 * follow, counted up for every {@code Report} instance generated
	 */
	public Integer getSqNum() {
		return sqNum;
	}

	/**
	 * For the case of long reports that do not fit into one message, a single report shall be divided into subreports.
	 * Each segment – of one report – shall be numbered with the same sequence number and a unique SubSqNum.
	 */
	public Integer getSubSqNum() {
		return subSqNum;
	}

	/**
	 * The parameter MoreSegmentsFollow indicates that more report segments with the same sequence number follow
	 */
	public boolean isMoreSegmentsFollow() {
		return moreSegmentsFollow;
	}

	public String getDataSetRef() {
		return dataSetRef;
	}

	/**
	 * The parameter BufOvfl shall indicate to the client that entries within the buffer may have been lost. The
	 * detection of possible loss of information occurs when a client requests a resynchronization to a non-existent
	 * entry or to the first entry in the queue.
	 */
	public boolean isBufOvfl() {
		return bufOvfl;
	}

	public Long getConfRev() {
		return confRev;
	}

	/**
	 * The parameter TimeOfEntry shall specify the time when the EntryID was created
	 */
	public BdaEntryTime getTimeOfEntry() {
		return timeOfEntry;
	}

	/**
	 * Unique Id of this Report
	 */
	public BdaOctetString getEntryId() {
		return entryId;
	}

	public List<ReportEntryData> getEntryData() {
		return entryData;
	}

	/**
	 * Indicator of data set members included in the report
	 */
	public byte[] getInclusionBitString() {
		return inclusionBitString;
	}

	/**
	 * Reason for the inclusion
	 */
	public List<BdaReasonForInclusion> getReasonCodes() {
		return reasonCodes;
	}

	/**
	 * Data set reference - this is an updated copy
	 */
	public DataSet getDataSet() {
		return dataSet;
	}

}
