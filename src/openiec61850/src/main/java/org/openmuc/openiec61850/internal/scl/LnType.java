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
package org.openmuc.openiec61850.internal.scl;

import java.util.ArrayList;
import java.util.List;

import org.openmuc.openiec61850.SclParseException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class LnType extends AbstractType {

	// attributes not needed: lnClass, iedType

	public List<Do> dos = new ArrayList<Do>();

	public LnType(Node xmlNode) throws SclParseException {

		super(xmlNode);

		if (xmlNode.getAttributes().getNamedItem("lnClass") == null) {
			throw new SclParseException("Required attribute \"lnClass\" not found in LNType!");
		}

		NodeList elements = xmlNode.getChildNodes();

		for (int i = 0; i < elements.getLength(); i++) {
			Node node = elements.item(i);
			if (node.getNodeName().equals("DO")) {
				dos.add(new Do(node));
			}
		}
	}

}
