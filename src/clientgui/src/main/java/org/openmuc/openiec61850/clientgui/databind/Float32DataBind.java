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

import org.openmuc.openiec61850.BdaFloat32;
import org.openmuc.openiec61850.BdaType;

public class Float32DataBind extends TextFieldDataBind<BdaFloat32> {

	private static final FloatFilter FILTER = new FloatFilter();

	public Float32DataBind(BdaFloat32 data) {
		super(data, BdaType.FLOAT32, FILTER);
	}

	@Override
	protected void resetImpl() {
		inputField.setText(data.getFloat().toString());
	}

	@Override
	protected void writeImpl() {
		float newFloat = Float.parseFloat(inputField.getText());
		data.setFloat(newFloat);
	}

	private static class FloatFilter extends AbstractFilter {
		@Override
		protected boolean test(String text) {
			try {
				Float.parseFloat(text);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}
}
