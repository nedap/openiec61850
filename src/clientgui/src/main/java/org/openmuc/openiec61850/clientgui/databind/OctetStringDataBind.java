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
package org.openmuc.openiec61850.clientgui.databind;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.openmuc.openiec61850.BdaOctetString;
import org.openmuc.openiec61850.BdaType;
import org.openmuc.openiec61850.clientgui.BasicDataBind;

public class OctetStringDataBind extends BasicDataBind<BdaOctetString> {

	public OctetStringDataBind(BdaOctetString data) {
		super(data, BdaType.OCTET_STRING);
	}

	@Override
	protected JComponent init() {
		byte[] value = data.getValue();
		StringBuilder sb;

		sb = new StringBuilder("OctetString [");
		for (int i = 0; i < value.length; i++) {
			sb.append(Integer.toHexString(value[i] & 0xff));
			if (i != value.length - 1) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return new JLabel(sb.toString());
	}

	@Override
	protected void resetImpl() {
		// ignore for now
	}

	@Override
	protected void writeImpl() {
		// ignore for now
	}
}
