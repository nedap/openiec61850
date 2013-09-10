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

public final class BdaTapCommand extends BdaBitString {

	public enum TapCommand {
		STOP(0), LOWER(1), HIGHER(2), RESERVED(3);
		private final int value;

		private TapCommand(int value) {
			this.value = value;
		}

		public int getIntValue() {
			return value;
		}
	}

	public BdaTapCommand(ObjectReference objectReference, Fc fc, String sAddr, boolean dchg, boolean dupd) {
		super(objectReference, fc, sAddr, 2, dchg, dupd);
		basicType = BdaType.TAP_COMMAND;
		setDefault();
	}

	/**
	 * Sets the value to TapCommand.STOP
	 */
	@Override
	public void setDefault() {
		value = new byte[] { 0x00, 0x00 };
	}

	@Override
	public BdaTapCommand copy() {
		BdaTapCommand copy = new BdaTapCommand(objectReference, fc, sAddr, dchg, dupd);
		copy.setValue(value);
		copy.setMirror(this);
		return copy;
	}

	public TapCommand getTapCommand() {

		if ((value[0] & 0xC0) == 0xC0) {
			return TapCommand.RESERVED;
		}

		if ((value[0] & 0x80) == 0x80) {
			return TapCommand.HIGHER;
		}

		if ((value[0] & 0x40) == 0x40) {
			return TapCommand.LOWER;
		}

		return TapCommand.STOP;

	}

	public void setTapCommand(TapCommand tapCommand) {
		if (tapCommand == TapCommand.RESERVED) {
			value[0] = (byte) 0xC0;
		}
		else if (tapCommand == TapCommand.HIGHER) {
			value[0] = (byte) 0x80;
		}
		else if (tapCommand == TapCommand.LOWER) {
			value[0] = (byte) 0x40;
		}
		else {
			value[0] = (byte) 0x00;
		}
	}

}
