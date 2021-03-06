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

import java.util.List;

public class Urcb extends Rcb {

	public Urcb(ObjectReference objectReference, List<FcModelNode> children) {
		super(objectReference, Fc.RP, children, null);
	}

	/**
	 * Reserve URCB - The attribute Resv (if set to TRUE) shall indicate that the URCB is currently exclusively reserved
	 * for the client that has set the value to TRUE. Other clients shall not be allowed to set any attribute of that
	 * URCB.
	 */
	public BdaBoolean getResv() {
		return (BdaBoolean) children.get("Resv");
	}

}
