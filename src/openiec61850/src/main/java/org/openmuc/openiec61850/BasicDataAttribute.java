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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

abstract public class BasicDataAttribute extends FcModelNode {

	private BasicDataAttribute mirror;

	BasicDataAttribute(ObjectReference objectReference, Fc fc, String sAddr, boolean dchg, boolean dupd) {
		this.objectReference = objectReference;
		this.fc = fc;
		this.sAddr = sAddr;
		this.dchg = dchg;
		this.dupd = dupd;
	}

	/** attribute value type */
	protected BdaType basicType = null;

	/** short address, can be used by SCSM and for local data mapping */
	protected String sAddr = null;

	protected boolean dchg;
	protected boolean qchg;
	protected boolean dupd;

	public boolean getDchg() {
		return dchg;
	}

	public boolean getDupd() {
		return dupd;
	}

	public boolean getQchg() {
		return dupd;
	}

	public BdaType getBasicType() {
		return basicType;
	}

	public String getSAddr() {
		return sAddr;
	}

	@Override
	public ModelNode getChild(String childName, Fc fc) {
		return null;
	}

	@Override
	public ModelNode getChild(String childName) {
		return null;
	}

    @Override
    public ModelNode findChild(String[] objectReferenceTokens) {
        if(objectReferenceTokens.length == 0) {
            return this;
        }
        return null;
    }

	@Override
	public Collection<ModelNode> getChildren() {
		return null;
	}

	@Override
	public Iterator<ModelNode> iterator() {
		return Collections.<ModelNode> emptyList().iterator();
	}

	abstract public void setDefault();

	@Override
	public List<BasicDataAttribute> getBasicDataAttributes() {
		List<BasicDataAttribute> subBasicDataAttributes = new LinkedList<BasicDataAttribute>();
		subBasicDataAttributes.add(this);
		return subBasicDataAttributes;
	}

	abstract public Object getValue();

	abstract void setValueFrom(BasicDataAttribute bda);

	void setMirror(BasicDataAttribute bda) {
		mirror = bda;
	}

	BasicDataAttribute getMirror() {
		return mirror;
	}

}
