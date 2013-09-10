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

import org.openmuc.openiec61850.BdaInt8;
import org.openmuc.openiec61850.BdaType;

public class Int8DataBind extends TextFieldDataBind<BdaInt8> {

	private static final Int8Filter FILTER = new Int8Filter();

	public Int8DataBind(BdaInt8 data) {
		super(data, BdaType.INT8, FILTER);
	}

	@Override
	protected void resetImpl() {
		inputField.setText(data.getValue().toString());
	}

	@Override
	protected void writeImpl() {
		data.setValue(Byte.parseByte(inputField.getText()));
	}

	private static class Int8Filter extends AbstractFilter {
		@Override
		protected boolean test(String text) {
			try {
				Byte.parseByte(text);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}
}
