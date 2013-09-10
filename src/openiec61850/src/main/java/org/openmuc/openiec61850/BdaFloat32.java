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

import java.nio.ByteBuffer;

import org.openmuc.jasn1.ber.types.BerInteger;
import org.openmuc.jasn1.ber.types.BerOctetString;
import org.openmuc.openiec61850.internal.mms.asn1.Data;
import org.openmuc.openiec61850.internal.mms.asn1.TypeSpecification;
import org.openmuc.openiec61850.internal.mms.asn1.TypeSpecification.SubSeq_floating_point;

public final class BdaFloat32 extends BasicDataAttribute {

	private byte[] value;

	private static TypeSpecification mmsTypeSpec = new TypeSpecification(null, null, null, null, null, null,
			new SubSeq_floating_point(new BerInteger(32), new BerInteger(8)), null, null, null, null, null);

	public BdaFloat32(ObjectReference objectReference, Fc fc, String sAddr, boolean dchg, boolean dupd) {
		super(objectReference, fc, sAddr, dchg, dupd);
		basicType = BdaType.FLOAT32;
		setDefault();
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	@Override
	void setValueFrom(BasicDataAttribute bda) {
		value = ((BdaFloat32) bda).getValue();
	}

	public void setFloat(Float value) {
		this.value = ByteBuffer.allocate(1 + 4).put((byte) 8).putFloat(value).array();
	}

	@Override
	public byte[] getValue() {
		return value;
	}

	public Float getFloat() {
		if (value == null) {
			return null;
		}
		return Float.intBitsToFloat(((0xff & value[1]) << 24) | ((0xff & value[2]) << 16) | ((0xff & value[3]) << 8)
				| ((0xff & value[4]) << 0));
	}

	@Override
	public void setDefault() {
		value = new byte[] { 8, 0, 0, 0, 0 };
	}

	@Override
	public BdaFloat32 copy() {
		BdaFloat32 copy = new BdaFloat32(objectReference, fc, sAddr, dchg, dupd);
		copy.setValue(value);
		copy.setMirror(this);
		return copy;
	}

	@Override
	Data getMmsDataObj() {
		if (value == null) {
			return null;
		}
		return new Data(null, null, null, null, null, null, new BerOctetString(value), null, null, null, null, null);
	}

	@Override
	void setValueFromMmsDataObj(Data data) throws ServiceError {
		if (data.floating_point == null || data.floating_point.octetString.length != 5) {
			throw new ServiceError(ServiceError.TYPE_CONFLICT,
					"expected type: floating_point as an octet string of size 5");
		}
		value = data.floating_point.octetString;
	}

	@Override
	TypeSpecification getMmsTypeSpec() {
		return mmsTypeSpec;
	}

	@Override
	public String toString() {
		return getReference().toString() + ": " + getFloat();
	}

}
