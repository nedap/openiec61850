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

package org.openmuc.openiec61850.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import org.openmuc.openiec61850.internal.acse.AcseAssociation;
import org.openmuc.openiec61850.internal.acse.ByteBufferInputStream;
import org.openmuc.openiec61850.internal.acse.DecodingException;
import org.openmuc.openiec61850.internal.mms.asn1.ConfirmedRequestPdu;
import org.openmuc.openiec61850.internal.mms.asn1.MmsPdu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientReceiver extends Thread {

	private final static Logger logger = LoggerFactory.getLogger(ClientReceiver.class);

	private final AcseAssociation acseAssociation;

	private final BlockingQueue<MmsPdu> incomingResponses;
	private final BlockingQueue<MmsPdu> incomingReports;
	private Integer expectedResponseId;
	private final ByteBuffer pduBuffer;

	private volatile boolean queueReports = false;
	private volatile boolean stopped = false;

	private IOException lastIOException = null;

	public ClientReceiver(AcseAssociation acseAssociation, BlockingQueue<MmsPdu> incomingResponses,
			BlockingQueue<MmsPdu> incomingReports, int maxMmsPduSize) {
		this.acseAssociation = acseAssociation;
		this.incomingResponses = incomingResponses;
		this.incomingReports = incomingReports;

		pduBuffer = ByteBuffer.allocate(maxMmsPduSize + 400);
	}

	@Override
	public void run() {
		try {
			while (true) {

				pduBuffer.clear();
				try {
					acseAssociation.receive(pduBuffer);
				} catch (TimeoutException e) {
					logger.error("Illegal state: A timeout exception was thrown.", e);
					throw new IllegalStateException();
				} catch (DecodingException e) {
					logger.warn("Error decoding the OSI headers of the received packet", e);
					continue;
				}

				MmsPdu decodedResponsePdu = new MmsPdu();
				try {
					decodedResponsePdu.decode(new ByteBufferInputStream(pduBuffer), null);
				} catch (IOException e) {
					logger.warn("Error decoding the received MMS PDU", e);
					continue;
				}

				if (decodedResponsePdu.unconfirmedPdu != null) {
					if (decodedResponsePdu.unconfirmedPdu.unconfirmedService.informationReport.variableAccessSpecification.listOfVariable != null) {
						logger.debug("Discarding LastApplError Report");
					}
					else if (queueReports == true) {
						try {
							incomingReports.put(decodedResponsePdu);
						} catch (InterruptedException e) {
						}
					}
					else {
						logger.debug("discarding report because reports are disabled");
					}
				}
				else if (decodedResponsePdu.rejectPdu != null) {
					synchronized (incomingResponses) {
						if (expectedResponseId == null) {
							logger.warn("Discarding Reject MMS PDU because no listener for request was found.");
							continue;
						}
						else if (decodedResponsePdu.rejectPdu.originalInvokeID.val != expectedResponseId) {
							logger.warn("Discarding Reject MMS PDU because no listener with fitting invokeID was found.");
							continue;
						}
						else {
							try {
								incomingResponses.put(decodedResponsePdu);
							} catch (InterruptedException e) {
							}
						}
					}
				}
				else if (decodedResponsePdu.confirmedErrorPdu != null) {
					synchronized (incomingResponses) {
						if (expectedResponseId == null) {
							logger.warn("Discarding ConfirmedError MMS PDU because no listener for request was found.");
							continue;
						}
						else if (decodedResponsePdu.confirmedErrorPdu.invokeID.val != expectedResponseId) {
							logger.warn("Discarding ConfirmedError MMS PDU because no listener with fitting invokeID was found.");
							continue;
						}
						else {
							try {
								incomingResponses.put(decodedResponsePdu);
							} catch (InterruptedException e) {
							}
						}
					}
				}
				else {
					synchronized (incomingResponses) {
						if (expectedResponseId == null) {
							logger.warn("Discarding ConfirmedResponse MMS PDU because no listener for request was found.");
							continue;
						}
						else if (decodedResponsePdu.confirmedResponsePdu.invokeID.val != expectedResponseId) {
							logger.warn("Discarding ConfirmedResponse MMS PDU because no listener with fitting invokeID was found.");
							continue;
						}
						else {
							try {
								incomingResponses.put(decodedResponsePdu);
							} catch (InterruptedException e) {
							}
						}
					}

				}
			}
		} catch (IOException e) {
			if (stopped == false) {
				shutdown();
				lastIOException = e;
			}
		} catch (Exception e) {
			if (stopped == false) {
				shutdown();
				lastIOException = new IOException("unexpected exception while receiving", e);
			}
		}
	}

	public void enableReportQueueing() {
		queueReports = true;
	}

	public void disableReportQueueing() {
		queueReports = false;
	}

	public void setResponseExpected(int invokeId) {
		expectedResponseId = invokeId;
	}

	public void disconnect() {
		stopped = true;
		acseAssociation.disconnect();
	}

	public void close() {
		stopped = true;
		acseAssociation.close();
	}

	private void shutdown() {
		if (stopped == false) {
			close();
		}
		try {
			incomingResponses
					.put(new MmsPdu(new ConfirmedRequestPdu(), null, null, null, null, null, null, null, null));
			incomingReports.put(new MmsPdu(new ConfirmedRequestPdu(), null, null, null, null, null, null, null, null));
		} catch (InterruptedException e1) {
		}
	}

	public IOException getLastIOException() {
		return lastIOException;
	}

	public MmsPdu removeExpectedResponse() {
		synchronized (incomingResponses) {
			expectedResponseId = null;
			return incomingResponses.poll();
		}
	}
}
