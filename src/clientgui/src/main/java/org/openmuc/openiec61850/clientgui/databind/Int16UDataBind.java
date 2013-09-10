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

import org.openmuc.openiec61850.BdaInt16U;
import org.openmuc.openiec61850.BdaType;

public class Int16UDataBind extends TextFieldDataBind<BdaInt16U> {

	private static final UInt16Filter FILTER = new UInt16Filter();

	public Int16UDataBind(BdaInt16U data) {
		super(data, BdaType.INT16U, FILTER);
	}

	@Override
	protected void resetImpl() {
		inputField.setText(data.getValue().toString());
	}

	@Override
	protected void writeImpl() {
		data.setValue(Integer.parseInt(inputField.getText()));
	}

	private static class UInt16Filter extends AbstractFilter {
		@Override
		protected boolean test(String text) {
			try {
				int value = Integer.parseInt(text);
				return value >= 0 && value <= 0xFFFF;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}
}
