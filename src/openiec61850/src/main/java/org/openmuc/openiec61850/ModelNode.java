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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openmuc.jasn1.ber.types.string.BerVisibleString;
import org.openmuc.openiec61850.internal.mms.asn1.Data;
import org.openmuc.openiec61850.internal.mms.asn1.StructComponent;
import org.openmuc.openiec61850.internal.mms.asn1.TypeSpecification;
import org.openmuc.openiec61850.internal.mms.asn1.TypeSpecification.SubSeq_structure;
import org.openmuc.openiec61850.internal.mms.asn1.TypeSpecification.SubSeq_structure.SubSeqOf_components;

public abstract class ModelNode implements Iterable<ModelNode> {

	protected ObjectReference objectReference;
	protected Map<String, ModelNode> children;
	ModelNode parent;

	/**
	 * Copies the whole node with all of its children. Creates new BasicDataAttribute values but reuses
	 * ObjectReferences, FunctionalConstraints.
	 */
	public abstract ModelNode copy();

	public ModelNode getChild(String childName) {
		return getChild(childName, null);
	}

	public ModelNode getChild(String childName, Fc fc) {
		return children.get(childName);
	}

    public ModelNode findChild(String[] objectReferenceTokens) {
        if(objectReferenceTokens.length == 0) {
            return this;
        }
        
        ModelNode child = children.get(objectReferenceTokens[0]);
        if(child != null) {
            return child.findChild(stripFirstFromReference(objectReferenceTokens));
        }
        return null;
    }

    public String[] stripFirstFromReference(String[] reference) {
        return Arrays.copyOfRange(reference, 1, reference.length);
    }

	@SuppressWarnings("unchecked")
	public Collection<ModelNode> getChildren() {
		if (children == null) {
			return null;
		}
		return (Collection<ModelNode>) ((Collection<?>) children.values());
	}

	protected Iterator<Iterator<? extends ModelNode>> getIterators() {
		List<Iterator<? extends ModelNode>> iterators = new ArrayList<Iterator<? extends ModelNode>>();
		if (children != null) {
			iterators.add(children.values().iterator());
		}
		return iterators.iterator();
	}

	public ObjectReference getReference() {
		return objectReference;
	}

	public String getName() {
		return objectReference.getName();
	}

	@Override
	public Iterator<ModelNode> iterator() {
		return children.values().iterator();
	}

	public List<BasicDataAttribute> getBasicDataAttributes() {
		List<BasicDataAttribute> subBasicDataAttributes = new LinkedList<BasicDataAttribute>();
		for (ModelNode child : children.values()) {
			subBasicDataAttributes.addAll(child.getBasicDataAttributes());
		}
		return subBasicDataAttributes;
	}

	@Override
	public String toString() {
		return getReference().toString();
	}

	void setParent(ModelNode parent) {
		this.parent = parent;
	}

	public ModelNode getParent() {
		return parent;
	}

	Data getMmsDataObj() {
		return null;
	}

	void setValueFromMmsDataObj(Data data) throws ServiceError {
	}

	TypeSpecification getMmsTypeSpec() {
		List<StructComponent> structComponents = new LinkedList<StructComponent>();
		for (ModelNode child : children.values()) {
			structComponents.add(new StructComponent(new BerVisibleString(child.getName().getBytes()), child
					.getMmsTypeSpec()));
		}
		SubSeqOf_components componentsSequenceType = new SubSeqOf_components(structComponents);
		SubSeq_structure structure = new SubSeq_structure(null, componentsSequenceType);

		return new TypeSpecification(null, structure, null, null, null, null, null, null, null, null, null, null);
	}

}
