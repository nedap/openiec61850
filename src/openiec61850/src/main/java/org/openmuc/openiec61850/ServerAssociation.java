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
package org.openmuc.openiec61850;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jasn1.ber.types.BerBitString;
import org.openmuc.jasn1.ber.types.BerBoolean;
import org.openmuc.jasn1.ber.types.BerInteger;
import org.openmuc.jasn1.ber.types.BerNull;
import org.openmuc.jasn1.ber.types.string.BerVisibleString;
import org.openmuc.openiec61850.client.Report;
import org.openmuc.openiec61850.internal.acse.AcseAssociation;
import org.openmuc.openiec61850.internal.acse.ByteBufferInputStream;
import org.openmuc.openiec61850.internal.acse.DecodingException;
import org.openmuc.openiec61850.internal.mms.asn1.AccessResult;
import org.openmuc.openiec61850.internal.mms.asn1.ConfirmedErrorPdu;
import org.openmuc.openiec61850.internal.mms.asn1.ConfirmedRequestPdu;
import org.openmuc.openiec61850.internal.mms.asn1.ConfirmedResponsePdu;
import org.openmuc.openiec61850.internal.mms.asn1.ConfirmedServiceRequest;
import org.openmuc.openiec61850.internal.mms.asn1.ConfirmedServiceResponse;
import org.openmuc.openiec61850.internal.mms.asn1.Data;
import org.openmuc.openiec61850.internal.mms.asn1.DefineNamedVariableListRequest;
import org.openmuc.openiec61850.internal.mms.asn1.DeleteNamedVariableListRequest;
import org.openmuc.openiec61850.internal.mms.asn1.DeleteNamedVariableListResponse;
import org.openmuc.openiec61850.internal.mms.asn1.GetNameListRequest;
import org.openmuc.openiec61850.internal.mms.asn1.GetNameListResponse;
import org.openmuc.openiec61850.internal.mms.asn1.GetNameListResponse.SubSeqOf_listOfIdentifier;
import org.openmuc.openiec61850.internal.mms.asn1.GetNamedVariableListAttributesResponse;
import org.openmuc.openiec61850.internal.mms.asn1.GetNamedVariableListAttributesResponse.SubSeqOf_listOfVariable;
import org.openmuc.openiec61850.internal.mms.asn1.GetVariableAccessAttributesRequest;
import org.openmuc.openiec61850.internal.mms.asn1.GetVariableAccessAttributesResponse;
import org.openmuc.openiec61850.internal.mms.asn1.InitResponseDetail;
import org.openmuc.openiec61850.internal.mms.asn1.InitiateRequestPdu;
import org.openmuc.openiec61850.internal.mms.asn1.InitiateResponsePdu;
import org.openmuc.openiec61850.internal.mms.asn1.MmsPdu;
import org.openmuc.openiec61850.internal.mms.asn1.ObjectName;
import org.openmuc.openiec61850.internal.mms.asn1.ObjectName.SubSeq_domain_specific;
import org.openmuc.openiec61850.internal.mms.asn1.ReadRequest;
import org.openmuc.openiec61850.internal.mms.asn1.ReadResponse;
import org.openmuc.openiec61850.internal.mms.asn1.ReadResponse.SubSeqOf_listOfAccessResult;
import org.openmuc.openiec61850.internal.mms.asn1.ServiceError.SubChoice_errorClass;
import org.openmuc.openiec61850.internal.mms.asn1.StructComponent;
import org.openmuc.openiec61850.internal.mms.asn1.TypeSpecification;
import org.openmuc.openiec61850.internal.mms.asn1.TypeSpecification.SubSeq_structure;
import org.openmuc.openiec61850.internal.mms.asn1.TypeSpecification.SubSeq_structure.SubSeqOf_components;
import org.openmuc.openiec61850.internal.mms.asn1.VariableAccessSpecification;
import org.openmuc.openiec61850.internal.mms.asn1.VariableDef;
import org.openmuc.openiec61850.internal.mms.asn1.WriteRequest;
import org.openmuc.openiec61850.internal.mms.asn1.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ONE connection between a client and a server - the server side variant
 */
final class ServerAssociation {

	private final static Logger logger = LoggerFactory.getLogger(ServerAssociation.class);

	private final static WriteResponse.SubChoice writeSuccess = new WriteResponse.SubChoice(null, new BerNull());

	private AcseAssociation acseAssociation = null;

	private int negotiatedMaxPduSize;
	private ByteBuffer pduBuffer;
	private final ServerSap serverSap;
	final ServerModel serverModel;

	private final BerByteArrayOutputStream berOStream = new BerByteArrayOutputStream(500, true);
	private boolean insertRef;
	private String continueAfter;

	List<FcModelNode> selects = new ArrayList<FcModelNode>();

	private static String[] mmsFcs = { "MX", "ST", "CO", "CF", "DC", "SP", "SG", "RP", "LG", "BR", "GO", "GS", "SV",
			"SE", "EX", "SR", "OR", "BL" };

	public ServerAssociation(ServerSap serverSap) {
		this.serverSap = serverSap;
		serverModel = serverSap.getServerModel();
	}

	public void handleNewAssociation(AcseAssociation acseAssociation, ByteBuffer associationRequest) {

		this.acseAssociation = acseAssociation;

		if (!associate(acseAssociation, associationRequest)) {
			return;
		}

		handleConnection(acseAssociation);

	}

	private boolean associate(AcseAssociation acseAssociation, ByteBuffer associationRequest) {

		MmsPdu mmsPdu = new MmsPdu();
		try {
			mmsPdu.decode(new ByteBufferInputStream(associationRequest), null);
		} catch (IOException e1) {
			logger.warn("IOException decoding MMS association request PDU", e1);
			acseAssociation.close();
			return false;
		}

		MmsPdu initiateResponseMmsPdu = constructAssociationResponsePdu(mmsPdu.initiateRequestPdu);

		try {
			initiateResponseMmsPdu.encode(berOStream, true);
		} catch (IOException e1) {
			logger.error("Unable to encode initiate response MMS PDU.");
			acseAssociation.close();
			return false;
		}

		try {
			acseAssociation.accept(berOStream.getByteBuffer());
		} catch (IOException e) {
			logger.warn("IOException accepting association request", e);
			acseAssociation.close();
			return false;
		}
		return true;
	}

	private MmsPdu constructAssociationResponsePdu(InitiateRequestPdu associationRequestMMSpdu) {

		negotiatedMaxPduSize = serverSap.getMaxMmsPduSize();

		if (associationRequestMMSpdu.localDetailCalling != null) {
			int proposedMaxMmsPduSize = (int) associationRequestMMSpdu.localDetailCalling.val;
			if (negotiatedMaxPduSize > proposedMaxMmsPduSize && proposedMaxMmsPduSize >= ServerSap.MINIMUM_MMS_PDU_SIZE) {
				negotiatedMaxPduSize = proposedMaxMmsPduSize;
			}
		}

		int negotiatedMaxServOutstandingCalling = serverSap.getProposedMaxServOutstandingCalling();
		int proposedMaxServOutstandingCalling = (int) associationRequestMMSpdu.proposedMaxServOutstandingCalling.val;

		if (negotiatedMaxServOutstandingCalling > proposedMaxServOutstandingCalling
				&& proposedMaxServOutstandingCalling > 0) {
			negotiatedMaxServOutstandingCalling = proposedMaxServOutstandingCalling;
		}

		int negotiatedMaxServOutstandingCalled = serverSap.getProposedMaxServOutstandingCalled();
		int proposedMaxServOutstandingCalled = (int) associationRequestMMSpdu.proposedMaxServOutstandingCalled.val;

		if (negotiatedMaxServOutstandingCalled > proposedMaxServOutstandingCalled
				&& proposedMaxServOutstandingCalled > 0) {
			negotiatedMaxServOutstandingCalled = proposedMaxServOutstandingCalled;
		}

		int negotiatedDataStructureNestingLevel = serverSap.getProposedDataStructureNestingLevel();

		if (associationRequestMMSpdu.proposedDataStructureNestingLevel != null) {
			int proposedDataStructureNestingLevel = (int) associationRequestMMSpdu.proposedDataStructureNestingLevel.val;
			if (negotiatedDataStructureNestingLevel > proposedDataStructureNestingLevel) {
				negotiatedDataStructureNestingLevel = proposedDataStructureNestingLevel;
			}
		}

		pduBuffer = ByteBuffer.allocate(negotiatedMaxPduSize + 500);

		byte[] negotiatedParameterCbbBitString = serverSap.getCbbBitString();

		byte[] servicesSupportedCalledBitString = serverSap.getServicesSupportedCalled();

		InitResponseDetail initRespDetail = new InitResponseDetail(new BerInteger(1), new BerBitString(
				negotiatedParameterCbbBitString, negotiatedParameterCbbBitString.length * 8 - 5), new BerBitString(
				servicesSupportedCalledBitString, servicesSupportedCalledBitString.length * 8 - 3));

		InitiateResponsePdu initRespPdu = new InitiateResponsePdu(new BerInteger(negotiatedMaxPduSize), new BerInteger(
				negotiatedMaxServOutstandingCalling), new BerInteger(negotiatedMaxServOutstandingCalled),
				new BerInteger(negotiatedDataStructureNestingLevel), initRespDetail);

		MmsPdu initiateResponseMMSpdu = new MmsPdu(null, null, null, null, null, null, initRespPdu, null, null);

		return initiateResponseMMSpdu;
	}

	private void handleConnection(AcseAssociation acseAssociation) {

		while (true) {

			MmsPdu mmsRequestPdu = listenForMmsRequest(acseAssociation);
			if (mmsRequestPdu == null) {
				cleanUpConnection();
				return;
			}

			ConfirmedRequestPdu confirmedRequestPdu = mmsRequestPdu.confirmedRequestPdu;
			// Do not have to check whether confirmedRequestPdu is null because that was already done by
			// listenForMmsRequest()

			if (confirmedRequestPdu.invokeID == null) {
				// cannot respond with ServiceError because no InvokeID was received
				logger.warn("Got unexpected MMS PDU or no invokeID");
				continue;
			}
			int invokeId = (int) confirmedRequestPdu.invokeID.val;

			try {
				if (confirmedRequestPdu.confirmedServiceRequest == null) {
					throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
							"Got an invalid MMS packet: confirmedServiceRequest empty");
				}

				ConfirmedServiceRequest confirmedServiceRequest = confirmedRequestPdu.confirmedServiceRequest;

				ConfirmedServiceResponse confirmedServiceResponse = null;

				if (confirmedServiceRequest.getNameList != null) {

					GetNameListRequest getNameListRequest = confirmedServiceRequest.getNameList;
					GetNameListResponse response = null;

					if (getNameListRequest.objectClass.basicObjectClass == null) {
						throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
								"Got an invalid MMS packet: ObjectClass was not selected in GetNameList request");
					}

					long basicObjectClass = getNameListRequest.objectClass.basicObjectClass.val;
					if (basicObjectClass == 9) {
						logger.debug("Got a GetServerDirectory (MMS GetNameList[DOMAIN]) request");
						response = handleGetServerDirectoryRequest(getNameListRequest);
					}
					else if (basicObjectClass == 0) {
						logger.debug("Got a Get{LD|LN}Directory (MMS GetNameList[NAMED_VARIABLE]) request");
						response = handleGetDirectoryRequest(getNameListRequest);
					}
					else if (basicObjectClass == 2) {
						logger.debug("Got a GetLogicalNodeDirectory[DataSet] (MMS GetNameList[NAMED_VARIABLE_LIST]) request");
						response = handleGetDataSetNamesRequest(getNameListRequest);
					}
					// else if (basicObjectClass == 8) {
					// logger.debug("Got a GetLogicalNodeDirectory[Log] (MMS GetNameList[JOURNAL]) request");
					// response =
					// handleGetNameListJournalRequest(getNameListRequest);
					// }

					confirmedServiceResponse = new ConfirmedServiceResponse(response, null, null, null, null, null,
							null);

				}
				else if (confirmedServiceRequest.getVariableAccessAttributes != null) {
					logger.debug("Got a GetDataDirectory/GetDataDefinition (MMS GetVariableAccessAttributes) request");
					GetVariableAccessAttributesResponse response = handleGetVariableAccessAttributesRequest(

					confirmedServiceRequest.getVariableAccessAttributes);
					confirmedServiceResponse = new ConfirmedServiceResponse(null, null, null, response, null, null,
							null);

				}
				else if (confirmedServiceRequest.read != null) {
					// GetDataValues, GetDataSetValues, GetBRCBValues and GetURCBValues map to this
					ReadResponse response = handleGetDataValuesRequest(confirmedServiceRequest.read);

					confirmedServiceResponse = new ConfirmedServiceResponse(null, response, null, null, null, null,
							null);
				}
				else if (confirmedServiceRequest.write != null) {
					logger.debug("Got a Write request");

					WriteResponse response = handleSetDataValuesRequest(confirmedServiceRequest.write);

					confirmedServiceResponse = new ConfirmedServiceResponse(null, null, response, null, null, null,
							null);

				}
				// for Data Sets
				else if (confirmedServiceRequest.defineNamedVariableList != null) {
					logger.debug("Got a CreateDataSet request");

					BerNull response = handleCreateDataSetRequest(confirmedServiceRequest.defineNamedVariableList);

					confirmedServiceResponse = new ConfirmedServiceResponse(null, null, null, null, response, null,
							null);
				}

				else if (confirmedServiceRequest.getNamedVariableListAttributes != null) {
					logger.debug("Got a GetDataSetDirectory request");
					GetNamedVariableListAttributesResponse response = handleGetDataSetDirectoryRequest(confirmedServiceRequest.getNamedVariableListAttributes);

					confirmedServiceResponse = new ConfirmedServiceResponse(null, null, null, null, null, response,
							null);

				}

				else if (confirmedServiceRequest.deleteNamedVariableList != null) {
					logger.debug("Got a DeleteDataSet request");
					DeleteNamedVariableListResponse response = handleDeleteDataSetRequest(confirmedServiceRequest.deleteNamedVariableList);

					confirmedServiceResponse = new ConfirmedServiceResponse(null, null, null, null, null, null,
							response);
				}

				else {
					throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
							"invalid MMS packet: unknown request type.");
				}

				ConfirmedResponsePdu confirmedResponsePdu = new ConfirmedResponsePdu(confirmedRequestPdu.invokeID,
						confirmedServiceResponse);

				MmsPdu mmsResponsePdu = new MmsPdu(null, confirmedResponsePdu, null, null, null, null, null, null, null);
				if (!sendAnMmsPdu(acseAssociation, mmsResponsePdu)) {
					cleanUpConnection();
					return;
				}
			} catch (ServiceError e) {
				logger.warn(e.getMessage());
				if (!sendAnMmsPdu(acseAssociation, createServiceErrorResponse(e, invokeId))) {
					cleanUpConnection();
					return;
				}
			}
		}
	}

	private void cleanUpConnection() {
		synchronized (serverModel) {
			for (FcModelNode selectedCdo : selects) {
				logger.debug("cleanin up fcdo: " + selectedCdo.getReference());
				selectedCdo.deselect();
			}
		}
	}

	private boolean sendAnMmsPdu(AcseAssociation acseAssociation, MmsPdu mmsResponsePdu) {

		berOStream.reset();
		try {
			mmsResponsePdu.encode(berOStream, false);
		} catch (IOException e1) {
			logger.error("IOException while encoding MMS PDU. Closing association.", e1);
			acseAssociation.close();
			return false;
		}
		try {
			acseAssociation.send(berOStream.getByteBuffer());
		} catch (IOException e) {
			logger.warn("IOException while sending MMS PDU. Closing association.", e);
			acseAssociation.close();
			return false;
		}
		return true;
	}

	private MmsPdu listenForMmsRequest(AcseAssociation acseAssociation) {

		while (true) {
			MmsPdu mmsRequestPdu = null;
			pduBuffer.clear();
			try {
				acseAssociation.receive(pduBuffer);
			} catch (EOFException e) {
				logger.debug("Connection was closed by client.");
				acseAssociation.close();
				return null;
			} catch (SocketTimeoutException e) {
				logger.warn("Message fragment timeout occured while receiving request. Closing association.", e);
				acseAssociation.close();
				return null;
			} catch (IOException e) {
				logger.warn("IOException at lower layers while listening for incoming request. Closing association.", e);
				acseAssociation.close();
				return null;
			} catch (DecodingException e) {
				logger.error("Error decoding request at OSI layers.", e);
				continue;
			} catch (TimeoutException e) {
				logger.error(
						"Illegal state: message timeout while receiving request though this timeout should 0 and never be thrown",
						e);
				acseAssociation.close();
				return null;
			}
			mmsRequestPdu = new MmsPdu();

			try {
				mmsRequestPdu.decode(new ByteBufferInputStream(pduBuffer), null);
			} catch (IOException e) {
				logger.warn("IOException decoding received MMS request PDU.", e);
				continue;
			}

			if (mmsRequestPdu.confirmedRequestPdu == null) {
				if (mmsRequestPdu.conclude_RequestPDU != null) {
					logger.debug("Got Conclude request, will close connection");
					acseAssociation.close();
					return null;
				}
				else {
					logger.warn("Got unexpected MMS PDU, will ignore it");
					continue;
				}
			}

			return mmsRequestPdu;
		}
	}

	private MmsPdu createServiceErrorResponse(ServiceError e, int invokeId) {

		SubChoice_errorClass errClass = null;

		switch (e.getErrorCode()) {

		case ServiceError.NO_ERROR:

			break;
		case ServiceError.INSTANCE_NOT_AVAILABLE:
			errClass = new SubChoice_errorClass(null, null, null, null, null, null, null, new BerInteger(
					e.getErrorCode()), null, null, null, null, null);
			break;
		case ServiceError.INSTANCE_IN_USE:
			errClass = new SubChoice_errorClass(null, null, new BerInteger(e.getErrorCode()), null, null, null, null,
					null, null, null, null, null, null);
			break;
		case ServiceError.ACCESS_VIOLATION:
			errClass = new SubChoice_errorClass(null, null, null, null, null, null, null, new BerInteger(
					e.getErrorCode()), null, null, null, null, null);
			break;
		case ServiceError.ACCESS_NOT_ALLOWED_IN_CURRENT_STATE:
			errClass = new SubChoice_errorClass(null, null, null, null, null, null, null, null, null, null, null, null,
					new BerInteger(e.getErrorCode()));
			break;
		case ServiceError.INSTANCE_LOCKED_BY_OTHER_CLIENT:
			errClass = new SubChoice_errorClass(null, null, null, null, null, null, null, null, null, null, null,
					new BerInteger(2), null);
			break;
		case ServiceError.TYPE_CONFLICT:
			errClass = new SubChoice_errorClass(null, null, null, null, null, null, null, null, null, null, null,
					new BerInteger(4), null);
			break;
		default:
			errClass = new SubChoice_errorClass(null, null, null, null, null, null, null, null, null, null, null, null,
					new BerInteger(e.getErrorCode()));
		}
		org.openmuc.openiec61850.internal.mms.asn1.ServiceError asn1ServiceError = null;

		asn1ServiceError = new org.openmuc.openiec61850.internal.mms.asn1.ServiceError(errClass, null,
				new BerVisibleString(e.getMessage()));

		MmsPdu mmsPdu = new MmsPdu(null, null, new ConfirmedErrorPdu(new BerInteger(invokeId), null, asn1ServiceError),
				null, null, null, null, null, null);

		return mmsPdu;
	}

	private GetNameListResponse handleGetServerDirectoryRequest(GetNameListRequest getNameListRequest)
			throws ServiceError {

		Vector<BerVisibleString> identifiers = new Vector<BerVisibleString>();
		BerVisibleString identifier = null;

		for (ModelNode ld : serverModel) {
			identifier = new BerVisibleString(ld.getName());
			identifiers.add(identifier);
		}

		GetNameListResponse getNameListResponse = new GetNameListResponse(new SubSeqOf_listOfIdentifier(identifiers),
				new BerBoolean(false));

		return getNameListResponse;
	}

	private GetNameListResponse handleGetDirectoryRequest(GetNameListRequest getNameListRequest) throws ServiceError {

		// the ObjectScope can be vmdSpecific,domainSpecific, or aaSpecific. vmdSpecific and aaSpecific are not part of
		// 61850-8-1 but are used by some IEC 61850 clients anyways. This stack will return an empty list on vmdSpecific
		// and aaSpecific requests.
		if (getNameListRequest.objectScope.aaSpecific != null || getNameListRequest.objectScope.vmdSpecific != null) {
			SubSeqOf_listOfIdentifier listOfIden = new SubSeqOf_listOfIdentifier(new Vector<BerVisibleString>());
			GetNameListResponse getNameListResponse = new GetNameListResponse(listOfIden, new BerBoolean(false));
			return getNameListResponse;
		}

		String mmsDomainId = getNameListRequest.objectScope.domainSpecific.toString();

		ModelNode logicalDeviceMn = serverModel.getChild(mmsDomainId);

		if (logicalDeviceMn == null) {
			throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
					"Got an invalid MMS request: given Domain name in GetNameList request is not a Logical Device name");
		}

		LogicalDevice logicalDevice = (LogicalDevice) logicalDeviceMn;

		insertRef = true;

		if (getNameListRequest.continueAfter != null) {
			continueAfter = getNameListRequest.continueAfter.toString();
			insertRef = false;
		}

		List<String> mmsReferences = new LinkedList<String>();

		for (ModelNode logicalNodeMn : logicalDevice) {
			LogicalNode logicalNode = (LogicalNode) logicalNodeMn;
			mmsReferences.add(logicalNode.getName());

			for (String mmsFC : mmsFcs) {
				Fc fc = Fc.fromString(mmsFC);
				if (fc != null) {

					if (fc == Fc.RP) {
						Collection<Urcb> urcbs = logicalNode.getUrcbs();
						if (urcbs.size() != 0) {
							mmsReferences.add(logicalNode.getName() + "$RP");
							for (Urcb urcb : urcbs) {
								insertMmsRef(urcb, mmsReferences, logicalNode.getName() + "$RP");
							}
						}
					}
					else if (fc == Fc.BR) {
						Collection<Brcb> brcbs = logicalNode.getBrcbs();
						if (brcbs.size() != 0) {
							mmsReferences.add(logicalNode.getName() + "$BR");
							for (Brcb brcb : brcbs) {
								insertMmsRef(brcb, mmsReferences, logicalNode.getName() + "$BR");
							}
						}
					}
					else {
						List<FcDataObject> fcDataObjects = logicalNode.getChildren(fc);
						if (fcDataObjects != null) {
							mmsReferences.add(logicalNode.getName() + "$" + mmsFC);
							for (FcDataObject dataObject : fcDataObjects) {
								insertMmsRef(dataObject, mmsReferences, logicalNode.getName() + "$" + mmsFC);
							}
						}
					}
				}
			}
		}

		Vector<BerVisibleString> identifiers = new Vector<BerVisibleString>();

		int identifierSize = 0;
		boolean moreFollows = false;
		for (String mmsReference : mmsReferences) {
			if (insertRef == true) {
				if (identifierSize > negotiatedMaxPduSize - 200) {
					moreFollows = true;
					logger.debug(" ->maxMMSPduSize of " + negotiatedMaxPduSize + " Bytes reached");
					break;
				}

				BerVisibleString identifier = null;

				identifier = new BerVisibleString(mmsReference);

				identifiers.add(identifier);
				identifierSize += mmsReference.length() + 2;
			}
			else {
				if (mmsReference.equals(continueAfter)) {
					insertRef = true;
				}
			}
		}

		SubSeqOf_listOfIdentifier listOfIden = new SubSeqOf_listOfIdentifier(identifiers);

		return new GetNameListResponse(listOfIden, new BerBoolean(moreFollows));
	}

	private static void insertMmsRef(ModelNode node, List<String> mmsRefs, String parentRef) {
		String ref = parentRef + '$' + node.getName();
		mmsRefs.add(ref);
		if (!(node instanceof Array)) {
			for (ModelNode childNode : node) {
				insertMmsRef(childNode, mmsRefs, ref);
			}
		}
	}

	/**
	 * GetVariableAccessAttributes (GetDataDefinition/GetDataDirectory) can be called with different kinds of
	 * references. Examples: 1. DGEN1 2. DGEN1$CF 3. DGEN1$CF$GnBlk
	 *
	 */
	@SuppressWarnings("unchecked")
	private GetVariableAccessAttributesResponse handleGetVariableAccessAttributesRequest(
			GetVariableAccessAttributesRequest getVariableAccessAttributesRequest) throws ServiceError {
		if (getVariableAccessAttributesRequest.name == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Got an invalid MMS packet: name is not selected in GetVariableAccessAttributesRequest");
		}

		SubSeq_domain_specific domainSpecific = getVariableAccessAttributesRequest.name.domain_specific;

		if (domainSpecific == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"Got an invalid MMS packet: Domain specific is not selected in GetVariableAccessAttributesRequest");
		}

		ModelNode modelNode = serverModel.getChild(domainSpecific.domainId.toString());

		if (modelNode == null) {
			throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
					"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
							+ getVariableAccessAttributesRequest.name.domain_specific.domainId + " and ItemID "
							+ getVariableAccessAttributesRequest.name.domain_specific.itemId + " was found.");
		}

		String itemIdString = domainSpecific.itemId.toString();

		int index1 = itemIdString.indexOf('$');

		LogicalNode logicalNode = null;

		if (index1 != -1) {
			logicalNode = (LogicalNode) modelNode.getChild(itemIdString.substring(0, index1));
			if (logicalNode == null) {
				throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
						"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
								+ getVariableAccessAttributesRequest.name.domain_specific.domainId + " and ItemID "
								+ getVariableAccessAttributesRequest.name.domain_specific.itemId + " was found.");
			}
			int index2 = itemIdString.indexOf('$', index1 + 2);
			if (index2 != -1) {
				Fc fc = Fc.fromString(itemIdString.substring(index1 + 1, index2));
				if (fc == null) {
					throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
							"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
									+ getVariableAccessAttributesRequest.name.domain_specific.domainId + " and ItemID "
									+ getVariableAccessAttributesRequest.name.domain_specific.itemId + " was found.");
				}
				index1 = itemIdString.indexOf('$', index2 + 2);
				ModelNode subNode;
				if (index1 == -1) {
					subNode = logicalNode.getChild(itemIdString.substring(index2 + 1), fc);
					if (subNode == null) {
						throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
								"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
										+ getVariableAccessAttributesRequest.name.domain_specific.domainId
										+ " and ItemID "
										+ getVariableAccessAttributesRequest.name.domain_specific.itemId
										+ " was found.");
					}
				}
				else {
					subNode = logicalNode.getChild(itemIdString.substring(index2 + 1, index1), fc);
					if (subNode == null) {
						throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
								"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
										+ getVariableAccessAttributesRequest.name.domain_specific.domainId
										+ " and ItemID "
										+ getVariableAccessAttributesRequest.name.domain_specific.itemId
										+ " was found.");
					}
					index2 = itemIdString.indexOf('$', index1 + 2);
					while (index2 != -1) {
						subNode = subNode.getChild(itemIdString.substring(index1 + 1, index2));
						if (subNode == null) {
							throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
									"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
											+ getVariableAccessAttributesRequest.name.domain_specific.domainId
											+ " and ItemID "
											+ getVariableAccessAttributesRequest.name.domain_specific.itemId
											+ " was found.");
						}
						index1 = index2;
						index2 = itemIdString.indexOf('$', index1 + 2);
					}
					subNode = subNode.getChild(itemIdString.substring(index1 + 1));
					if (subNode == null) {
						throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
								"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
										+ getVariableAccessAttributesRequest.name.domain_specific.domainId
										+ " and ItemID "
										+ getVariableAccessAttributesRequest.name.domain_specific.itemId
										+ " was found.");
					}
				}
				return new GetVariableAccessAttributesResponse(new BerBoolean(false), subNode.getMmsTypeSpec());
			}
			else {
				Fc fc = Fc.fromString(itemIdString.substring(index1 + 1));

				if (fc == null) {
					throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
							"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
									+ getVariableAccessAttributesRequest.name.domain_specific.domainId + " and ItemID "
									+ getVariableAccessAttributesRequest.name.domain_specific.itemId + " was found.");
				}

				List<FcDataObject> fcDataObjects = logicalNode.getChildren(fc);

				if (fcDataObjects == null || fcDataObjects.size() == 0) {
					throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
							"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
									+ getVariableAccessAttributesRequest.name.domain_specific.domainId + " and ItemID "
									+ getVariableAccessAttributesRequest.name.domain_specific.itemId + " was found.");
				}
				List<StructComponent> doStructComponents = new LinkedList<StructComponent>();

				for (ModelNode child : fcDataObjects) {
					doStructComponents.add(new StructComponent(new BerVisibleString(child.getName().getBytes()), child
							.getMmsTypeSpec()));
				}

				SubSeqOf_components comp = new SubSeqOf_components(doStructComponents);
				SubSeq_structure struct = new SubSeq_structure(null, comp);

				return new GetVariableAccessAttributesResponse(new BerBoolean(false), new TypeSpecification(null,
						struct, null, null, null, null, null, null, null, null, null, null));

			}
		}

		logicalNode = (LogicalNode) modelNode.getChild(itemIdString);
		if (logicalNode == null) {
			throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
					"GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
							+ getVariableAccessAttributesRequest.name.domain_specific.domainId + " and ItemID "
							+ getVariableAccessAttributesRequest.name.domain_specific.itemId + " was found.");
		}

		List<StructComponent> structComponents = new LinkedList<StructComponent>();

		for (String mmsFc : mmsFcs) {
			Fc fc = Fc.fromString(mmsFc);
			if (fc != null) {

				Collection<FcDataObject> fcDataObjects;
				if (fc == Fc.RP) {
					fcDataObjects = (Collection<FcDataObject>) (Collection<?>) logicalNode.getUrcbs();
					if (fcDataObjects.size() == 0) {
						continue;
					}
				}
				else if (fc == Fc.BR) {
					fcDataObjects = (Collection<FcDataObject>) (Collection<?>) logicalNode.getBrcbs();
					if (fcDataObjects.size() == 0) {
						continue;
					}
				}
				else {

					fcDataObjects = logicalNode.getChildren(fc);
					if (fcDataObjects == null) {
						continue;
					}
				}

				List<StructComponent> doStructComponents = new LinkedList<StructComponent>();

				for (ModelNode child : fcDataObjects) {
					doStructComponents.add(new StructComponent(new BerVisibleString(child.getName().getBytes()), child
							.getMmsTypeSpec()));
				}

				SubSeqOf_components comp = new SubSeqOf_components(doStructComponents);
				SubSeq_structure struct = new SubSeq_structure(null, comp);

				TypeSpecification fcTypeSpec = new TypeSpecification(null, struct, null, null, null, null, null, null,
						null, null, null, null);

				StructComponent structCom = null;

				structCom = new StructComponent(new BerVisibleString(mmsFc), fcTypeSpec);

				structComponents.add(structCom);

			}
		}

		SubSeqOf_components comp = new SubSeqOf_components(structComponents);
		SubSeq_structure struct = new SubSeq_structure(null, comp);

		TypeSpecification typeSpec = new TypeSpecification(null, struct, null, null, null, null, null, null, null,
				null, null, null);

		return new GetVariableAccessAttributesResponse(new BerBoolean(false), typeSpec);

	}

	private ReadResponse handleGetDataValuesRequest(ReadRequest mmsReadRequest) throws ServiceError {

		VariableAccessSpecification variableAccessSpecification = mmsReadRequest.variableAccessSpecification;

		if (mmsReadRequest.specificationWithResult == null || mmsReadRequest.specificationWithResult.val == false) {
			logger.debug("Got a GetDataValues request.");

			if (variableAccessSpecification.listOfVariable == null) {
				throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
						"handleGetDataValuesRequest: Got an invalid MMS packet");
			}

			List<VariableDef> listOfVariable = variableAccessSpecification.listOfVariable.seqOf;

			if (listOfVariable.size() != 1) {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INCONSISTENT,
						"handleGetDataValuesRequest: more than one variableAccessSpecification is not allowed");
			}

			List<AccessResult> listOfAccessResult = new ArrayList<AccessResult>(1);

			VariableDef variableDef = listOfVariable.get(0);

			FcModelNode modelNode = serverModel.getNodeFromVariableDef(variableDef);

			if (modelNode == null) {
				// 10 indicates error "object-non-existent"
				listOfAccessResult.add(new AccessResult(new BerInteger(10L), null));
			}
			else {
				synchronized (serverModel) {
					listOfAccessResult.add(getReadResult(modelNode));
				}
			}

			return new ReadResponse(null, new SubSeqOf_listOfAccessResult(listOfAccessResult));
		}

		else {
			logger.debug("Got a GetDataSetValues request.");

			String dataSetReference = convertToDataSetReference(variableAccessSpecification.variableListName);

			if (dataSetReference == null) {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INCONSISTENT,
						"handleGetDataSetValuesRequest: DataSet name incorrect");
			}

			// TODO handle non-persistent DataSets too

			List<AccessResult> listOfAccessResult;

			synchronized (serverModel) {
				DataSet dataSet = serverModel.getDataSet(dataSetReference);

				if (dataSet == null) {
					throw new ServiceError(ServiceError.PARAMETER_VALUE_INCONSISTENT,
							"handleGetDataSetValuesRequest: a DataSet with the given reference does not exist");
				}

				listOfAccessResult = new ArrayList<AccessResult>(dataSet.getMembers().size());

				for (FcModelNode dsMember : dataSet) {
					listOfAccessResult.add(getReadResult(dsMember));
				}
			}

			return new ReadResponse(null, new SubSeqOf_listOfAccessResult(listOfAccessResult));

		}

	}

	private AccessResult getReadResult(FcModelNode modelNode) {

		if (modelNode.getFc() == Fc.CO && modelNode.getName().equals("SBO")) {
			// if (modelNode.getName().equals("SBO")) {
			FcModelNode cdcParent = (FcModelNode) modelNode.getParent();
			ModelNode ctlModelNode = serverModel.findModelNode(cdcParent.getReference(), Fc.CF).getChild("ctlModel");
			if (ctlModelNode == null || !(ctlModelNode instanceof BdaInt8) || ((BdaInt8) ctlModelNode).getValue() != 2) {
				logger.warn("Selecting controle DO fails because ctlModel is not set to \"sbo-with-normal-security\"");
				// 3 indicates error "object_access_denied"
				return new AccessResult(new BerInteger(3L), null);
			}
			if (!cdcParent.select(this, serverSap.getTimer())) {
				return new AccessResult(null, new Data(null, null, null, null, null, null, null, null,
						new BerVisibleString(""), null, null, null));
			}
			return new AccessResult(null, new Data(null, null, null, null, null, null, null, null,
					new BerVisibleString("success"), null, null, null));
			// }
			// else {
			// logger.warn("A client tried to read a controle variable other than SBO. This is not allowed.");
			// // 3 indicates error "object_access_denied"
			// return new AccessResult(new BerInteger(3L), null);
			// }

		}

		Data data = modelNode.getMmsDataObj();

		if (data == null) {
			// 11 indicates error "object_value_invalid"
			return new AccessResult(new BerInteger(11L), null);
		}

		return new AccessResult(null, data);

	}

	private WriteResponse handleSetDataValuesRequest(WriteRequest mmsWriteRequest) throws ServiceError {

		VariableAccessSpecification variableAccessSpecification = mmsWriteRequest.variableAccessSpecification;

		List<Data> listOfData = mmsWriteRequest.listOfData.seqOf;

		List<WriteResponse.SubChoice> mmsResponseValues = new ArrayList<WriteResponse.SubChoice>(listOfData.size());

		if (variableAccessSpecification.listOfVariable != null) {
			logger.debug("Got a SetDataValues request.");

			List<VariableDef> listOfVariable = variableAccessSpecification.listOfVariable.seqOf;

			if (listOfVariable.size() != 1 || listOfData.size() != 1) {
				throw new ServiceError(ServiceError.PARAMETER_VALUE_INCONSISTENT,
						"handleSetDataValuesRequest: more than one variableAccessSpecification or data element is not allowed");
			}

			VariableDef variableDef = listOfVariable.get(0);

			FcModelNode modelNode = serverModel.getNodeFromVariableDef(variableDef);

			if (modelNode == null) {
				// 10 indicates error "object-non-existent"
				mmsResponseValues.add(new WriteResponse.SubChoice(new BerInteger(10L), null));
			}
			else {
				synchronized (serverModel) {
					mmsResponseValues.add(getWriteResult(modelNode, listOfData.get(0)));
				}
			}
		}
		else if (variableAccessSpecification.variableListName != null) {
			logger.debug("Got a SetDataSetValues request.");

			String dataSetRef = convertToDataSetReference(variableAccessSpecification.variableListName);

			// TODO handle non-persisten DataSets too

			DataSet dataSetCopy = serverModel.getDataSet(dataSetRef).copy();

			Iterator<Data> dataIterator = listOfData.iterator();

			for (FcModelNode dataSetMember : dataSetCopy) {
				mmsResponseValues.add(getWriteResult(dataSetMember, dataIterator.next()));
			}

		}
		else {
			throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
					"handleSetDataValuesRequest: invalid MMS request");
		}

		return new WriteResponse(mmsResponseValues);
	}

	private WriteResponse.SubChoice getWriteResult(FcModelNode modelNode, Data mmsData) {

		Fc fc = modelNode.getFc();
		if (fc == Fc.ST || fc == Fc.MX || fc == Fc.OR || fc == Fc.EX) {
			// 3 indicates error "object_access_denied"
			return new WriteResponse.SubChoice(new BerInteger(3L), null);
		}

		if (fc == Fc.CO) {
			String nodeName = modelNode.getName();

			if (nodeName.equals("Oper")) {
				FcModelNode cdcParent = (FcModelNode) modelNode.getParent();
				ModelNode ctlModelNode = serverModel.findModelNode(cdcParent.getReference(), Fc.CF)
						.getChild("ctlModel");
				if (ctlModelNode == null || !(ctlModelNode instanceof BdaInt8)) {
					logger.warn("Operatring controle DO failed because ctlModel is not set.");
					// 3 indicates error "object_access_denied"
					return new WriteResponse.SubChoice(new BerInteger(3L), null);
				}

				int ctlModel = ((BdaInt8) ctlModelNode).getValue();

				/* Direct control with normal security (direct-operate) */
				if (ctlModel == 1) {
					return operate(modelNode, mmsData);

				}
				/* SBO control with normal security (operate-once or operate-many) */
				else if (ctlModel == 2) {
					if (cdcParent.isSelectedBy(this)) {
						return operate(modelNode, mmsData);
					}
					else {
						// 3 indicates error "object_access_denied"
						return new WriteResponse.SubChoice(new BerInteger(3L), null);
					}

				}
				else {
					logger.warn("SetDataValues failed because of unsupported ctlModel: " + ctlModel);
					// 9 indicates error "object_access_unsupported"
					return new WriteResponse.SubChoice(new BerInteger(9L), null);

				}
			}
			else {
				logger.warn("SetDataValues failed because of the operation is not allowed yet: " + modelNode.getName());
				// 9 indicates error "object_access_unsupported"
				return new WriteResponse.SubChoice(new BerInteger(9L), null);
			}
		}
		else {

			FcModelNode fcModelNodeCopy = (FcModelNode) modelNode.copy();

			try {
				fcModelNodeCopy.setValueFromMmsDataObj(mmsData);
			} catch (ServiceError e1) {
				return new WriteResponse.SubChoice(new BerInteger(serviceErrorToMmsError(e1)), null);
			}

			List<BasicDataAttribute> bdas = fcModelNodeCopy.getBasicDataAttributes();

			WriteResponse.SubChoice result = writeSuccess;

			for (BasicDataAttribute bda : bdas) {
				try {
					serverSap.getWriteListener().write(bda);
					bda.getMirror().setValueFrom(bda);
				} catch (ServiceError e) {
					result = new WriteResponse.SubChoice(new BerInteger(serviceErrorToMmsError(e)), null);
				}
			}

			return result;
		}

	}

	private WriteResponse.SubChoice operate(FcModelNode modelNode, Data mmsData) {
		FcModelNode fcModelNodeCopy = (FcModelNode) modelNode.copy();
		try {
			fcModelNodeCopy.setValueFromMmsDataObj(mmsData);
		} catch (ServiceError e) {
			logger.warn("SetDataValues failed because of data missmatch.", e);
			return new WriteResponse.SubChoice(new BerInteger(serviceErrorToMmsError(e)), null);
		}

		// TODO timeactivate operate

		BasicDataAttribute ctlValBda = (BasicDataAttribute) fcModelNodeCopy.getChild("ctlVal");
		try {
			serverSap.getWriteListener().write(ctlValBda);
		} catch (ServiceError e) {
			return new WriteResponse.SubChoice(new BerInteger(serviceErrorToMmsError(e)), null);
		}
		ctlValBda.getMirror().setValueFrom(ctlValBda);
		// TODO write origin and ctlNum if they exist

		return writeSuccess;
	}

	private int serviceErrorToMmsError(ServiceError e) {

		switch (e.getErrorCode()) {
		case ServiceError.FAILED_DUE_TO_SERVER_CONSTRAINT:
			return 1;
		case ServiceError.INSTANCE_LOCKED_BY_OTHER_CLIENT:
			return 2;
		case ServiceError.ACCESS_VIOLATION:
			return 3;
		case ServiceError.TYPE_CONFLICT:
			return 7;
		case ServiceError.INSTANCE_NOT_AVAILABLE:
			return 10;
		case ServiceError.PARAMETER_VALUE_INCONSISTENT:
			return 11;
		default:
			return 9;
		}
	}

	private GetNameListResponse handleGetDataSetNamesRequest(GetNameListRequest getNameListRequest) throws ServiceError {

		BerVisibleString domainSpecific = getNameListRequest.objectScope.domainSpecific;

		if (domainSpecific == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"handleGetDataSetNamesRequest: domainSpecific not selected");
		}

		List<String> dsList = serverModel.getDataSetNames(domainSpecific.toString());

		insertRef = true;
		if (getNameListRequest.continueAfter != null) {
			continueAfter = getNameListRequest.continueAfter.toString();
			insertRef = false;
		}

		Vector<BerVisibleString> identifiers = new Vector<BerVisibleString>();

		int identifierSize = 0;
		boolean moreFollows = false;

		if (dsList != null) {
			for (String dsRef : dsList) {
				if (insertRef == true) {
					if (identifierSize > negotiatedMaxPduSize - 200) {
						moreFollows = true;
						logger.info("maxMMSPduSize reached");
						break;
					}
					identifiers.add(new BerVisibleString(dsRef.getBytes()));
					identifierSize += dsRef.length() + 2;
				}
				else {
					if (dsRef.equals(continueAfter)) {
						insertRef = true;
					}
				}
			}
		}

		SubSeqOf_listOfIdentifier listOf = new SubSeqOf_listOfIdentifier(identifiers);

		return new GetNameListResponse(listOf, new BerBoolean(moreFollows));
	}

	private GetNamedVariableListAttributesResponse handleGetDataSetDirectoryRequest(ObjectName mmsGetNamedVarListAttReq)
			throws ServiceError {

		String dataSetReference = convertToDataSetReference(mmsGetNamedVarListAttReq);

		DataSet dataSet = serverModel.getDataSet(dataSetReference);

		List<VariableDef> listOfVariable = new ArrayList<VariableDef>();

		for (FcModelNode member : dataSet) {
			listOfVariable.add(member.getMmsVariableDef());
		}
		return new GetNamedVariableListAttributesResponse(new BerBoolean(dataSet.isDeletable()),
				new SubSeqOf_listOfVariable(listOfVariable));
	}

	private static String convertToDataSetReference(ObjectName mmsObjectName) {
		if (mmsObjectName.domain_specific != null) {
			return mmsObjectName.domain_specific.domainId.toString() + "/"
					+ mmsObjectName.domain_specific.itemId.toString().replace('$', '.');
		}
		else if (mmsObjectName.aa_specific != null) {
			// format is "@DataSetName"
			return mmsObjectName.aa_specific.toString();
		}
		return null;
	}

	private BerNull handleCreateDataSetRequest(DefineNamedVariableListRequest mmsDefineNamedVariableListRequest)
			throws ServiceError {
		String dataSetReference = convertToDataSetReference(mmsDefineNamedVariableListRequest.variableListName);
		if (dataSetReference == null) {
			throw new ServiceError(ServiceError.PARAMETER_VALUE_INCONSISTENT,
					"handleCreateDataSetRequest: invalid MMS request (No DataSet Name Specified)");
		}

		List<VariableDef> nameList = mmsDefineNamedVariableListRequest.listOfVariable.seqOf;

		List<FcModelNode> dataSetMembers = new ArrayList<FcModelNode>(nameList.size());

		for (VariableDef variableDef : nameList) {
			dataSetMembers.add(serverModel.getNodeFromVariableDef(variableDef));
		}

		DataSet dataSet = new DataSet(dataSetReference, dataSetMembers, true);

		if (dataSetReference.startsWith("@")) {
			serverSap.addNonPersistentDataSet(dataSet, this);
		}
		else {
			serverModel.addDataSet(dataSet);
		}

		return new BerNull();
	}

	private DeleteNamedVariableListResponse handleDeleteDataSetRequest(
			DeleteNamedVariableListRequest mmsDelNamVarListReq) throws ServiceError {
		String dataSetRef = convertToDataSetReference(mmsDelNamVarListReq.listOfVariableListName.seqOf.get(0));

		// TODO handle non-persistent DataSet

		if (serverModel.removeDataSet(dataSetRef) == null) {
			if (serverModel.getDataSet(dataSetRef) == null) {
				return new DeleteNamedVariableListResponse(new BerInteger(0), new BerInteger(0));
			}
			else {
				return new DeleteNamedVariableListResponse(new BerInteger(1), new BerInteger(0));
			}
		}
		else {
			return new DeleteNamedVariableListResponse(new BerInteger(1), new BerInteger(1));
		}
	}

	// public void sendReport(Report report) throws IOException {
	// try {
	// // InformationReport infoReport = new InformationReport();
	//
	// // ObjectName rptName = new ObjectName();
	// // rptName.selectVmd_specific(new Identifier("RPT"));
	// //
	// // VariableAccessSpecification varAccessSpec = new
	// // VariableAccessSpecification();
	// // varAccessSpec.selectVariableListName(rptName);
	// //
	// // infoReport.setVariableAccessSpecification(varAccessSpec);
	//
	// VariableAccessSpecification varAccSpec = new
	// VariableAccessSpecification(null, new ObjectName(
	// new BerVisibleString("RPT"), null, null));
	//
	// OptFields optFlds = report.getOptFlds();
	//
	// List<AccessResult> listOfAccessResult = new LinkedList<AccessResult>();
	//
	// // rptID
	//
	// AccessResult accessRes1 = new AccessResult(null, null, null, null, null,
	// null, null, null, null,
	// new BerVisibleString(report.getRptId()), null, null, null, null, null,
	// null);
	// listOfAccessResult.add(accessRes1);
	//
	// // Reported OptFlds
	// // Data rptOptFlds = new Data();
	// // rptOptFlds.selectBit_string(new BitString(report.getOptFlds()
	// // .getValue()));
	// // AccessResult accessRes2 = new AccessResult();
	// // accessRes2.selectSuccess(rptOptFlds);
	// AccessResult accessRes2 = new AccessResult(null, null, null, null, new
	// BerBitString(report.getOptFlds()
	// .getValue(), 10), null, null, null, null, null, null, null, null, null,
	// null, null);
	//
	// listOfAccessResult.add(accessRes2);
	//
	// if (optFlds.isSeqNum()) {
	// // Data seqNum = new Data();
	// // seqNum.selectUnsigned(new Long(report.getSqNum()));
	// // AccessResult accessRes3 = new AccessResult();
	// // accessRes3.selectSuccess(seqNum);
	// AccessResult accessRes3 = new AccessResult(null, null, null, null, null,
	// null, new BerInteger(
	// report.getSqNum()), null, null, null, null, null, null, null, null,
	// null);
	// listOfAccessResult.add(accessRes3);
	// }
	//
	// if (optFlds.isTimeStamp()) {
	// // Data timeStamp = new Data();
	// // timeStamp.selectBinary_time(new TimeOfDay(
	// // MmsAsn1PrimitiveTypeConverter.toByteArray(report
	// // .getTimeOfEntry())));
	// // AccessResult accessRes4 = new AccessResult();
	// // accessRes4.selectSuccess(timeStamp);
	// AccessResult accessRes4 = new AccessResult(null, null, null, null, null,
	// null, null, null, null, null,
	// null, new
	// BerOctetString(MmsAsn1PrimitiveTypeConverter.toByteArray(report.getTimeOfEntry())),
	// null, null, null, null);
	// listOfAccessResult.add(accessRes4);
	// }
	//
	// if (optFlds.isDataSet()) {
	// // Data dataSet = new Data();
	// // dataSet.selectVisible_string(report.getDataSet().replace(".",
	// // "$"));
	// // AccessResult accessRes5 = new AccessResult();
	// // accessRes5.selectSuccess(dataSet);
	// AccessResult accessRes5 = new AccessResult(null, null, null, null, null,
	// null, null, null, null,
	// new BerVisibleString(report.getDataSetRef().replace(".", "$")), null,
	// null, null, null, null,
	// null);
	// listOfAccessResult.add(accessRes5);
	// }
	//
	// if (optFlds.isBufOvfl()) {
	// // Data bufOvfl = new Data();
	// // bufOvfl.selectBoolean_(report.getBufOvfl());
	// // AccessResult accessRes6 = new AccessResult();
	// // accessRes6.selectSuccess(bufOvfl);
	// AccessResult accessRes6 = new AccessResult(null, null, null, new
	// BerBoolean(report.getBufOvfl()), null,
	// null, null, null, null, null, null, null, null, null, null, null);
	// listOfAccessResult.add(accessRes6);
	// }
	//
	// if (optFlds.isEntryId()) {
	// // Data entryId = new Data();
	// // entryId.selectOctet_string(MmsAsn1PrimitiveTypeConverter
	// // .toByteArray(report.getEntryId()));
	// // AccessResult accessRes7 = new AccessResult();
	// // accessRes7.selectSuccess(entryId);
	// AccessResult accessRes7 = new AccessResult(null, null, null, null, null,
	// null, null, null,
	// new
	// BerOctetString(MmsAsn1PrimitiveTypeConverter.toByteArray(report.getEntryId())),
	// null, null,
	// null, null, null, null, null);
	// listOfAccessResult.add(accessRes7);
	// }
	//
	// if (optFlds.isConfigRef()) {
	// // Data confRev = new Data();
	// // confRev.selectUnsigned(report.getConvRev());
	// // AccessResult accessRes8 = new AccessResult();
	// // accessRes8.selectSuccess(confRev);
	// AccessResult accessRes8 = new AccessResult(null, null, null, null, null,
	// null, new BerInteger(
	// report.getConvRev()), null, null, null, null, null, null, null, null,
	// null);
	// listOfAccessResult.add(accessRes8);
	// }
	//
	// // TODO check if segmentation number is used
	//
	// // if (optFlds.isSeqNum()) {
	// // Data subSeqNum = new Data();
	// // subSeqNum.selectUnsigned(new Long(report.getSubSqNum()));
	// // AccessResult accessRes9 = new AccessResult();
	// // accessRes9.selectSuccess(subSeqNum);
	// // listOfAccessResult.add(accessRes9);
	// //
	// // Data moreFollows = new Data();
	// // moreFollows.selectBoolean_(report.getMoreSegmentsFollow());
	// // AccessResult accessRes10 = new AccessResult();
	// // accessRes10.selectSuccess(moreFollows);
	// // listOfAccessResult.add(accessRes10);
	// // }
	//
	// // TODO padding required?
	// // Data inclusionString = new Data();
	// // inclusionString.selectBit_string(new BitString(report
	// // .getInclusionBitString()));
	// // AccessResult accessRes11 = new AccessResult();
	// // accessRes11.selectSuccess(inclusionString);
	// AccessResult accessRes11 = new AccessResult(null, null, null, null, new
	// BerBitString(
	// report.getInclusionBitString(), 8), null, null, null, null, null, null,
	// null, null, null, null,
	// null);
	// listOfAccessResult.add(accessRes11);
	//
	// List<ReportEntryData> entryData = report.getEntryData();
	// List<AccessResult> dataRefs = new ArrayList<AccessResult>();
	// List<AccessResult> values = new ArrayList<AccessResult>();
	// List<AccessResult> reasonCodes = new ArrayList<AccessResult>();
	//
	// AccessResult dataRefAC;
	// // Data dataRef;
	// AccessResult reasonCodeAC;
	// AccessResult value;
	// // Data reasonCode;
	// byte[] reasonCodeValue = new byte[1];
	//
	// for (ReportEntryData data : entryData) {
	// if (optFlds.isDataRef()) {
	// // dataRef = new Data();
	// // dataRef.selectVisible_string(data.getValue().getReference()
	// // .get(0)
	// // + "/"
	// // + MMSReference.getMMSItemId(data.getValue()
	// // .getReference(), data.getValue()
	// // .getFunctionalConstraint()));
	// // dataRefAC = new AccessResult();
	// // dataRefAC.selectSuccess(dataRef);
	// dataRefAC = new AccessResult(null, null, null, null, null, null, null,
	// null, null,
	// new BerVisibleString(data.getValue().getReference().get(0)
	// + "/"
	// + MmsReference.getMMSItemId(data.getValue().getReference(),
	// data.getValue()
	// .getFunctionalConstraint())), null, null, null, null, null, null);
	// dataRefs.add(dataRefAC);
	// }
	//
	// Data result = data.getValue().getMmsDataObj();
	//
	// if (result == null) {
	// value = new AccessResult(new BerInteger(0L), null, null, null, null,
	// null, null, null, null, null,
	// null, null, null, null, null, null);
	// }
	// else {
	// value = new AccessResult(null, result.array, result.structure,
	// result.boolean_, result.bit_string,
	// result.integer, result.unsigned, result.floating_point,
	// result.octet_string,
	// result.visible_string, result.generalized_time, result.binary_time,
	// result.bcd,
	// result.booleanArray, result.mMSString, result.utc_time);
	// }
	//
	// values.add(value);
	//
	// if (optFlds.isReasonCode()) {
	// // reasonCode = new Data();
	// switch (data.getReasonCode()) {
	// case DCHG:
	// reasonCodeValue[0] = (byte) (reasonCodeValue[0] | (1 << 6));
	// break;
	// case QCHG:
	// reasonCodeValue[0] = (byte) (reasonCodeValue[0] | (1 << 5));
	// break;
	// case DUPD:
	// reasonCodeValue[0] = (byte) (reasonCodeValue[0] | (1 << 4));
	// break;
	// case INTEGRITY:
	// reasonCodeValue[0] = (byte) (reasonCodeValue[0] | (1 << 3));
	// break;
	// case GI:
	// reasonCodeValue[0] = (byte) (reasonCodeValue[0] | (1 << 2));
	// break;
	// case APPTRIGGER:
	// reasonCodeValue[0] = (byte) (byte) (reasonCodeValue[0] | (1 << 1));
	// }
	// // reasonCode.selectBit_string(new
	// // BitString(reasonCodeValue,
	// // 1));
	// // reasonCodeAC = new AccessResult();
	// // reasonCodeAC.selectSuccess(reasonCode);
	// reasonCodeAC = new AccessResult(null, null, null, null, new
	// BerBitString(reasonCodeValue, 8), null,
	// null, null, null, null, null, null, null, null, null, null);
	// reasonCodes.add(reasonCodeAC);
	// }
	// }
	//
	// listOfAccessResult.addAll(dataRefs);
	// listOfAccessResult.addAll(values);
	// listOfAccessResult.addAll(reasonCodes);
	//
	// // infoReport.setListOfAccessResult(listOfAccessResult);
	// // UnconfirmedService unconfirmedSer = new UnconfirmedService();
	// // unconfirmedSer.selectInformationReport(infoReport);
	// //
	// // UnconfirmedPDU unconfirmedPDU = new UnconfirmedPDU();
	// // unconfirmedPDU.setUnconfirmedService(unconfirmedSer);
	// //
	// // MmsPdu mmsResponsePdu = new MmsPdu();
	// // mmsResponsePdu.selectUnconfirmedPDU(unconfirmedPDU);
	// // sendAnMmsPdu(pConnection, mmsResponsePdu);
	// InformationReport infoReport = new InformationReport(varAccSpec,
	// new InformationReport.SubSeqOf_listOfAccessResult(listOfAccessResult));
	// UnconfirmedService unconfirmedSer = new UnconfirmedService(infoReport);
	// MmsPdu mmsResponsePdu = new MmsPdu(null, null, new
	// UnconfirmedPDU(unconfirmedSer), null, null, null);
	// sendAnMmsPdu(pConnection, mmsResponsePdu);
	// } catch (ServiceError e) {
	// logger.warn(e.getMessage());
	// sendAnMmsPdu(pConnection, createServiceErrorResponse(e));
	// }
	// }

	public void close() {
		if (acseAssociation != null) {
			acseAssociation.disconnect();
		}
	}

	public void sendReport(Report report) throws IOException {
		// TODO Auto-generated method stub

	}

	public void stop() {
		// TODO Auto-generated method stub

	}

}
