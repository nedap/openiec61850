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
import org.openmuc.openiec61850.internal.mms.asn1.Data;
import org.openmuc.openiec61850.internal.mms.asn1.TypeSpecification;

public final class BdaInt8 extends BasicDataAttribute {

	private Byte value;

	public BdaInt8(ObjectReference objectReference, Fc fc, String sAddr, boolean dchg, boolean dupd) {
		super(objectReference, fc, sAddr, dchg, dupd);
		basicType = BdaType.INT8;
		setDefault();
	}

	public void setValue(Byte value) {
		this.value = value;
	}

	@Override
	void setValueFrom(BasicDataAttribute bda) {
		value = ((BdaInt8) bda).getValue();
	}

	@Override
	public Byte getValue() {
		return value;
	}

	@Override
	public void setDefault() {
		value = 0;
	}

	@Override
	public BdaInt8 copy() {
		BdaInt8 copy = new BdaInt8(objectReference, fc, sAddr, dchg, dupd);
		copy.setValue(value);
		copy.setMirror(this);
		return copy;
	}

	@Override
	Data getMmsDataObj() {
		if (value == null) {
			return null;
		}
		return new Data(null, null, null, null, new BerInteger(value), null, null, null, null, null, null, null);
	}

	@Override
	void setValueFromMmsDataObj(Data data) throws ServiceError {
		if (data.integer == null) {
			throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: integer");
		}
		value = (byte) data.integer.val;
	}

	@Override
	TypeSpecification getMmsTypeSpec() {
		return new TypeSpecification(null, null, null, null, new BerInteger(8), null, null, null, null, null, null,
				null);
	}

	@Override
	public String toString() {
		return getReference().toString() + ": " + value;
	}

}
