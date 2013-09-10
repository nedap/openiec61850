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

import org.openmuc.jasn1.ber.types.BerInteger;
import org.openmuc.jasn1.ber.types.string.BerVisibleString;
import org.openmuc.openiec61850.internal.mms.asn1.Data;
import org.openmuc.openiec61850.internal.mms.asn1.TypeSpecification;

public final class BdaVisibleString extends BasicDataAttribute {

	private byte[] value = new byte[] {};
	private final int maxLength;

	public BdaVisibleString(ObjectReference objectReference, Fc fc, String sAddr, int maxLength, boolean dchg,
			boolean dupd) {
		super(objectReference, fc, sAddr, dchg, dupd);
		if (value != null && value.length > maxLength) {
			throw new IllegalArgumentException("max_length less than value.length().");
		}
		basicType = BdaType.VISIBLE_STRING;
		this.maxLength = maxLength;
		setDefault();
	}

	@Override
	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		if (value == null || value.length > maxLength) {
			throw new IllegalArgumentException("value was null or VISIBLE_STRING value size exceeds maxLength of "
					+ maxLength);
		}
		this.value = value;
	}

	@Override
	void setValueFrom(BasicDataAttribute bda) {
		value = ((BdaVisibleString) bda).getValue();
	}

	public void setValue(String value) {
		setValue(value.getBytes());
	}

	public int getMaxLength() {
		return maxLength;
	}

	public String getStringValue() {
		if (value == null) {
			return null;
		}
		return new String(value);
	}

	@Override
	public void setDefault() {
		value = new byte[maxLength];
	}

	@Override
	public BdaVisibleString copy() {
		BdaVisibleString copy = new BdaVisibleString(objectReference, fc, sAddr, maxLength, dchg, dupd);
		copy.setValue(value);
		copy.setMirror(this);
		return copy;
	}

	@Override
	Data getMmsDataObj() {
		if (value == null) {
			return null;
		}
		return new Data(null, null, null, null, null, null, null, null, new BerVisibleString(value), null, null, null);
	}

	@Override
	void setValueFromMmsDataObj(Data data) throws ServiceError {
		if (data.visible_string == null) {
			throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: visible_string");
		}
		value = data.visible_string.octetString;
	}

	@Override
	TypeSpecification getMmsTypeSpec() {
		return new TypeSpecification(null, null, null, null, null, null, null, null, new BerInteger(maxLength * -1),
				null, null, null);
	}

	@Override
	public String toString() {
		if (value == null) {
			return getReference().toString() + ": null";
		}
		return getReference().toString() + ": " + new String(value);
	}

}
