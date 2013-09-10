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

/**
 * 
 * Check packed list according to 61850-7-2
 * 
 */
public final class BdaCheck extends BdaBitString {

	public BdaCheck(ObjectReference objectReference) {
		super(objectReference, Fc.CO, null, 2, false, false);
		basicType = BdaType.CHECK;
		setDefault();
	}

	public boolean getSynchrocheck() {
		return ((value[0] & 0x80) == 0x80);
	}

	public boolean getInterlockCheck() {
		return ((value[0] & 0x40) == 0x40);
	}

	public void setSynchrocheck(boolean synchrocheck) {
		if (synchrocheck) {
			value[0] = (byte) (value[0] | 0x80);
		}
		else {
			value[0] = (byte) (value[0] & 0x7f);
		}
	}

	public void setInterlockCheck(boolean interlockCheck) {
		if (interlockCheck) {
			value[0] = (byte) (value[0] | 0x40);
		}
		else {
			value[0] = (byte) (value[0] & 0xbf);
		}
	}

	@Override
	public BdaCheck copy() {
		BdaCheck copy = new BdaCheck(objectReference);
		copy.setValue(value);
		copy.setMirror(this);
		return copy;
	}

}
