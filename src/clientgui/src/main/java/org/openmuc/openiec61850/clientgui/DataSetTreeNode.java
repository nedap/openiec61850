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
package org.openmuc.openiec61850.clientgui;

import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;

import org.openmuc.openiec61850.ClientAssociation;
import org.openmuc.openiec61850.DataSet;
import org.openmuc.openiec61850.ServiceError;

public class DataSetTreeNode extends DefaultMutableTreeNode implements DataTreeNode {

	private static final long serialVersionUID = 7919716359809465616L;

	private final DataSet node;

	public DataSetTreeNode(String name, DataSet node) {
		super(name);
		this.node = node;
	}

	public DataSet getNode() {
		return node;
	}

	@Override
	public void reset(ClientAssociation association) throws ServiceError, IOException {
		if (association != null) {
			association.getDataSetValues(node);
		}
		for (int i = 0; i < getChildCount(); i++) {
			if (getChildAt(i) instanceof DataObjectTreeNode) {
				DataTreeNode child = (DataTreeNode) getChildAt(i);
				child.reset(null);
			}
		}
	}

	@Override
	public void writeValues(ClientAssociation association) throws ServiceError, IOException {
		for (int i = 0; i < getChildCount(); i++) {
			if (getChildAt(i) instanceof DataObjectTreeNode) {
				DataTreeNode child = (DataTreeNode) getChildAt(i);
				child.writeValues(null);
			}
		}
		if (association != null) {
			association.setDataSetValues(node);
		}
	}

	@Override
	public BasicDataBind<?> getData() {
		return null;
	}

	@Override
	public boolean writable() {
		return true;
	}

	@Override
	public boolean readable() {
		return true;
	}
}
