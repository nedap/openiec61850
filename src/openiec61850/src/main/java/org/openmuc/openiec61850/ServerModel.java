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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openmuc.openiec61850.internal.mms.asn1.AlternateAccess;
import org.openmuc.openiec61850.internal.mms.asn1.ObjectName;
import org.openmuc.openiec61850.internal.mms.asn1.ObjectName.SubSeq_domain_specific;
import org.openmuc.openiec61850.internal.mms.asn1.VariableDef;

public final class ServerModel extends ModelNode {

	private final Map<String, DataSet> dataSets = new LinkedHashMap<String, DataSet>();

	private final Map<String, Urcb> urcbs = new HashMap<String, Urcb>();
	private final Map<String, Brcb> brcbs = new HashMap<String, Brcb>();

	public ServerModel(List<LogicalDevice> logicalDevices, Collection<DataSet> dataSets) {
		children = new LinkedHashMap<String, ModelNode>();
		objectReference = null;
		for (LogicalDevice logicalDevice : logicalDevices) {
			children.put(logicalDevice.getReference().getName(), logicalDevice);
			logicalDevice.setParent(this);
		}

		if (dataSets != null) {
			addDataSets(dataSets);
		}

		for (LogicalDevice ld : logicalDevices) {
			for (ModelNode ln : ld.getChildren()) {
				for (Urcb urcb : ((LogicalNode) ln).getUrcbs()) {
					urcbs.put(urcb.getReference().toString(), urcb);
				}
				for (Brcb brcb : ((LogicalNode) ln).getBrcbs()) {
					brcbs.put(brcb.getReference().toString(), brcb);
				}
			}
		}

	}

	@Override
	public ServerModel copy() {
		List<LogicalDevice> childCopies = new ArrayList<LogicalDevice>(children.size());
		for (ModelNode childNode : children.values()) {
			childCopies.add((LogicalDevice) childNode.copy());
		}

		List<DataSet> dataSetCopies = new ArrayList<DataSet>(dataSets.size());
		for (DataSet dataSet : dataSets.values()) {
			dataSetCopies.add(dataSet);
		}

		return new ServerModel(childCopies, dataSetCopies);
	}

	public DataSet getDataSet(String dataSetReference) {
		return dataSets.get(dataSetReference);
	}

	void addDataSet(DataSet dataSet) {
		dataSets.put(dataSet.getReferenceStr(), dataSet);
	}

	void addDataSets(Collection<DataSet> dataSets) {
		for (DataSet dataSet : dataSets) {
			addDataSet(dataSet);
		}
	}

	List<String> getDataSetNames(String ldName) {
		// TODO make thread save
		List<String> dataSetNames = new LinkedList<String>();
		for (String dataSetRef : dataSets.keySet()) {
			if (dataSetRef.startsWith(ldName)) {
				dataSetNames.add(dataSetRef.substring(dataSetRef.indexOf('/') + 1).replace('.', '$'));
			}
		}
		return dataSetNames;
	}

	public Collection<DataSet> getDataSets() {
		return dataSets.values();
	}

	/**
	 *
	 * @param dataSetReference
	 * @return returns the DataSet that was removed, null otherwise
	 */
	DataSet removeDataSet(String dataSetReference) {
		return dataSets.remove(dataSetReference);
	}

	void addUrcb(Urcb urcb) {
		urcbs.put(urcb.getReference().getName(), urcb);
	}

	public Collection<Urcb> getUrcbs() {
		return urcbs.values();
	}

	public Urcb getUrcb(String urcbRef) {
		return urcbs.get(urcbRef);
	}

	@Override
	public String toString() {
		return "Server";
	}

	/**
	 * Searches and returns the model node with the given object reference and FC. If searching for Logical Devices and
	 * Logical Nodes the given fc parameter may be <code>null</code>.
	 *
	 * @param objectReference
	 *            the object reference of the node that is being searched for. It has a syntax like "ldname/ln.do....".
	 * @param fc
	 *            the functional constraint of the requested model node. May be null for Logical Device and Logical Node
	 *            references.
	 * @return the model node if it was found or null otherwise
	 */
	public ModelNode findModelNode(ObjectReference objectReference, Fc fc) {

		ModelNode currentNode = this;
		Iterator<String> searchedNodeReferenceIterator = objectReference.iterator();

		while (searchedNodeReferenceIterator.hasNext()) {
			currentNode = currentNode.getChild(searchedNodeReferenceIterator.next(), fc);
			if (currentNode == null) {
				return null;
			}

		}
		return currentNode;
	}

	public ModelNode findModelNode(String objectReference, Fc fc) {
		return findModelNode(new ObjectReference(objectReference), fc);
	}

    /**
     * Find child. Do not care about FC. the chance of having two children with the same name is
     * REALLY small, so this does the trick in most cases.
     * Let's keep things simple, 61850 is more than complicated enough already.
     * @param objectReference
     * @return
     */
    public ModelNode findChildWithoutFc(String objectReference) {
        ObjectReference reference = new ObjectReference(objectReference);
        String[] tokens = new String[reference.size()];
        for(int i = 0; i < tokens.length; i++ ){
            tokens[i] = reference.get(i);
        }
        return findChild(tokens);
    }

	/**
	 * Returns the subModelNode that is referenced by the given VariableDef. Return null in case the referenced
	 * ModelNode is not found.
	 *
	 * @param variableDef
	 * @return
	 * @throws ServiceError
	 */
	FcModelNode getNodeFromVariableDef(VariableDef variableDef) throws ServiceError {

		ObjectName objectName = variableDef.variableSpecification.name;

		if (objectName == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"name in objectName is not selected");
		}

		SubSeq_domain_specific domainSpecific = objectName.domain_specific;

		if (domainSpecific == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"domain_specific in name is not selected");
		}

		ModelNode modelNode = getChild(domainSpecific.domainId.toString());

		if (modelNode == null) {
			return null;
		}

		String mmsItemId = domainSpecific.itemId.toString();
		int index1 = mmsItemId.indexOf('$');

		if (index1 == -1) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT, "invalid mms item id");
		}

		LogicalNode ln = (LogicalNode) modelNode.getChild(mmsItemId.substring(0, index1));

		if (ln == null) {
			return null;
		}

		int index2 = mmsItemId.indexOf('$', index1 + 1);

		if (index2 == -1) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT, "invalid mms item id");
		}

		Fc fc = Fc.fromString(mmsItemId.substring(index1 + 1, index2));

		if (fc == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"unknown functional constraint: " + mmsItemId.substring(index1 + 1, index2));
		}

		index1 = index2;

		index2 = mmsItemId.indexOf('$', index1 + 1);

		if (index2 == -1) {
			if (fc == Fc.RP) {
				return ln.getUrcb(mmsItemId.substring(index1 + 1));
			}
			if (fc == Fc.BR) {
				return ln.getBrcb(mmsItemId.substring(index1 + 1));
			}
			return (FcModelNode) ln.getChild(mmsItemId.substring(index1 + 1), fc);
		}

		modelNode = ln.getChild(mmsItemId.substring(index1 + 1, index2), fc);

		index1 = index2;
		index2 = mmsItemId.indexOf('$', index1 + 1);
		while (index2 != -1) {
			modelNode = modelNode.getChild(mmsItemId.substring(index1 + 1, index2));
			index1 = index2;
			index2 = mmsItemId.indexOf('$', index1 + 1);
		}

		modelNode = modelNode.getChild(mmsItemId.substring(index1 + 1));

		if (variableDef.alternateAccess == null) {
			return (FcModelNode) modelNode;
		}

		AlternateAccess.SubChoice altAccIt = variableDef.alternateAccess.seqOf.get(0);

		if (altAccIt.selectAlternateAccess != null) {
			modelNode = ((Array) modelNode).getChild((int) altAccIt.selectAlternateAccess.accessSelection.index.val);

			String mmsSubArrayItemId = altAccIt.selectAlternateAccess.alternateAccess.seqOf.get(0).component.toString();
			index1 = -1;
			index2 = mmsSubArrayItemId.indexOf('$');
			while (index2 != -1) {
				modelNode = modelNode.getChild(mmsSubArrayItemId.substring(index1 + 1, index2));
				index1 = index2;
				index2 = mmsItemId.indexOf('$', index1 + 1);
			}

			return (FcModelNode) modelNode.getChild(mmsSubArrayItemId.substring(index1 + 1));
		}
		else {
			return (FcModelNode) ((Array) modelNode).getChild((int) altAccIt.index.val);
		}

	}
}
